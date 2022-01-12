package hu.minhiriathaen.oqcp.api.project.v1;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.jira.issuetype.JiraIssueTypeApiClient;
import hu.minhiriathaen.oqcp.jira.workflow.JiraWorkflowApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectApiClient;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.repository.IssueTypeRepository;
import hu.minhiriathaen.oqcp.util.HostUtil;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncProjectMappingServiceImpl implements AsyncProjectMappingService {

  private final IssueTypeRepository issueTypeRepository;

  private final OpenQualityCheckerProjectApiClient openQualityCheckerProjectApiClient;

  private final JiraIssueTypeApiClient jiraIssueTypeApiClient;

  private final JiraWorkflowApiClient jiraWorkflowApiClient;

  @Async
  @Override
  public void processDeletedProjectMappings(
      final String openQualityCheckerUserToken, final Set<String> openQualityCheckerProjectIds) {
    openQualityCheckerProjectIds.forEach(
        id ->
            openQualityCheckerProjectApiClient.unsubscribeFromProject(
                openQualityCheckerUserToken, id));
  }

  @Async
  @Override
  public void processCreatedProjectMappings(
      final AtlassianHostUser atlassianHostUser,
      final String openQualityCheckerUserToken,
      final List<ProjectMapping> projectMappings) {

    final String accountId = HostUtil.unwrapAccountId(atlassianHostUser);

    if (CollectionUtils.isEmpty(projectMappings)) {
      log.info("[{}] No Project mapping to process", accountId);
      return;
    }

    log.info("[{}] Processing created Project mappings: {}", accountId, projectMappings);

    final AtlassianHost atlassianHost = HostUtil.unwrapHost(atlassianHostUser);

    final Optional<IssueType> optionalIssueType =
        issueTypeRepository.findByAtlassianHostUrl(atlassianHost.getBaseUrl());

    if (optionalIssueType.isEmpty()) {
      log.warn(
          "[{}] No IssueType found for Atlassian host: {}", accountId, atlassianHost.getBaseUrl());
      return;
    }

    final String jiraProjectId = projectMappings.get(0).getJiraProjectId();
    final IssueType issueType = optionalIssueType.get();

    jiraIssueTypeApiClient.addIssueTypesToIssueTypeSchemeForProject(
        atlassianHost, jiraProjectId, issueType);

    jiraWorkflowApiClient.assignDefaultWorkflowToIssueTypesAtProject(
        atlassianHost, jiraProjectId, issueType);

    projectMappings.forEach(
        projectMapping ->
            openQualityCheckerProjectApiClient.subscribeToProject(
                openQualityCheckerUserToken, projectMapping.getOpenQualityCheckerProjectId()));

    log.info("[{}] Processing finished", accountId);
  }
}
