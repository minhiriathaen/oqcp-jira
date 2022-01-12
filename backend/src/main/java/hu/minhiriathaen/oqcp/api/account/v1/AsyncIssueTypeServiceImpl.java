package hu.minhiriathaen.oqcp.api.account.v1;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.issuetype.JiraIssueTypeApiClient;
import hu.minhiriathaen.oqcp.jira.transfer.CreateIssueTypeRequest;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeCategory;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeDetails;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.persistence.repository.IssueTypeRepository;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncIssueTypeServiceImpl implements AsyncIssueTypeService {

  private final IssueTypeRepository issueTypeRepository;

  private final JiraIssueTypeApiClient jiraIssueTypeApiClient;

  private final ContextHelper contextHelper;

  private static final String ADVICE_GROUP_ISSUE_TYPE_NAME = "OpenQualityChecker Advice group";
  private static final String ADVICE_ISSUE_TYPE_NAME = "OpenQualityChecker Advice";

  @Async
  @Override
  public void createIssueTypes(final AtlassianHost atlassianHost) {
    final String adviceGroupIssueTypeId;
    final String adviceIssueTypeId;

    final String atlassianHostBaseUrl = atlassianHost.getBaseUrl();

    final Optional<IssueType> optionalIssueType =
        issueTypeRepository.findByAtlassianHostUrl(atlassianHostBaseUrl);

    final List<IssueTypeDetails> issueTypeDetailsByAtlassianHost =
        jiraIssueTypeApiClient.getAllIssueTypes(atlassianHost);

    final Optional<IssueTypeDetails> groupIssueTypeDetails =
        issueTypeDetailsByAtlassianHost.stream()
            .filter(jiraIssueType -> ADVICE_GROUP_ISSUE_TYPE_NAME.equals(jiraIssueType.getName()))
            .findFirst();

    final Optional<IssueTypeDetails> subtaskIssueTypeDetails =
        issueTypeDetailsByAtlassianHost.stream()
            .filter(jiraIssueType -> ADVICE_ISSUE_TYPE_NAME.equals(jiraIssueType.getName()))
            .findFirst();

    if (groupIssueTypeDetails.isPresent()) {
      adviceGroupIssueTypeId = groupIssueTypeDetails.get().getId();
    } else {
      final IdentifiedJiraObject newGroupIssueType =
          createIssueType(atlassianHost, IssueTypeCategory.STANDARD);

      adviceGroupIssueTypeId = newGroupIssueType.getId();
    }

    if (subtaskIssueTypeDetails.isPresent()) {
      adviceIssueTypeId = subtaskIssueTypeDetails.get().getId();
    } else {
      final IdentifiedJiraObject newSubtaskIssueType =
          createIssueType(atlassianHost, IssueTypeCategory.SUBTASK);

      adviceIssueTypeId = newSubtaskIssueType.getId();
    }

    if (optionalIssueType.isPresent()) {
      final IssueType issueType = optionalIssueType.get();

      if (groupIssueTypeDetails.isEmpty()
          || !issueType.getAdviceGroupIssueTypeId().equals(groupIssueTypeDetails.get().getId())) {
        updateAdviceGroupIssueTypeId(issueType, adviceGroupIssueTypeId);
      }

      if (subtaskIssueTypeDetails.isEmpty()
          || !issueType.getAdviceIssueTypeId().equals(subtaskIssueTypeDetails.get().getId())) {
        updateAdviceIssueTypeId(issueType, adviceIssueTypeId);
      }
    } else {
      saveIssueType(atlassianHostBaseUrl, adviceGroupIssueTypeId, adviceIssueTypeId);
    }
  }

  private IdentifiedJiraObject createIssueType(
      final AtlassianHost atlassianHost, final IssueTypeCategory issueTypeCategory) {
    final CreateIssueTypeRequest createIssueTypeRequest = new CreateIssueTypeRequest();

    createIssueTypeRequest.setName(
        issueTypeCategory.equals(IssueTypeCategory.STANDARD)
            ? ADVICE_GROUP_ISSUE_TYPE_NAME
            : ADVICE_ISSUE_TYPE_NAME);
    createIssueTypeRequest.setType(issueTypeCategory);

    log.info(
        "[{}] Create issue type request sent to '{}' host: '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        createIssueTypeRequest);

    return jiraIssueTypeApiClient.createIssueType(atlassianHost, createIssueTypeRequest);
  }

  private void saveIssueType(
      final String atlassianHostUrl,
      final String adviceGroupIssueTypeId,
      final String adviceIssueTypeId) {
    final IssueType newIssueType = new IssueType();
    newIssueType.setAtlassianHostUrl(atlassianHostUrl);
    newIssueType.setAdviceGroupIssueTypeId(adviceGroupIssueTypeId);
    newIssueType.setAdviceIssueTypeId(adviceIssueTypeId);

    issueTypeRepository.save(newIssueType);

    log.info("[{}] New issue type saved: {}", contextHelper.getUserIdForLog(), newIssueType);
  }

  private void updateAdviceGroupIssueTypeId(
      final IssueType issuetype, final String adviceGroupIssueTypeId) {

    final String originalAdviceGroupIssueTypeId = issuetype.getAdviceGroupIssueTypeId();

    issuetype.setAdviceGroupIssueTypeId(adviceGroupIssueTypeId);

    issueTypeRepository.save(issuetype);

    log.info(
        "[{}] IssueType adviceGroupIssueTypeId value updated from '{}' to '{}'",
        contextHelper.getUserIdForLog(),
        originalAdviceGroupIssueTypeId,
        adviceGroupIssueTypeId);
  }

  private void updateAdviceIssueTypeId(final IssueType issuetype, final String adviceIssueTypeId) {

    final String originalAdviceIssueTypeId = issuetype.getAdviceIssueTypeId();

    issuetype.setAdviceIssueTypeId(adviceIssueTypeId);

    issueTypeRepository.save(issuetype);

    log.info(
        "[{}] IssueType adviceIssueTypeId value updated from '{}' to '{}'",
        contextHelper.getUserIdForLog(),
        originalAdviceIssueTypeId,
        adviceIssueTypeId);
  }
}
