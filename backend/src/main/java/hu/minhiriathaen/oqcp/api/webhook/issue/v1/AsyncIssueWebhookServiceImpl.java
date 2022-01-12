package hu.minhiriathaen.oqcp.api.webhook.issue.v1;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.jira.issue.JiraIssueApiClient;
import hu.minhiriathaen.oqcp.jira.issue.JiraIssueManager;
import hu.minhiriathaen.oqcp.jira.transfer.JiraIssueStatus;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueTransition;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.IssueChangelogItem;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.IssueUpdatedEvent;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.UpdatedIssue;
import hu.minhiriathaen.oqcp.openqualitychecker.issue.OpenQualityCheckerIssueApiClient;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.Advice;
import hu.minhiriathaen.oqcp.persistence.entity.AdviceGroup;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.AdviceRepository;
import hu.minhiriathaen.oqcp.persistence.repository.ProjectMappingRepository;
import hu.minhiriathaen.oqcp.util.AccountMappingUtil;
import hu.minhiriathaen.oqcp.util.UserMappingUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncIssueWebhookServiceImpl implements AsyncIssueWebhookService {

  public static final String STATUS_FIELD = "status";

  private final OpenQualityCheckerIssueApiClient openQualityCheckerIssueApiClient;
  private final AccountMappingUtil accountMappingUtil;
  private final UserMappingUtil userMappingUtil;
  private final AdviceRepository adviceRepository;
  private final ProjectMappingRepository projectMappingRepository;
  private final JiraIssueApiClient jiraIssueApiClient;
  private final JiraIssueManager jiraIssueManager;

  @Async
  @Override
  public void handleIssueUpdated(
      final AtlassianHostUser atlassianHostUser, final IssueUpdatedEvent event) {
    final UpdatedIssue issue = event.getIssue();
    final String issueId = issue.getId();

    final Optional<IssueChangelogItem> optionalStatusChange = getStatusChange(event);

    if (optionalStatusChange.isEmpty()) {
      log.info("[{}] Issue updated event does not contain status change, event ignored", issueId);
      return;
    }

    boolean isIgnoredStatusChange = true;

    final IssueChangelogItem statusChange = optionalStatusChange.get();

    if (isStatusChangedFromOpenToClosed(statusChange)) {
      isIgnoredStatusChange = false;
      notifyOpenQualityCheckerAboutClosedIssue(atlassianHostUser, issue);
    }

    if (isStatusChangedToResolvedOrClosed(statusChange)) {
      isIgnoredStatusChange = false;
      resolveParentIssueIfPossible(atlassianHostUser, issue);
    }

    if (isIgnoredStatusChange) {
      log.info("[{}] No action needed, event ignored", issueId);
    }
  }

  private Optional<IssueChangelogItem> getStatusChange(final IssueUpdatedEvent event) {
    return event.getChangelog().getItems().stream()
        .filter(issueChangelogItem -> STATUS_FIELD.equals(issueChangelogItem.getField()))
        .findFirst();
  }

  private boolean isStatusChangedFromOpenToClosed(final IssueChangelogItem issueChangelogItem) {
    final JiraIssueStatus fromStatus =
        JiraIssueStatus.fromString(issueChangelogItem.getFromString());
    final JiraIssueStatus toStatus = JiraIssueStatus.fromString(issueChangelogItem.getToString());

    return fromStatus == JiraIssueStatus.OPEN && toStatus == JiraIssueStatus.CLOSED;
  }

  private void notifyOpenQualityCheckerAboutClosedIssue(
      final AtlassianHostUser atlassianHostUser, final UpdatedIssue issue) {

    final String issueId = issue.getId();

    log.info("[{}] Notifying OpenQualityChecker about closed issue", issueId);

    final AccountMapping accountMapping = accountMappingUtil.findAccountMapping(atlassianHostUser);

    final String jiraProjectId = issue.getFields().getProject().getId();

    final List<ProjectMapping> projectMappings =
        projectMappingRepository.findByAccountMappingAndJiraProjectId(
            accountMapping, jiraProjectId);

    if (projectMappings.isEmpty()) {
      log.warn(
          "[{}] Project mapping not found for Atlassian host '{}' and Jira project ID '{}'",
          issueId,
          accountMapping.getAtlassianHostUrl(),
          jiraProjectId);
      return;
    }

    final List<String> openQualityCheckerProjectIds =
        projectMappings.stream()
            .map(ProjectMapping::getOpenQualityCheckerProjectId)
            .collect(Collectors.toList());

    final Optional<Advice> optionalAdvice =
        adviceRepository.findByJiraIssueIdAndGroupOpenQualityCheckerProjectIdIn(
            issueId, openQualityCheckerProjectIds);

    if (optionalAdvice.isEmpty()) {
      log.warn(
          "[{}] Advice not found for issue and Atlassian host '{}'",
          issueId,
          accountMapping.getAtlassianHostUrl());
      return;
    }

    final Advice advice = optionalAdvice.get();
    final AdviceGroup adviceGroup = advice.getGroup();

    final String openQualityCheckerProjectId = adviceGroup.getOpenQualityCheckerProjectId();
    final String branchName = adviceGroup.getBranchName();
    final String adviceId = advice.getAdviceId();

    final String adminAtlassianUserAccountId =
        projectMappings.get(0).getCreatorAtlassianUserAccountId();
    final UserMapping userMapping =
        userMappingUtil.findUserMapping(accountMapping, adminAtlassianUserAccountId);

    openQualityCheckerIssueApiClient.notifyIssueClosed(
        userMapping.getOpenQualityCheckerUserToken(),
        openQualityCheckerProjectId,
        branchName,
        adviceId);
  }

  private boolean isStatusChangedToResolvedOrClosed(final IssueChangelogItem issueChangelogItem) {
    final JiraIssueStatus toStatus = JiraIssueStatus.fromString(issueChangelogItem.getToString());

    return toStatus == JiraIssueStatus.RESOLVED || toStatus == JiraIssueStatus.CLOSED;
  }

  private void resolveParentIssueIfPossible(
      final AtlassianHostUser atlassianHostUser, final UpdatedIssue issue) {
    final AtlassianHost atlassianHost = atlassianHostUser.getHost();

    final IssueBean parentIssue =
        jiraIssueApiClient.getIssue(atlassianHost, issue.getFields().getParent().getId());

    final String parentIssueId = parentIssue.getId();
    final JiraIssueStatus parentIssueStatus = JiraIssueStatus.fromIssue(parentIssue);

    if (JiraIssueStatus.RESOLVED == parentIssueStatus
        || JiraIssueStatus.CLOSED == parentIssueStatus) {
      log.info(
          "[{}] No status change needed because the parent issue status is: {}",
          parentIssueId,
          parentIssueStatus);
    } else {
      final String issueTypeId = issue.getFields().getIssueType().getId();

      if (checkAllSubtasksAreResolvedOrClosed(atlassianHost, issueTypeId, parentIssueId)) {
        final Optional<IssueTransition> optionalTransition =
            jiraIssueManager.findTransitionForTargetStatus(parentIssue, JiraIssueStatus.RESOLVED);

        jiraIssueManager.performTransition(
            optionalTransition, JiraIssueStatus.RESOLVED, parentIssueId, atlassianHost);
      } else {
        log.info(
            "[{}] No status change needed because not all subtasks are resolved or closed",
            parentIssueId);
      }
    }
  }

  private boolean checkAllSubtasksAreResolvedOrClosed(
      final AtlassianHost atlassianHost, final String issueTypeId, final String parentIssueId) {
    final List<IssueBean> subtasksByIssueType =
        jiraIssueApiClient.getSubtasksByIssueTypeAndParentIssue(
            atlassianHost, issueTypeId, parentIssueId);

    return subtasksByIssueType.stream()
        .allMatch(
            subtask ->
                Arrays.asList(JiraIssueStatus.RESOLVED, JiraIssueStatus.CLOSED)
                    .contains(JiraIssueStatus.fromIssue(subtask)));
  }
}
