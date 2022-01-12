package hu.minhiriathaen.oqcp.jira.issue;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.jira.JiraRestClient;
import hu.minhiriathaen.oqcp.jira.transfer.CommentRequest;
import hu.minhiriathaen.oqcp.jira.transfer.CreateIssueRequest;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.IssueListResponse;
import hu.minhiriathaen.oqcp.jira.transfer.PerformTransitionRequest;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import hu.minhiriathaen.oqcp.util.HostUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestJiraIssueApiClient implements JiraIssueApiClient {

  public static final String JIRA_SEARCH_URL_BASE = "/rest/api/3/search";

  public static final String JIRA_SEARCH_URL_PARAMS =
      "?jql={jql}&startAt={startAt}&maxResults={maxResults}";

  public static final String JIRA_SEARCH_URL = JIRA_SEARCH_URL_BASE + JIRA_SEARCH_URL_PARAMS;

  public static final String JIRA_ISSUE_URL = "/rest/api/3/issue";

  public static final String JIRA_GET_ISSUE_URL =
      "/rest/api/3/issue/{issueIdOrKey}?expand=transitions";

  public static final String JIRA_PERFORM_TRANSITION_URL =
      "/rest/api/3/issue/{issueIdOrKey}/transitions";

  public static final String JIRA_ADD_COMMENT_URL = "/rest/api/3/issue/{issueIdOrKey}/comment";

  public static final String EMPTY_ISSUE_TEXT =
      "Issue identifier parameter can not be null or empty";

  public static final int SEARCH_MAX_RESULTS = 50;

  private final ContextHelper contextHelper;

  private final JiraRestClient jiraRestClient;

  @Override
  public IssueBean getIssue(final AtlassianHost atlassianHost, final String issueId) {

    HostUtil.checkAtlassianHost(atlassianHost);

    if (StringUtils.isBlank(issueId)) {
      throw new IllegalArgumentException(EMPTY_ISSUE_TEXT);
    }

    log.info(
        "[{}] Getting issue with host '{}' and issue id '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        issueId);

    try {
      final ResponseEntity<IssueBean> response =
          jiraRestClient.authenticatedGet(
              atlassianHost, JIRA_GET_ISSUE_URL, IssueBean.class, issueId);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Jira getIssue: response status is not successful, response: {}",
            contextHelper.getUserIdForLog(),
            response);

        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

      return response.getBody();

    } catch (final ServiceError serviceError) {
      throw serviceError;
    } catch (final Exception exception) {
      log.error(exception.getClass().getName(), exception);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
    }
  }

  @Override
  public List<IssueBean> getSubtasksByIssueTypeAndParentIssue(
      final AtlassianHost atlassianHost, final String issueTypeId, final String parentIssueId) {

    HostUtil.checkAtlassianHost(atlassianHost);

    if (StringUtils.isBlank(parentIssueId)) {
      throw new IllegalArgumentException(EMPTY_ISSUE_TEXT);
    }

    log.info(
        "[{}] Getting the statuses of the subtask issues for parent issue with host '{}'"
            + " and issue id '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        parentIssueId);

    int startAt = 0;
    boolean lastPage = false;

    final String jql =
        MessageFormat.format("issuetype={0} AND parent={1}", issueTypeId, parentIssueId);
    final List<IssueBean> issuesForParent = new ArrayList<>();

    while (!lastPage) {

      final ResponseEntity<IssueListResponse> response =
          jiraRestClient.authenticatedGet(
              atlassianHost,
              JIRA_SEARCH_URL,
              IssueListResponse.class,
              jql,
              startAt,
              SEARCH_MAX_RESULTS);

      if (!response.hasBody()) {
        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

      try {
        assert response.getBody() != null;

        issuesForParent.addAll(response.getBody().getIssues());

        startAt += SEARCH_MAX_RESULTS;

        lastPage = startAt >= response.getBody().getTotal();

      } catch (final ServiceError serviceError) {
        throw serviceError;
      } catch (final Exception exception) {
        log.error(exception.getClass().getName(), exception);
        throw new ServiceError(
            HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
      }
    }

    return issuesForParent;
  }

  @Override
  public String createIssue(final AtlassianHost atlassianHost, final CreateIssueRequest request) {

    HostUtil.checkAtlassianHost(atlassianHost);

    Objects.requireNonNull(request, "Request parameter can not be null");

    log.info(
        "[{}] creating issue with host '{}' and project id '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        request.getFields().getProject().getId());

    try {
      final ResponseEntity<IdentifiedJiraObject> response =
          jiraRestClient.authenticatedPost(
              atlassianHost, JIRA_ISSUE_URL, request, IdentifiedJiraObject.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Jira createIssue: response status is not successful, response: {}",
            contextHelper.getUserIdForLog(),
            response);
        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

      final IdentifiedJiraObject createdIssue = response.getBody();

      return createdIssue.getId();
    } catch (final ServiceError serviceError) {
      throw serviceError;
    } catch (final Exception exception) {
      log.error(exception.getClass().getName(), exception);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
    }
  }

  @Override
  public void performTransition(
      final AtlassianHost atlassianHost,
      final String issueId,
      final PerformTransitionRequest request) {

    HostUtil.checkAtlassianHost(atlassianHost);

    if (StringUtils.isBlank(issueId)) {
      throw new IllegalArgumentException(EMPTY_ISSUE_TEXT);
    }

    Objects.requireNonNull(request, "Request parameter can not be null");

    log.info(
        "[{}] Performing an issue transition with host '{}', issue id '{}' and transition id '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        issueId,
        request.getTransition().getId());

    try {
      final ResponseEntity<Object> response =
          jiraRestClient.authenticatedPost(
              atlassianHost, JIRA_PERFORM_TRANSITION_URL, request, Object.class, issueId);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Jira performTransition: response status is not successful, response: {}",
            contextHelper.getUserIdForLog(),
            response);
        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

    } catch (final ServiceError serviceError) {
      throw serviceError;
    } catch (final Exception exception) {
      log.error(exception.getClass().getName(), exception);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
    }
  }

  @Override
  public void addComment(
      final AtlassianHost atlassianHost, final String issueId, final CommentRequest request) {
    HostUtil.checkAtlassianHost(atlassianHost);

    if (StringUtils.isBlank(issueId)) {
      throw new IllegalArgumentException(EMPTY_ISSUE_TEXT);
    }

    Objects.requireNonNull(request, "Request parameter can not be null");

    log.info(
        "[{}] Creating comment with host '{}' and issue id '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        issueId);

    try {
      final ResponseEntity<Object> response =
          jiraRestClient.authenticatedPost(
              atlassianHost, JIRA_ADD_COMMENT_URL, request, Object.class, issueId);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Jira addComment: response status is not successful, response: {}",
            contextHelper.getUserIdForLog(),
            response);
        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

    } catch (final ServiceError serviceError) {
      throw serviceError;
    } catch (final Exception exception) {
      log.error(exception.getClass().getName(), exception);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
    }
  }
}
