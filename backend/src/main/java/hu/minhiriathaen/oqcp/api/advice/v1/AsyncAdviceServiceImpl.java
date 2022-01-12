package hu.minhiriathaen.oqcp.api.advice.v1;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.issue.JiraIssueManager;
import hu.minhiriathaen.oqcp.jira.transfer.JiraIssueStatus;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueTransition;
import hu.minhiriathaen.oqcp.persistence.entity.Advice;
import hu.minhiriathaen.oqcp.persistence.entity.AdviceGroup;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.repository.IssueTypeRepository;
import hu.minhiriathaen.oqcp.persistence.repository.ProjectMappingRepository;
import hu.minhiriathaen.oqcp.util.AtlassianHostUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncAdviceServiceImpl implements AsyncAdviceService {

  private final IssueTypeRepository issueTypeRepository;

  private final ProjectMappingRepository projectMappingRepository;

  private final JiraIssueManager jiraIssueManager;

  private final AdviceManager adviceManager;

  private final ContributorExtractor contributorExtractor;

  private final ReasonExtractor reasonExtractor;

  private final AtlassianHostUtil atlassianHostUtil;

  @Async
  @Override
  public void assessmentChanged(
      final AdviceAssessmentChangedTransfer adviceAssessmentChangedTransfer) {
    final String adviceId = adviceAssessmentChangedTransfer.getAdvice().getId();

    log.info(
        "[{}] Processing assessment changed event: {}", adviceId, adviceAssessmentChangedTransfer);

    final String openQualityCheckerProjectId =
        Long.toString(adviceAssessmentChangedTransfer.getProjectId());

    final Optional<ProjectMapping> projectMapping =
        projectMappingRepository.findByOpenQualityCheckerProjectId(openQualityCheckerProjectId);

    if (projectMapping.isEmpty()) {
      log.warn(
          "[{}] Project mapping not found for OQC project id: {}",
          adviceId,
          openQualityCheckerProjectId);
      return;
    }

    final String atlassianHostUrl = projectMapping.get().getAccountMapping().getAtlassianHostUrl();

    final AtlassianHost atlassianHost = atlassianHostUtil.getAtlassianHost(atlassianHostUrl);

    final Optional<Advice> advice = adviceManager.getAdvice(adviceId, openQualityCheckerProjectId);

    if (Assessment.DISLIKE.equals(adviceAssessmentChangedTransfer.getAdvice().getAssessment())) {
      if (advice.isPresent()) {
        log.info("[{}] Advice was disliked: {}", adviceId, adviceAssessmentChangedTransfer);
        resolveAdviceEntity(atlassianHost, advice.get());
      } else {
        log.info(
            "[{}] Advice not exists, no further action needed: {}",
            adviceId,
            adviceAssessmentChangedTransfer);
      }
    } else {
      if (advice.isPresent()) {
        log.info("[{}] Advice was liked: {}", adviceId, adviceAssessmentChangedTransfer);
        processExistingAdviceEntity(atlassianHost, advice.get());
      } else {
        processNotExistingAdviceEntity(
            atlassianHost,
            projectMapping.get().getJiraProjectId(),
            adviceAssessmentChangedTransfer);
      }
    }
  }

  @Async
  @Override
  public void resolved(final AdviceResolvedTransfer adviceResolvedTransfer) {
    final String openQualityCheckerProjectId = Long.toString(adviceResolvedTransfer.getProjectId());
    final String adviceId = adviceResolvedTransfer.getId();

    log.info("[{}] Processing advice resolved event: {}", adviceId, adviceResolvedTransfer);

    final Optional<ProjectMapping> projectMapping =
        projectMappingRepository.findByOpenQualityCheckerProjectId(openQualityCheckerProjectId);

    if (projectMapping.isEmpty()) {
      log.warn(
          "[{}] Project mapping not found for OQC project id: {}",
          adviceId,
          openQualityCheckerProjectId);
      return;
    }

    final Optional<Advice> advice = adviceManager.getAdvice(adviceId, openQualityCheckerProjectId);

    if (advice.isEmpty()) {
      log.error(
          "[{}] Advice not found for OQC project id: {}", adviceId, openQualityCheckerProjectId);
      return;
    }

    final String atlassianHostUrl = projectMapping.get().getAccountMapping().getAtlassianHostUrl();

    final AtlassianHost atlassianHost = atlassianHostUtil.getAtlassianHost(atlassianHostUrl);

    final String commitUrl = adviceResolvedTransfer.getCommitUrl();
    final Boolean adviceResolved = resolveAdviceEntity(atlassianHost, advice.get());

    if (adviceResolved) {
      jiraIssueManager.addResolutionComment(
          atlassianHost, advice.get().getJiraIssueId(), commitUrl);
    }
  }

  private Boolean resolveAdviceEntity(final AtlassianHost atlassianHost, final Advice advice) {
    log.info("[{}] Resolving existing advice: {}", advice.getAdviceId(), advice);

    final IssueBean adviceJiraIssue =
        jiraIssueManager.getJiraIssue(atlassianHost, advice.getAdviceId(), advice.getJiraIssueId());

    final JiraIssueStatus currentStatus =
        JiraIssueStatus.fromString(adviceJiraIssue.getFields().getStatus().getName());

    final Optional<IssueTransition> optionalTransition;

    switch (currentStatus) {
      case OPEN:
      case REOPENED:
      case IN_PROGRESS:
        optionalTransition =
            jiraIssueManager.findTransitionForTargetStatus(
                adviceJiraIssue, JiraIssueStatus.RESOLVED);
        break;
      default:
        log.info(
            "[{}] Issue status is {}, no further action needed",
            advice.getAdviceId(),
            currentStatus);
        return false;
    }

    jiraIssueManager.performTransition(
        optionalTransition, currentStatus, advice.getJiraIssueId(), atlassianHost);

    return true;
  }

  private void processExistingAdviceEntity(final AtlassianHost atlassianHost, final Advice advice) {
    log.info("[{}] Processing existing advice: {}", advice.getAdviceId(), advice);

    final IssueBean adviceJiraIssue =
        jiraIssueManager.getJiraIssue(atlassianHost, advice.getAdviceId(), advice.getJiraIssueId());

    final JiraIssueStatus currentStatus =
        JiraIssueStatus.fromString(adviceJiraIssue.getFields().getStatus().getName());

    final Optional<IssueTransition> optionalTransition;
    switch (currentStatus) {
      case IN_PROGRESS:
        optionalTransition =
            jiraIssueManager.findTransitionForTargetStatus(adviceJiraIssue, JiraIssueStatus.OPEN);
        break;
      case RESOLVED:
      case CLOSED:
        optionalTransition =
            jiraIssueManager.findTransitionForTargetStatus(
                adviceJiraIssue, JiraIssueStatus.REOPENED);
        break;
      default:
        log.info(
            "[{}] Issue status is {}, no further action needed",
            advice.getAdviceId(),
            currentStatus);
        return;
    }

    jiraIssueManager.performTransition(
        optionalTransition, currentStatus, advice.getJiraIssueId(), atlassianHost);
  }

  private void processNotExistingAdviceEntity(
      final AtlassianHost atlassianHost,
      final String jiraProjectId,
      final AdviceAssessmentChangedTransfer adviceAssessmentChangedTransfer) {

    final String adviceId = adviceAssessmentChangedTransfer.getAdvice().getId();

    log.info("[{}] Processing not existing advice", adviceId);

    final Optional<IssueType> optionalIssueType =
        issueTypeRepository.findByAtlassianHostUrl(atlassianHost.getBaseUrl());

    if (optionalIssueType.isEmpty()) {
      log.warn(
          "[{}] Issue type not found for Atlassian host url: {}",
          adviceId,
          atlassianHost.getBaseUrl());
      return;
    }

    final String branchName = adviceAssessmentChangedTransfer.getBranchName();
    final String contributor =
        contributorExtractor.extractContributor(adviceAssessmentChangedTransfer);
    final String openQualityCheckerProjectId =
        Long.toString(adviceAssessmentChangedTransfer.getProjectId());
    final Optional<AdviceGroup> optionalAdviceGroup =
        adviceManager.getLastAdviceGroup(openQualityCheckerProjectId, branchName, contributor);

    final AdviceGroup adviceGroup;
    if (optionalAdviceGroup.isPresent()) {
      final AdviceGroup existingAdviceGroup = optionalAdviceGroup.get();

      log.info("[{}] Processing existing advice group: {}", adviceId, existingAdviceGroup);

      final IssueBean adviceGroupIssue =
          jiraIssueManager.getJiraIssue(
              atlassianHost, adviceId, existingAdviceGroup.getJiraIssueId());

      final JiraIssueStatus adviceGroupIssueStatus = JiraIssueStatus.fromIssue(adviceGroupIssue);

      log.info("[{}] Advice group status is {}", adviceId, adviceGroupIssueStatus);

      if (JiraIssueStatus.OPEN.equals(adviceGroupIssueStatus)) {
        adviceGroup = existingAdviceGroup;
      } else {
        adviceGroup =
            adviceManager.createAdviceGroup(
                atlassianHost,
                adviceId,
                branchName,
                openQualityCheckerProjectId,
                contributor,
                jiraProjectId,
                optionalIssueType.get().getAdviceGroupIssueTypeId());
      }

    } else {
      log.info("[{}] Processing not existing advice group", adviceId);

      adviceGroup =
          adviceManager.createAdviceGroup(
              atlassianHost,
              adviceId,
              branchName,
              openQualityCheckerProjectId,
              contributor,
              jiraProjectId,
              optionalIssueType.get().getAdviceGroupIssueTypeId());
    }

    final String adviceUrl = adviceAssessmentChangedTransfer.getAdviceUrl();
    final String adviceSummary = adviceAssessmentChangedTransfer.getAdvice().getAdvice();
    final String reason = reasonExtractor.extractReason(adviceAssessmentChangedTransfer);

    adviceManager.createAdvice(
        atlassianHost,
        reason,
        adviceId,
        adviceUrl,
        adviceSummary,
        optionalIssueType.get().getAdviceIssueTypeId(),
        adviceGroup);
  }
}
