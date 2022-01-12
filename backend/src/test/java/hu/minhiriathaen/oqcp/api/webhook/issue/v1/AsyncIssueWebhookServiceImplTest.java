package hu.minhiriathaen.oqcp.api.webhook.issue.v1;

import static hu.minhiriathaen.oqcp.api.webhook.issue.v1.AsyncIssueWebhookServiceImpl.STATUS_FIELD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.jira.issue.JiraIssueApiClient;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.JiraIssueStatus;
import hu.minhiriathaen.oqcp.jira.transfer.PerformTransitionRequest;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueFields;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueTransition;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.IssueChangelog;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.IssueChangelogItem;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.IssueUpdatedEvent;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.UpdatedIssue;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.UpdatedIssueFields;
import hu.minhiriathaen.oqcp.openqualitychecker.issue.OpenQualityCheckerIssueApiClient;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.Advice;
import hu.minhiriathaen.oqcp.persistence.entity.AdviceGroup;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class AsyncIssueWebhookServiceImplTest extends ServiceTestBase {

  private static final String ADVICE_ID = "WARNING:NullPointerException:99627556";
  private static final String ADVICE_GROUP_BRANCH_NAME = "ADVICE_GROUP_BRANCH_NAME";
  private static final String OPEN_QUALITY_CHECKER_PROJECT_ID = "OPEN_QUALITY_CHECKER_PROJECT_ID";
  private static final String PROJECT_ID = "PROJECT_ID";
  private static final String ISSUE_ID = "ISSUE_ID";
  private static final String PARENT_ISSUE_ID = "PARENT_ISSUE_ID";
  private static final String TRANSITION_ID = "TRANSITION_ID";
  private static final String ADVICE_GROUP_ISSUE_TYPE_NAME = "OpenQualityChecker Advice group";
  private static final String ADVICE_GROUP_ISSUE_TYPE_ID = "ADVICE_GROUP_ISSUE_TYPE_ID";

  @Autowired protected transient AsyncIssueWebhookService asyncIssueWebhookService;

  @MockBean protected transient OpenQualityCheckerIssueApiClient openQualityCheckerIssueApiClient;

  @MockBean protected transient JiraIssueApiClient jiraIssueApiClient;

  private final transient AtlassianHostUser atlassianHostUser =
      createAtlassianHostUser(AtlassianUtil.BASE_URL);

  @Test
  public void testHandleIssueUpdatedNotifyOpenQualityCheckerAndResolveParent() {
    final IssueUpdatedEvent issueUpdatedEvent =
        createIssueUpdatedEvent(
            STATUS_FIELD, JiraIssueStatus.OPEN.name(), JiraIssueStatus.CLOSED.name());
    final AccountMapping accountMapping = mockDefaultAccountMapping();

    mockProjectMapping(accountMapping);
    mockAdvice();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockParentIssue(JiraIssueStatus.OPEN);
    mockSubtaskList(JiraIssueStatus.CLOSED);

    asyncIssueWebhookService.handleIssueUpdated(atlassianHostUser, issueUpdatedEvent);

    verify(openQualityCheckerIssueApiClient)
        .notifyIssueClosed(
            OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_PROJECT_ID, ADVICE_GROUP_BRANCH_NAME, ADVICE_ID);

    final IdentifiedJiraObject transition = new IdentifiedJiraObject();
    transition.setId(createTransition(JiraIssueStatus.OPEN).getId());

    final PerformTransitionRequest transitionRequest = new PerformTransitionRequest();
    transitionRequest.setTransition(transition);

    verify(jiraIssueApiClient)
        .performTransition(atlassianHostUser.getHost(), PARENT_ISSUE_ID, transitionRequest);
  }

  @Test
  public void testHandleIssueUpdatedIgnoredStatusChange() {
    final IssueUpdatedEvent issueUpdatedEvent =
        createIssueUpdatedEvent(
            STATUS_FIELD, JiraIssueStatus.OPEN.name(), JiraIssueStatus.IN_PROGRESS.name());
    final AccountMapping accountMapping = mockDefaultAccountMapping();

    mockProjectMapping(accountMapping);
    mockAdvice();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);

    asyncIssueWebhookService.handleIssueUpdated(atlassianHostUser, issueUpdatedEvent);

    verify(openQualityCheckerIssueApiClient, never()).notifyIssueClosed(any(), any(), any(), any());

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
  }

  @Test
  public void testHandleIssueUpdatedNoActionNeededBecauseParentClosed() {
    final IssueUpdatedEvent issueUpdatedEvent =
        createIssueUpdatedEvent(
            STATUS_FIELD, JiraIssueStatus.IN_PROGRESS.name(), JiraIssueStatus.CLOSED.name());
    final AccountMapping accountMapping = mockDefaultAccountMapping();

    mockProjectMapping(accountMapping);
    mockAdvice();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockParentIssue(JiraIssueStatus.CLOSED);
    mockSubtaskList(JiraIssueStatus.CLOSED);

    asyncIssueWebhookService.handleIssueUpdated(atlassianHostUser, issueUpdatedEvent);

    verify(openQualityCheckerIssueApiClient, never()).notifyIssueClosed(any(), any(), any(), any());

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
  }

  @Test
  public void testHandleIssueUpdatedNoActionNeededBecauseSubtaskStatus() {
    final IssueUpdatedEvent issueUpdatedEvent =
        createIssueUpdatedEvent(
            STATUS_FIELD, JiraIssueStatus.IN_PROGRESS.name(), JiraIssueStatus.RESOLVED.name());
    final AccountMapping accountMapping = mockDefaultAccountMapping();

    mockProjectMapping(accountMapping);
    mockAdvice();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockParentIssue(JiraIssueStatus.OPEN);
    mockSubtaskList(JiraIssueStatus.OPEN);

    asyncIssueWebhookService.handleIssueUpdated(atlassianHostUser, issueUpdatedEvent);

    verify(openQualityCheckerIssueApiClient, never()).notifyIssueClosed(any(), any(), any(), any());

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
  }

  @Test
  public void testHandleIssueUpdatedNoStatusChange() {
    final IssueUpdatedEvent issueUpdatedEvent =
        createIssueUpdatedEvent("description", "description1", "description2");
    final AccountMapping accountMapping = mockDefaultAccountMapping();

    mockProjectMapping(accountMapping);
    mockAdvice();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);

    asyncIssueWebhookService.handleIssueUpdated(atlassianHostUser, issueUpdatedEvent);

    verify(openQualityCheckerIssueApiClient, never()).notifyIssueClosed(any(), any(), any(), any());

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
  }

  private IssueUpdatedEvent createIssueUpdatedEvent(
      final String field, final String fromString, final String toString) {
    final IdentifiedJiraObject project = new IdentifiedJiraObject();
    project.setId(PROJECT_ID);

    final UpdatedIssueFields updatedIssueFields = new UpdatedIssueFields();
    updatedIssueFields.setProject(project);

    final IdentifiedJiraObject issueType = new IdentifiedJiraObject();
    issueType.setId(ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setName(ADVICE_GROUP_ISSUE_TYPE_NAME);
    updatedIssueFields.setIssueType(issueType);
    updatedIssueFields.setParent(createParentIssue(JiraIssueStatus.OPEN));

    final UpdatedIssue updatedIssue = new UpdatedIssue();
    updatedIssue.setId(ISSUE_ID);
    updatedIssue.setFields(updatedIssueFields);

    final IssueChangelogItem changelogItem = new IssueChangelogItem();
    changelogItem.setField(field);
    changelogItem.setFromString(fromString);
    changelogItem.setToString(toString);

    final IssueChangelog changelog = new IssueChangelog();
    changelog.setItems(Arrays.asList(changelogItem));

    final IssueUpdatedEvent issueUpdatedEvent = new IssueUpdatedEvent();
    issueUpdatedEvent.setIssue(updatedIssue);
    issueUpdatedEvent.setChangelog(changelog);

    return issueUpdatedEvent;
  }

  private void mockProjectMapping(final AccountMapping accountMapping) {
    final ProjectMapping projectMapping = new ProjectMapping();
    projectMapping.setOpenQualityCheckerProjectId(OPEN_QUALITY_CHECKER_PROJECT_ID);
    projectMapping.setCreatorAtlassianUserAccountId(AtlassianUtil.USER_ID);
    projectMapping.setJiraProjectId(PROJECT_ID);
    projectMapping.setAccountMapping(accountMapping);

    when(projectMappingRepository.findByAccountMappingAndJiraProjectId(accountMapping, PROJECT_ID))
        .thenReturn(Arrays.asList(projectMapping));
  }

  private void mockAdvice() {
    final AdviceGroup adviceGroup = new AdviceGroup();
    adviceGroup.setBranchName(ADVICE_GROUP_BRANCH_NAME);
    adviceGroup.setOpenQualityCheckerProjectId(OPEN_QUALITY_CHECKER_PROJECT_ID);
    adviceGroup.setJiraProjectId(PROJECT_ID);

    final Advice advice = new Advice();
    advice.setAdviceId(ADVICE_ID);
    advice.setJiraIssueId(ISSUE_ID);
    advice.setGroup(adviceGroup);

    when(adviceRepository.findByJiraIssueIdAndGroupOpenQualityCheckerProjectIdIn(
            ISSUE_ID, Arrays.asList(OPEN_QUALITY_CHECKER_PROJECT_ID)))
        .thenReturn(Optional.of(advice));
  }

  private IssueBean createParentIssue(final JiraIssueStatus jiraIssueStatus) {
    final IdentifiedJiraObject status = new IdentifiedJiraObject();
    status.setName(jiraIssueStatus.name());

    final IssueFields issueFields = new IssueFields();
    issueFields.setStatus(status);

    final IssueBean parentIssue = new IssueBean();
    parentIssue.setId(PARENT_ISSUE_ID);
    parentIssue.setFields(issueFields);
    parentIssue.setTransitions(Arrays.asList(createTransition(JiraIssueStatus.RESOLVED)));

    return parentIssue;
  }

  private void mockParentIssue(final JiraIssueStatus jiraIssueStatus) {
    when(jiraIssueApiClient.getIssue(eq(atlassianHostUser.getHost()), eq(PARENT_ISSUE_ID)))
        .thenReturn(createParentIssue(jiraIssueStatus));
  }

  private IssueTransition createTransition(final JiraIssueStatus jiraIssueStatus) {
    final IdentifiedJiraObject openStatusTarget = new IdentifiedJiraObject();
    openStatusTarget.setName(jiraIssueStatus.name());

    final IssueTransition openTransition = new IssueTransition();
    openTransition.setId(TRANSITION_ID);
    openTransition.setTarget(openStatusTarget);

    return openTransition;
  }

  private void mockSubtaskList(final JiraIssueStatus jiraIssueStatus) {
    final IdentifiedJiraObject status = new IdentifiedJiraObject();
    status.setName(jiraIssueStatus.name());

    final IssueFields issueFields = new IssueFields();
    issueFields.setStatus(status);

    final IssueBean subtaskIssue1 = new IssueBean();
    subtaskIssue1.setFields(issueFields);

    final IssueBean subtaskIssue2 = new IssueBean();
    subtaskIssue2.setFields(issueFields);

    when(jiraIssueApiClient.getSubtasksByIssueTypeAndParentIssue(
            eq(atlassianHostUser.getHost()), eq(ADVICE_GROUP_ISSUE_TYPE_ID), eq(PARENT_ISSUE_ID)))
        .thenReturn(Arrays.asList(subtaskIssue1, subtaskIssue2));
  }
}
