package hu.minhiriathaen.oqcp.api.advice.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.minhiriathaen.oqcp.jira.issue.JiraIssueApiClient;
import hu.minhiriathaen.oqcp.jira.issue.JiraIssueManager;
import hu.minhiriathaen.oqcp.jira.transfer.CommentRequest;
import hu.minhiriathaen.oqcp.jira.transfer.CreateIssueRequest;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.JiraIssueStatus;
import hu.minhiriathaen.oqcp.jira.transfer.PerformTransitionRequest;
import hu.minhiriathaen.oqcp.jira.transfer.document.Attributes;
import hu.minhiriathaen.oqcp.jira.transfer.document.ContentMark;
import hu.minhiriathaen.oqcp.jira.transfer.document.ParagraphNode;
import hu.minhiriathaen.oqcp.jira.transfer.document.RootDocumentNode;
import hu.minhiriathaen.oqcp.jira.transfer.document.TextNode;
import hu.minhiriathaen.oqcp.jira.transfer.issue.BasicIssueFields;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueFields;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueTransition;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.Advice;
import hu.minhiriathaen.oqcp.persistence.entity.AdviceGroup;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.test.util.AtlassianHostBuilder;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ResourceUtil;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;

@Slf4j
@SpringBootTest
class AsyncAdviceServiceImplTest extends ServiceTestBase {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String OPEN_QUALITY_CHECKER_PROJECT_ID = "12";
  private static final String ADVICE_ISSUE_TYPE_ID = "AdviceIssueTypeId";
  private static final String ADVICE_GROUP_ISSUE_TYPE_ID = "AdviceGroupIssueTypeId";
  private static final String ADVICE_ID = "WARNING:NullPointerException:99627556";
  private static final String ADVICE = "Source code element should be refactored";
  private static final String ADVICE_ISSUE_ID = "AdviceIssueId";
  private static final String ADVICE_GROUP_ISSUE_ID = "AdviceGroupIssueId";
  private static final String ADVICE_GROUP_ISSUE_ID_OTHER = "AdviceGroupIssueIdOther";
  private static final String TRANSITION_ID_PREFIX = "TransitionId";
  private static final long ASYNC_CALL_TIMEOUT = 3000L;
  private static final String JIRA_PROJECT_ID = "JiraProjectId";
  private static final String MASTER_BRANCH_NAME = "master";
  private static final String CONTRIBUTOR = "developer.name.2";
  private static final String TEXT_TYPE = "text";
  private static final String TEXT_SEPARATOR = " - ";

  @Autowired private transient AsyncAdviceService asyncAdviceService;

  @MockBean private transient JiraIssueApiClient jiraIssueApiClient;

  @Value("classpath:advice-like.json")
  private transient Resource adviceLikeEvent;

  @Value("classpath:advice-like-without-advice-url.json")
  private transient Resource adviceLikeEventWithoutAdviceUrl;

  @Value("classpath:advice-dislike.json")
  private transient Resource adviceDislikeEvent;

  @Value("classpath:advice-resolved.json")
  private transient Resource adviceResolvedEvent;

  @Value("classpath:advice-resolved-no-commitUrl.json")
  private transient Resource adviceResolvedNoCommitUrlEvent;

