package hu.minhiriathaen.oqcp.api.advice.v1;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.issue.JiraIssueManager;
import hu.minhiriathaen.oqcp.persistence.entity.Advice;
import hu.minhiriathaen.oqcp.persistence.entity.AdviceGroup;
import hu.minhiriathaen.oqcp.persistence.repository.AdviceGroupRepository;
import hu.minhiriathaen.oqcp.persistence.repository.AdviceRepository;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdviceManager {

  private final AdviceRepository adviceRepository;

  private final AdviceGroupRepository adviceGroupRepository;

  private final JiraIssueManager jiraIssueManager;

  public Optional<Advice> getAdvice(
      final String adviceId, final String openQualityCheckerProjectId) {
    return adviceRepository.findByAdviceIdAndGroupOpenQualityCheckerProjectId(
        adviceId, openQualityCheckerProjectId);
  }

  public Optional<AdviceGroup> getLastAdviceGroup(
      final String openQualityCheckerProjectId, final String branchName, final String contributor) {
    return adviceGroupRepository
        .findTopByOpenQualityCheckerProjectIdAndBranchNameAndContributorOrderByCreatedAtDesc(
            openQualityCheckerProjectId, branchName, contributor);
  }

  public void createAdvice(
      final AtlassianHost atlassianHost,
      final String reason,
      final String adviceId,
      final String adviceUrl,
      final String adviceSummary,
      final String adviceIssueTypeId,
      final AdviceGroup adviceGroup) {

    final String adviceIssueId =
        jiraIssueManager.createAdviceIssue(
            atlassianHost,
            reason,
            adviceId,
            adviceUrl,
            adviceSummary,
            adviceGroup,
            adviceIssueTypeId);

    final Advice advice = new Advice();
    advice.setJiraIssueId(adviceIssueId);
    advice.setAdviceId(adviceId);
    advice.setGroup(adviceGroup);

    adviceRepository.save(advice);

    log.info("[{}] Advice saved: {}", adviceId, advice);
  }

  public AdviceGroup createAdviceGroup(
      final AtlassianHost atlassianHost,
      final String adviceId,
      final String branchName,
      final String openQualityCheckerProjectId,
      final String contributor,
      final String jiraProjectId,
      final String adviceGroupIssueTypeId) {

    final String adviceGroupIssueId =
        jiraIssueManager.createAdviceGroupIssue(
            atlassianHost,
            adviceId,
            branchName,
            jiraProjectId,
            adviceGroupIssueTypeId,
            contributor);

    final AdviceGroup adviceGroup = new AdviceGroup();
    adviceGroup.setJiraProjectId(jiraProjectId);
    adviceGroup.setJiraIssueId(adviceGroupIssueId);
    adviceGroup.setBranchName(branchName);
    adviceGroup.setCreatedAt(new Date());
    adviceGroup.setOpenQualityCheckerProjectId(openQualityCheckerProjectId);

    if (StringUtils.isNotBlank(contributor)) {
      adviceGroup.setContributor(contributor);
    }

    adviceGroupRepository.save(adviceGroup);

    log.info("[{}] Advice group saved: {}", adviceId, adviceGroup);

    return adviceGroup;
  }
}
