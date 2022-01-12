package hu.minhiriathaen.oqcp.jira.issue;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
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
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueTransition;
import hu.minhiriathaen.oqcp.persistence.entity.AdviceGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JiraIssueManager {

  public static final String OPEN_QUALITY_CHECKER_ADVICE_GROUP = "OpenQualityChecker Advice group ";

  public static final String OPEN_QUALITY_CHECKER_ADVICE = "OpenQualityChecker Advice ";

  private final JiraIssueApiClient jiraIssueApiClient;

  public Optional<IssueTransition> findTransitionForTargetStatus(
      final IssueBean jiraIssue, final JiraIssueStatus targetStatus) {

    if (null == jiraIssue || null == jiraIssue.getTransitions()) {
      log.error("[{}] Unable to get transitions for issue", jiraIssue.getId());
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCode.JIRA_CLOUD_ERROR,
          new NoSuchElementException("Issue status"));
    }

    return jiraIssue.getTransitions().stream()
        .filter(
            transition ->
                targetStatus.equals(JiraIssueStatus.fromString(transition.getTarget().getName())))
        .findFirst();
  }

  public void performTransition(
      final Optional<IssueTransition> optionalTransition,
      final JiraIssueStatus issueStatus,
      final String issueId,
      final AtlassianHost atlassianHost) {

    if (optionalTransition.isPresent()) {
      log.info("[{}] Performing issue transition: {}", issueId, optionalTransition.get());

      final IdentifiedJiraObject transition = new IdentifiedJiraObject();
      transition.setId(optionalTransition.get().getId());

      final PerformTransitionRequest transitionRequest = new PerformTransitionRequest();
      transitionRequest.setTransition(transition);

      jiraIssueApiClient.performTransition(atlassianHost, issueId, transitionRequest);
    } else {
      log.warn("[{}] Unable to find transition for {} status", issueId, issueStatus);
    }
  }

  public String createAdviceGroupIssue(
      final AtlassianHost atlassianHost,
      final String adviceId,
      final String branchName,
      final String jiraProjectId,
      final String adviceGroupIssueTypeId,
      final String contributor) {

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.ENGLISH);
    dateFormat.format(new Date());

    log.info("[{}] Creating advice group issue", adviceId);

    final TextNode branchNameText = new TextNode();
    branchNameText.setText("Branch name: " + branchName + ", ");

    final ParagraphNode paragraphNode = new ParagraphNode();
    paragraphNode.getContent().add(branchNameText);

    if (StringUtils.isNotBlank(contributor)) {
      final TextNode contributorText = new TextNode();
      contributorText.setText("Contributor: " + contributor);
      paragraphNode.getContent().add(contributorText);
    }

    final RootDocumentNode description = new RootDocumentNode();
    description.setContent(new ArrayList<>());
    description.getContent().add(paragraphNode);

    final BasicIssueFields issueFields = new BasicIssueFields();
    issueFields.setSummary(
        OPEN_QUALITY_CHECKER_ADVICE_GROUP
            + " - "
            + dateFormat.format(new Date())
            + " - "
            + branchName
            + (StringUtils.isBlank(contributor) ? "" : " - " + contributor));
    issueFields.setProject(new IdentifiedJiraObject(jiraProjectId));
    issueFields.setIssueType(new IdentifiedJiraObject(adviceGroupIssueTypeId));
    issueFields.setDescription(description);

    final CreateIssueRequest createIssueRequest = new CreateIssueRequest();
    createIssueRequest.setFields(issueFields);

    log.info(
        "[{}] Sending advice group issue create request to Jira: '{}', request: {} ",
        adviceId,
        atlassianHost,
        createIssueRequest);

    return jiraIssueApiClient.createIssue(atlassianHost, createIssueRequest);
  }

  public String createAdviceIssue(
      final AtlassianHost atlassianHost,
      final String reason,
      final String adviceId,
      final String adviceUrl,
      final String adviceSummary,
      final AdviceGroup adviceGroup,
      final String adviceIssueTypeId) {

    log.info("[{}] Creating advice issue", adviceId);

    final BasicIssueFields issueFields = new BasicIssueFields();
    issueFields.setSummary(OPEN_QUALITY_CHECKER_ADVICE + adviceSummary);
    issueFields.setProject(new IdentifiedJiraObject(adviceGroup.getJiraProjectId()));
    issueFields.setParent(new IdentifiedJiraObject(adviceGroup.getJiraIssueId()));
    issueFields.setIssueType(new IdentifiedJiraObject(adviceIssueTypeId));

    final TextNode adviceUrlText = new TextNode();
    adviceUrlText.setText("Advice url: ");

    final Attributes attributes = new Attributes();
    attributes.setHref(adviceUrl);
    final ContentMark contentMark = new ContentMark();
    contentMark.setType("link");
    contentMark.setAttrs(attributes);
    final TextNode adviceLink = new TextNode();
    adviceLink.setText(adviceUrl + "\n");
    adviceLink.setMarks(Arrays.asList(contentMark));

    final TextNode reasonText = new TextNode();
    reasonText.setText(reason);

    final ParagraphNode paragraphNode = new ParagraphNode();
    if (StringUtils.isNotBlank(adviceUrl)) {
      paragraphNode.getContent().add(adviceUrlText);
      paragraphNode.getContent().add(adviceLink);
    }
    paragraphNode.getContent().add(reasonText);

    final RootDocumentNode rootDocumentNode = new RootDocumentNode();
    rootDocumentNode.setContent(new ArrayList<>());
    rootDocumentNode.getContent().add(paragraphNode);
    issueFields.setDescription(rootDocumentNode);

    final CreateIssueRequest createIssueRequest = new CreateIssueRequest();
    createIssueRequest.setFields(issueFields);

    log.info(
        "[{}] Sending advice issue create request to Jira: '{}', request: {} ",
        adviceId,
        atlassianHost,
        createIssueRequest);

    return jiraIssueApiClient.createIssue(atlassianHost, createIssueRequest);
  }

  public IssueBean getJiraIssue(
      final AtlassianHost atlassianHost, final String adviceId, final String jiraIssueId) {
    final IssueBean adviceJiraIssue = jiraIssueApiClient.getIssue(atlassianHost, jiraIssueId);

    if (null == adviceJiraIssue
        || null == adviceJiraIssue.getFields()
        || null == adviceJiraIssue.getFields().getStatus()) {

      log.error("[{}] Unable to get status for issue: {}", adviceId, jiraIssueId);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCode.JIRA_CLOUD_ERROR,
          new NoSuchElementException("Issue status"));
    }

    return adviceJiraIssue;
  }

  public void addResolutionComment(
      final AtlassianHost atlassianHost, final String jiraIssueId, final String commitUrl) {

    final TextNode resolutionText = new TextNode();
    final ParagraphNode paragraphNode = new ParagraphNode();

    if (StringUtils.isBlank(commitUrl)) {
      resolutionText.setText("Advice resolved. Commit URL is not available.");
      paragraphNode.setContent(Collections.singletonList(resolutionText));
    } else {
      resolutionText.setText("Advice resolved by this ");

      final Attributes attributes = new Attributes();
      attributes.setHref(commitUrl);
      attributes.setTitle("Commit URL");

      final ContentMark contentMark = new ContentMark();
      contentMark.setType("link");
      contentMark.setAttrs(attributes);

      final TextNode linkNode = new TextNode();
      linkNode.setText("commit");
      linkNode.setMarks(Collections.singletonList(contentMark));

      paragraphNode.setContent(Arrays.asList(resolutionText, linkNode));
    }

    final RootDocumentNode rootDocumentNode = new RootDocumentNode();
    rootDocumentNode.setContent(Collections.singletonList(paragraphNode));

    final CommentRequest commentRequest = new CommentRequest();
    commentRequest.setBody(rootDocumentNode);

    jiraIssueApiClient.addComment(atlassianHost, jiraIssueId, commentRequest);
  }
}