  @Test
  public void testResolvedAdviceNoCommitUr() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.OPEN);

    asyncAdviceService.resolved(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceResolvedNoCommitUrlEvent), AdviceResolvedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.RESOLVED);

    final ArgumentCaptor<CommentRequest> commentRequestCaptor =
        ArgumentCaptor.forClass(CommentRequest.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT))
        .addComment(eq(atlassianHost), eq(ADVICE_ISSUE_ID), commentRequestCaptor.capture());

    assertResolveAdviceIssueNoCommitUrl(commentRequestCaptor.getValue());
  }

  @Test
  public void testResolvedAdviceIsOpen() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.OPEN);

    asyncAdviceService.resolved(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceResolvedEvent), AdviceResolvedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.RESOLVED);

    final ArgumentCaptor<CommentRequest> commentRequestCaptor =
        ArgumentCaptor.forClass(CommentRequest.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT))
        .addComment(eq(atlassianHost), eq(ADVICE_ISSUE_ID), commentRequestCaptor.capture());

    assertResolveAdviceIssue(commentRequestCaptor.getValue());
  }

  @Test
  public void testResolvedAdviceIsReOpened() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.REOPENED);

    asyncAdviceService.resolved(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceResolvedEvent), AdviceResolvedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.RESOLVED);

    final ArgumentCaptor<CommentRequest> commentRequestCaptor =
        ArgumentCaptor.forClass(CommentRequest.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT))
        .addComment(eq(atlassianHost), eq(ADVICE_ISSUE_ID), commentRequestCaptor.capture());

    assertResolveAdviceIssue(commentRequestCaptor.getValue());
  }

  @Test
  public void testResolvedAdviceIsInProgress() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.IN_PROGRESS);

    asyncAdviceService.resolved(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceResolvedEvent), AdviceResolvedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.RESOLVED);

    final ArgumentCaptor<CommentRequest> commentRequestCaptor =
        ArgumentCaptor.forClass(CommentRequest.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT))
        .addComment(eq(atlassianHost), eq(ADVICE_ISSUE_ID), commentRequestCaptor.capture());

    assertResolveAdviceIssue(commentRequestCaptor.getValue());
  }

  @Test
  public void testResolvedAdviceIsClosed() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.CLOSED);

    asyncAdviceService.resolved(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceResolvedEvent), AdviceResolvedTransfer.class));

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
    verify(jiraIssueApiClient, never()).addComment(any(), any(), any());
  }

  @Test
  public void testResolvedAdviceIsResolved() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.RESOLVED);

    asyncAdviceService.resolved(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceResolvedEvent), AdviceResolvedTransfer.class));

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
    verify(jiraIssueApiClient, never()).addComment(any(), any(), any());
  }

  @Test
  public void testAssessmentChangedAdviceIsInProgress() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.IN_PROGRESS);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.OPEN);

    verify(jiraIssueApiClient, never())
        .createIssue(eq(atlassianHost), any(CreateIssueRequest.class));
    verify(adviceRepository, never()).save(any(Advice.class));
    verify(adviceGroupRepository, never()).save(any(AdviceGroup.class));
  }

  @Test
  public void testAssessmentChangedAdviceIsResolved() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdviceGroup(true);
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.RESOLVED);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.REOPENED);

    verify(jiraIssueApiClient, never())
        .createIssue(eq(atlassianHost), any(CreateIssueRequest.class));
    verify(adviceRepository, never()).save(any(Advice.class));
    verify(adviceGroupRepository, never()).save(any(AdviceGroup.class));
  }

  @Test
  public void testAssessmentChangedAdviceIsClosed() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.CLOSED);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.REOPENED);

    verify(jiraIssueApiClient, never())
        .createIssue(eq(atlassianHost), any(CreateIssueRequest.class));
    verify(adviceRepository, never()).save(any(Advice.class));
    verify(adviceGroupRepository, never()).save(any(AdviceGroup.class));
  }

  @Test
  public void testAssessmentChangedAdviceIsOpen() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.OPEN);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    verify(jiraIssueApiClient, never())
        .performTransition(
            eq(atlassianHost), eq(ADVICE_ISSUE_ID), any(PerformTransitionRequest.class));
  }

  @Test
  public void testAssessmentChangedAdviceIsReopened() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdviceGroup(true);
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.REOPENED);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    verify(jiraIssueApiClient, never())
        .createIssue(eq(atlassianHost), any(CreateIssueRequest.class));
    verify(adviceRepository, never()).save(any(Advice.class));
    verify(adviceGroupRepository, never()).save(any(AdviceGroup.class));
    verify(jiraIssueApiClient, never())
        .performTransition(
            any(AtlassianHost.class), anyString(), any(PerformTransitionRequest.class));
  }

  @Test
  public void testAssessmentChangedExistingAdviceGroupWithOpenStatus() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdviceGroup(true);

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockCreateAdviceIssue();

    mockGetIssue(atlassianHost, ADVICE_GROUP_ISSUE_ID, JiraIssueStatus.OPEN);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    final ArgumentCaptor<CreateIssueRequest> createIssueRequestCaptor =
        ArgumentCaptor.forClass(CreateIssueRequest.class);

    final ArgumentCaptor<Advice> saveAdviceCaptor = ArgumentCaptor.forClass(Advice.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT))
        .createIssue(eq(atlassianHost), createIssueRequestCaptor.capture());

    verify(adviceRepository, timeout(ASYNC_CALL_TIMEOUT)).save(saveAdviceCaptor.capture());
    verify(adviceGroupRepository, never()).save(any(AdviceGroup.class));

    assertAdviceIssue(createIssueRequestCaptor.getValue(), ADVICE_GROUP_ISSUE_ID);
    assertSavedAdvice(saveAdviceCaptor, ADVICE_GROUP_ISSUE_ID);
  }

  @Test
  public void testAssessmentChangedExistingAdviceGroupWithOpenStatusWithoutAdviceUrl()
      throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdviceGroup(false);

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockCreateAdviceIssue();

    mockGetIssue(atlassianHost, ADVICE_GROUP_ISSUE_ID, JiraIssueStatus.OPEN);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEventWithoutAdviceUrl),
            AdviceAssessmentChangedTransfer.class));

    final ArgumentCaptor<CreateIssueRequest> createIssueRequestCaptor =
        ArgumentCaptor.forClass(CreateIssueRequest.class);

    final ArgumentCaptor<Advice> saveAdviceCaptor = ArgumentCaptor.forClass(Advice.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT))
        .createIssue(eq(atlassianHost), createIssueRequestCaptor.capture());

    verify(adviceRepository, timeout(ASYNC_CALL_TIMEOUT)).save(saveAdviceCaptor.capture());
    verify(adviceGroupRepository, never()).save(any(AdviceGroup.class));

    assertAdviceIssueWithoutAdviceUrl(createIssueRequestCaptor.getValue(), ADVICE_GROUP_ISSUE_ID);
    assertSavedAdvice(saveAdviceCaptor, ADVICE_GROUP_ISSUE_ID);
  }

  @Test
  public void testAssessmentChangedExistingAdviceGroupWithInProgressStatus() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdviceGroup(true);

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_GROUP_ISSUE_ID, JiraIssueStatus.IN_PROGRESS);

    final String createDate = createDate();

    mockCreateIssues(ADVICE_GROUP_ISSUE_ID_OTHER);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    final ArgumentCaptor<CreateIssueRequest> createIssueRequestCaptor =
        ArgumentCaptor.forClass(CreateIssueRequest.class);

    final ArgumentCaptor<Advice> saveAdviceCaptor = ArgumentCaptor.forClass(Advice.class);
    final ArgumentCaptor<AdviceGroup> saveAdviceGroupCaptor =
        ArgumentCaptor.forClass(AdviceGroup.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT).times(2))
        .createIssue(eq(atlassianHost), createIssueRequestCaptor.capture());

    verify(adviceRepository, timeout(ASYNC_CALL_TIMEOUT)).save(saveAdviceCaptor.capture());
    verify(adviceGroupRepository, timeout(ASYNC_CALL_TIMEOUT))
        .save(saveAdviceGroupCaptor.capture());

    assertAdviceGroupIssue(createIssueRequestCaptor.getAllValues().get(0), createDate, true);
    assertAdviceIssue(createIssueRequestCaptor.getAllValues().get(1), ADVICE_GROUP_ISSUE_ID_OTHER);

    assertSavedAdviceGroup(saveAdviceGroupCaptor, ADVICE_GROUP_ISSUE_ID_OTHER);
    assertSavedAdvice(saveAdviceCaptor, ADVICE_GROUP_ISSUE_ID_OTHER);
  }

  @Test
  public void testAssessmentChangedExistingAdviceGroupWithInProgressStatusWithoutAdviceUrl()
      throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdviceGroup(true);

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_GROUP_ISSUE_ID, JiraIssueStatus.IN_PROGRESS);

    final String createDate = createDate();

    mockCreateIssues(ADVICE_GROUP_ISSUE_ID_OTHER);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEventWithoutAdviceUrl),
            AdviceAssessmentChangedTransfer.class));

    final ArgumentCaptor<CreateIssueRequest> createIssueRequestCaptor =
        ArgumentCaptor.forClass(CreateIssueRequest.class);

    final ArgumentCaptor<Advice> saveAdviceCaptor = ArgumentCaptor.forClass(Advice.class);
    final ArgumentCaptor<AdviceGroup> saveAdviceGroupCaptor =
        ArgumentCaptor.forClass(AdviceGroup.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT).times(2))
        .createIssue(eq(atlassianHost), createIssueRequestCaptor.capture());

    verify(adviceRepository, timeout(ASYNC_CALL_TIMEOUT)).save(saveAdviceCaptor.capture());
    verify(adviceGroupRepository, timeout(ASYNC_CALL_TIMEOUT))
        .save(saveAdviceGroupCaptor.capture());

    assertAdviceGroupIssue(createIssueRequestCaptor.getAllValues().get(0), createDate, false);
    assertAdviceIssueWithoutAdviceUrl(
        createIssueRequestCaptor.getAllValues().get(1), ADVICE_GROUP_ISSUE_ID_OTHER);

    assertSavedAdviceGroup(saveAdviceGroupCaptor, ADVICE_GROUP_ISSUE_ID_OTHER);
    assertSavedAdvice(saveAdviceCaptor, ADVICE_GROUP_ISSUE_ID_OTHER);
  }

  @Test
  public void testAssessmentChangedNotExistingAdviceGroup() throws IOException {
    mockProjectMapping();
    mockIssueType();

    final AtlassianHost atlassianHost = mockAtlassianHost();

    final String createDate = createDate();

    mockCreateIssues(ADVICE_GROUP_ISSUE_ID);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEvent), AdviceAssessmentChangedTransfer.class));

    final ArgumentCaptor<CreateIssueRequest> createIssueRequestCaptor =
        ArgumentCaptor.forClass(CreateIssueRequest.class);

    final ArgumentCaptor<Advice> saveAdviceCaptor = ArgumentCaptor.forClass(Advice.class);
    final ArgumentCaptor<AdviceGroup> saveAdviceGroupCaptor =
        ArgumentCaptor.forClass(AdviceGroup.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT).times(2))
        .createIssue(eq(atlassianHost), createIssueRequestCaptor.capture());

    verify(adviceRepository, timeout(ASYNC_CALL_TIMEOUT)).save(saveAdviceCaptor.capture());
    verify(adviceGroupRepository, timeout(ASYNC_CALL_TIMEOUT))
        .save(saveAdviceGroupCaptor.capture());

    assertAdviceGroupIssue(createIssueRequestCaptor.getAllValues().get(0), createDate, true);
    assertAdviceIssue(createIssueRequestCaptor.getAllValues().get(1), ADVICE_GROUP_ISSUE_ID);

    assertSavedAdviceGroup(saveAdviceGroupCaptor, ADVICE_GROUP_ISSUE_ID);
    assertSavedAdvice(saveAdviceCaptor, ADVICE_GROUP_ISSUE_ID);
  }

  @Test
  public void testAssessmentChangedNotExistingAdviceGroupWithoutAdviceUrl() throws IOException {
    mockProjectMapping();
    mockIssueType();

    final AtlassianHost atlassianHost = mockAtlassianHost();

    final String createDate = createDate();

    mockCreateIssues(ADVICE_GROUP_ISSUE_ID);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceLikeEventWithoutAdviceUrl),
            AdviceAssessmentChangedTransfer.class));

    final ArgumentCaptor<CreateIssueRequest> createIssueRequestCaptor =
        ArgumentCaptor.forClass(CreateIssueRequest.class);

    final ArgumentCaptor<Advice> saveAdviceCaptor = ArgumentCaptor.forClass(Advice.class);
    final ArgumentCaptor<AdviceGroup> saveAdviceGroupCaptor =
        ArgumentCaptor.forClass(AdviceGroup.class);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT).times(2))
        .createIssue(eq(atlassianHost), createIssueRequestCaptor.capture());

    verify(adviceRepository, timeout(ASYNC_CALL_TIMEOUT)).save(saveAdviceCaptor.capture());
    verify(adviceGroupRepository, timeout(ASYNC_CALL_TIMEOUT))
        .save(saveAdviceGroupCaptor.capture());

    assertAdviceGroupIssue(createIssueRequestCaptor.getAllValues().get(0), createDate, false);
    assertAdviceIssueWithoutAdviceUrl(
        createIssueRequestCaptor.getAllValues().get(1), ADVICE_GROUP_ISSUE_ID);

    assertSavedAdviceGroup(saveAdviceGroupCaptor, ADVICE_GROUP_ISSUE_ID);
    assertSavedAdvice(saveAdviceCaptor, ADVICE_GROUP_ISSUE_ID);
  }

  @Test
  public void testAssessmentChangedDislikedIssueIsResolved() throws IOException {
    mockProjectMapping();
    mockAdvice();

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.RESOLVED);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceDislikeEvent), AdviceAssessmentChangedTransfer.class));

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
  }

  @Test
  public void testAssessmentChangedDislikedAdviceNotExists() throws IOException {
    mockProjectMapping();

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceDislikeEvent), AdviceAssessmentChangedTransfer.class));

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
  }

  @Test
  public void testAssessmentChangedDislikedIssueIsClosed() throws IOException {
    mockProjectMapping();
    mockAdvice();

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.CLOSED);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceDislikeEvent), AdviceAssessmentChangedTransfer.class));

    verify(jiraIssueApiClient, never()).performTransition(any(), any(), any());
  }

  @Test
  public void testAssessmentChangedDislikedIssueIsInProgress() throws IOException {
    mockProjectMapping();
    mockAdvice();

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.IN_PROGRESS);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceDislikeEvent), AdviceAssessmentChangedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.RESOLVED);
  }

  @Test
  public void testAssessmentChangedDislikedIssueIsInReopened() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.REOPENED);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceDislikeEvent), AdviceAssessmentChangedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.RESOLVED);
  }

  @Test
  public void testAssessmentChangedDislikedIssueIsInOpen() throws IOException {
    mockProjectMapping();
    mockIssueType();
    mockAdvice();
    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_ISSUE_ID, JiraIssueStatus.OPEN);

    asyncAdviceService.assessmentChanged(
        OBJECT_MAPPER.readValue(
            ResourceUtil.asString(adviceDislikeEvent), AdviceAssessmentChangedTransfer.class));

    assertIssueTransition(atlassianHost, JiraIssueStatus.RESOLVED);
  }

  private void assertAdviceGroupIssue(
      final CreateIssueRequest createIssueRequest,
      final String createDate,
      final boolean withContributor) {

    final BasicIssueFields adviceFields = createIssueRequest.getFields();

    if (withContributor) {
      assertThat(adviceFields.getSummary())
          .isEqualTo(
              JiraIssueManager.OPEN_QUALITY_CHECKER_ADVICE_GROUP
                  + TEXT_SEPARATOR
                  + createDate
                  + TEXT_SEPARATOR
                  + MASTER_BRANCH_NAME
                  + TEXT_SEPARATOR
                  + CONTRIBUTOR);
    } else {
      assertThat(adviceFields.getSummary())
          .isEqualTo(
              JiraIssueManager.OPEN_QUALITY_CHECKER_ADVICE_GROUP
                  + TEXT_SEPARATOR
                  + createDate
                  + TEXT_SEPARATOR
                  + MASTER_BRANCH_NAME);
    }

    assertThat(adviceFields.getProject().getId()).isEqualTo(JIRA_PROJECT_ID);
    assertThat(adviceFields.getIssueType().getId()).isEqualTo(ADVICE_GROUP_ISSUE_TYPE_ID);
  }

  private void assertAdviceIssue(
      final CreateIssueRequest createIssueRequest, final String parentId) {
    final BasicIssueFields adviceFields = createIssueRequest.getFields();

    assertThat(adviceFields.getSummary())
        .isEqualTo(JiraIssueManager.OPEN_QUALITY_CHECKER_ADVICE + ADVICE);
    assertThat(adviceFields.getProject().getId()).isEqualTo(JIRA_PROJECT_ID);
    assertThat(adviceFields.getParent().getId()).isEqualTo(parentId);
    assertThat(adviceFields.getIssueType().getId()).isEqualTo(ADVICE_ISSUE_TYPE_ID);
    assertThat(createIssueRequest.getFields().getDescription().getContent().get(0))
        .isInstanceOf(ParagraphNode.class);
    assertThat(createIssueRequest.getFields().getDescription().getContent().get(0).getType())
        .isEqualTo("paragraph");

    final ParagraphNode paragraphNode =
        (ParagraphNode) createIssueRequest.getFields().getDescription().getContent().get(0);

    final TextNode adviceUrlText = (TextNode) paragraphNode.getContent().get(0);
    assertThat(adviceUrlText).isInstanceOf(TextNode.class);
    assertThat(adviceUrlText.getType()).isEqualTo(TEXT_TYPE);
    assertThat(adviceUrlText.getText()).isEqualTo("Advice url: ");

    final TextNode adviceUrl = (TextNode) paragraphNode.getContent().get(1);
    final ContentMark contentMark = adviceUrl.getMarks().get(0);
    final Attributes attributes = contentMark.getAttrs();
    assertThat(adviceUrl).isInstanceOf(TextNode.class);
    assertThat(adviceUrl.getType()).isEqualTo(TEXT_TYPE);
    assertThat(adviceUrl.getText()).isEqualTo("https://open-quality-checker.com/....\n");
    assertThat(contentMark.getType()).isEqualTo("link");
    assertThat(attributes.getHref()).isEqualTo("https://open-quality-checker.com/....");

    final TextNode reasonText = (TextNode) paragraphNode.getContent().get(2);
    assertThat(reasonText).isInstanceOf(TextNode.class);
    assertThat(reasonText.getType()).isEqualTo(TEXT_TYPE);
    assertThat(reasonText.getText()).contains("This coding issue was created 15 day(s) earlier");
    assertThat(reasonText.getText()).contains("This is a Blocker-level warning");
    assertThat(reasonText.getText()).contains("Factor based on similar advice assessments");
  }

  private void assertAdviceIssueWithoutAdviceUrl(
      final CreateIssueRequest createIssueRequest, final String parentId) {
    final BasicIssueFields adviceFields = createIssueRequest.getFields();

    assertThat(adviceFields.getSummary())
        .isEqualTo(JiraIssueManager.OPEN_QUALITY_CHECKER_ADVICE + ADVICE);
    assertThat(adviceFields.getProject().getId()).isEqualTo(JIRA_PROJECT_ID);
    assertThat(adviceFields.getParent().getId()).isEqualTo(parentId);
    assertThat(adviceFields.getIssueType().getId()).isEqualTo(ADVICE_ISSUE_TYPE_ID);
    assertThat(createIssueRequest.getFields().getDescription().getContent().get(0))
        .isInstanceOf(ParagraphNode.class);
    assertThat(createIssueRequest.getFields().getDescription().getContent().get(0).getType())
        .isEqualTo("paragraph");

    final ParagraphNode paragraphNode =
        (ParagraphNode) createIssueRequest.getFields().getDescription().getContent().get(0);
    assertThat(paragraphNode.getContent().size()).isEqualTo(1);

    final TextNode reasonText = (TextNode) paragraphNode.getContent().get(0);
    assertThat(reasonText).isInstanceOf(TextNode.class);
    assertThat(reasonText.getType()).isEqualTo(TEXT_TYPE);
    assertThat(reasonText.getText()).contains("This coding issue was created 15 day(s) earlier");
    assertThat(reasonText.getText()).contains("This is a Blocker-level warning");
    assertThat(reasonText.getText()).contains("Factor based on similar advice assessments");
  }

  private void assertResolveAdviceIssue(final CommentRequest commentRequest) {
    final RootDocumentNode rootDocumentNode = commentRequest.getBody();
    final ParagraphNode paragraphNode = (ParagraphNode) rootDocumentNode.getContent().get(0);
    final TextNode labelNode = (TextNode) paragraphNode.getContent().get(0);
    final TextNode linkNode = (TextNode) paragraphNode.getContent().get(1);
    final ContentMark contentMark = linkNode.getMarks().get(0);
    final Attributes attributes = contentMark.getAttrs();

    assertThat(labelNode.getText()).isEqualTo("Advice resolved by this ");
    assertThat(linkNode.getText()).isEqualTo("commit");
    assertThat(contentMark.getType()).isEqualTo("link");
    assertThat(attributes.getHref()).isEqualTo("https://github.com/....");
    assertThat(attributes.getTitle()).isEqualTo("Commit URL");
  }

  private void assertResolveAdviceIssueNoCommitUrl(final CommentRequest commentRequest) {
    final RootDocumentNode rootDocumentNode = commentRequest.getBody();
    final ParagraphNode paragraphNode = (ParagraphNode) rootDocumentNode.getContent().get(0);
    final TextNode labelNode = (TextNode) paragraphNode.getContent().get(0);

    assertThat(labelNode.getText()).isEqualTo("Advice resolved. Commit URL is not available.");
  }

  private void assertSavedAdvice(
      final ArgumentCaptor<Advice> saveAdviceCaptor, final String parentId) {
    final Advice savedAdvice = saveAdviceCaptor.getValue();
    assertThat(savedAdvice.getJiraIssueId()).isEqualTo(ADVICE_ISSUE_ID);
    assertThat(savedAdvice.getAdviceId()).isEqualTo(ADVICE_ID);
    assertThat(savedAdvice.getGroup().getOpenQualityCheckerProjectId())
        .isEqualTo(OPEN_QUALITY_CHECKER_PROJECT_ID);
    assertThat(savedAdvice.getGroup().getJiraProjectId()).isEqualTo(JIRA_PROJECT_ID);
    assertThat(savedAdvice.getGroup().getJiraIssueId()).isEqualTo(parentId);
    assertThat(savedAdvice.getGroup().getBranchName()).isEqualTo(MASTER_BRANCH_NAME);
  }

  private void assertSavedAdviceGroup(
      final ArgumentCaptor<AdviceGroup> saveAdviceGroupCaptor, final String issueId) {
    final AdviceGroup savedAdviceGroup = saveAdviceGroupCaptor.getValue();
    assertThat(savedAdviceGroup.getJiraProjectId()).isEqualTo(JIRA_PROJECT_ID);
    assertThat(savedAdviceGroup.getJiraIssueId()).isEqualTo(issueId);
    assertThat(savedAdviceGroup.getBranchName()).isEqualTo(MASTER_BRANCH_NAME);
    assertThat(savedAdviceGroup.getOpenQualityCheckerProjectId())
        .isEqualTo(OPEN_QUALITY_CHECKER_PROJECT_ID);
    if (StringUtils.isNotBlank(savedAdviceGroup.getContributor())) {
      assertThat(savedAdviceGroup.getContributor()).isEqualTo(CONTRIBUTOR);
    }
  }

  private void mockCreateAdviceIssue() {
    when(jiraIssueApiClient.createIssue(any(AtlassianHost.class), any(CreateIssueRequest.class)))
        .thenReturn(ADVICE_ISSUE_ID);
  }

  private void mockCreateIssues(final String adviceGroupIssueId) {
    when(jiraIssueApiClient.createIssue(any(AtlassianHost.class), any(CreateIssueRequest.class)))
        .thenReturn(adviceGroupIssueId, ADVICE_ISSUE_ID);
  }

  private void mockAdviceGroup(final boolean withContributor) {
    final AdviceGroup adviceGroup = new AdviceGroup();
    adviceGroup.setJiraProjectId(JIRA_PROJECT_ID);
    adviceGroup.setJiraIssueId(ADVICE_GROUP_ISSUE_ID);
    adviceGroup.setOpenQualityCheckerProjectId(OPEN_QUALITY_CHECKER_PROJECT_ID);
    adviceGroup.setBranchName(MASTER_BRANCH_NAME);
    if (withContributor) {
      adviceGroup.setContributor(CONTRIBUTOR);
    }

    final AtlassianHost atlassianHost = mockAtlassianHost();

    mockGetIssue(atlassianHost, ADVICE_GROUP_ISSUE_ID, JiraIssueStatus.OPEN);

    when(adviceGroupRepository
            .findTopByOpenQualityCheckerProjectIdAndBranchNameAndContributorOrderByCreatedAtDesc(
                eq(OPEN_QUALITY_CHECKER_PROJECT_ID),
                eq(MASTER_BRANCH_NAME),
                withContributor ? eq(CONTRIBUTOR) : eq(null)))
        .thenReturn(Optional.of(adviceGroup));
  }

  private void assertIssueTransition(
      final AtlassianHost atlassianHost, final JiraIssueStatus targetStatus) {
    final IdentifiedJiraObject transition = new IdentifiedJiraObject();
    transition.setId(TRANSITION_ID_PREFIX + targetStatus);

    final PerformTransitionRequest transitionRequest = new PerformTransitionRequest();
    transitionRequest.setTransition(transition);

    verify(jiraIssueApiClient, timeout(ASYNC_CALL_TIMEOUT))
        .performTransition(eq(atlassianHost), eq(ADVICE_ISSUE_ID), eq(transitionRequest));
  }

  private void mockGetIssue(
      final AtlassianHost atlassianHost,
      final String issueId,
      final JiraIssueStatus jiraIssueStatus) {

    final IdentifiedJiraObject status = new IdentifiedJiraObject();
    status.setName(jiraIssueStatus.name());

    final IssueFields issueFields = new IssueFields();
    issueFields.setStatus(status);

    final IssueBean adviceIssueBean = new IssueBean();
    adviceIssueBean.setFields(issueFields);
    adviceIssueBean.setTransitions(new ArrayList<>());

    Arrays.stream(jiraIssueStatus.getPossibleTargets())
        .forEach(target -> adviceIssueBean.getTransitions().add(createTransition(target)));

    when(jiraIssueApiClient.getIssue(eq(atlassianHost), eq(issueId))).thenReturn(adviceIssueBean);
  }

  private IssueTransition createTransition(final JiraIssueStatus jiraIssueStatus) {
    final IdentifiedJiraObject openStatusTarget = new IdentifiedJiraObject();
    openStatusTarget.setName(jiraIssueStatus.name());
    final IssueTransition openTransition = new IssueTransition();
    openTransition.setId(TRANSITION_ID_PREFIX + jiraIssueStatus.name());
    openTransition.setTarget(openStatusTarget);
    return openTransition;
  }

  private void mockAdvice() {
    final Advice advice = new Advice();
    advice.setAdviceId(ADVICE_ID);
    advice.setJiraIssueId(ADVICE_ISSUE_ID);

    when(adviceRepository.findByAdviceIdAndGroupOpenQualityCheckerProjectId(
            ADVICE_ID, OPEN_QUALITY_CHECKER_PROJECT_ID))
        .thenReturn(Optional.of(advice));
  }

  private AtlassianHost mockAtlassianHost() {

    final AtlassianHost atlassianHost =
        new AtlassianHostBuilder().withBaseUrl(AtlassianUtil.BASE_URL).build();

    when(atlassianHostRepository.findFirstByBaseUrlOrderByLastModifiedDateDesc(
            AtlassianUtil.BASE_URL))
        .thenReturn(Optional.of(atlassianHost));

    return atlassianHost;
  }

  private void mockIssueType() {
    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(AtlassianUtil.BASE_URL);
    issueType.setAdviceIssueTypeId(ADVICE_ISSUE_TYPE_ID);
    issueType.setAdviceGroupIssueTypeId(ADVICE_GROUP_ISSUE_TYPE_ID);

    when(issueTypeRepository.findByAtlassianHostUrl(AtlassianUtil.BASE_URL))
        .thenReturn(Optional.of(issueType));
  }

  private void mockProjectMapping() {
    final AccountMapping accountMapping = new AccountMapping();
    accountMapping.setAtlassianHostUrl(AtlassianUtil.BASE_URL);

    final ProjectMapping projectMapping = new ProjectMapping();
    projectMapping.setAccountMapping(accountMapping);
    projectMapping.setJiraProjectId(JIRA_PROJECT_ID);

    when(projectMappingRepository.findByOpenQualityCheckerProjectId(
            OPEN_QUALITY_CHECKER_PROJECT_ID))
        .thenReturn(Optional.of(projectMapping));
  }

  private String createDate() {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.ENGLISH);
    return dateFormat.format(new Date());
  }
}
