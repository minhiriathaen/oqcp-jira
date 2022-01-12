package hu.minhiriathaen.oqcp.jira.issuetype;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.jira.JiraRestClient;
import hu.minhiriathaen.oqcp.jira.transfer.AddIssueTypeRequest;
import hu.minhiriathaen.oqcp.jira.transfer.CreateIssueTypeRequest;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeDetails;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeSchemeMapping;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeSchemeProjects;
import hu.minhiriathaen.oqcp.jira.transfer.PageBeanIssueTypeSchemeMapping;
import hu.minhiriathaen.oqcp.jira.transfer.PageBeanIssueTypeSchemeProjects;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import hu.minhiriathaen.oqcp.util.HostUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestJiraIssueTypeApiClient implements JiraIssueTypeApiClient {

  public static final String JIRA_ISSUE_TYPES_URL = "/rest/api/3/issuetype";

  public static final String JIRA_GET_ISSUE_TYPE_SCHEME_FOR_PROJECT_URL =
      "/rest/api/3/issuetypescheme/project?projectId={projectId}";

  public static final String JIRA_GET_ISSUE_TYPES_FOR_SCHEME_URL =
      "/rest/api/3/issuetypescheme/mapping?issueTypeSchemeId={issueTypeSchemeId}&startAt={startAt}";

  public static final String JIRA_ADD_ISSUE_TYPES_TO_SCHEME_URL =
      "/rest/api/3/issuetypescheme/{issueTypeSchemeId}/issuetype";

  private final ContextHelper contextHelper;

  private final JiraRestClient jiraRestClient;

  @Override
  public List<IssueTypeDetails> getAllIssueTypes(final AtlassianHost atlassianHost) {

    HostUtil.checkAtlassianHost(atlassianHost);

    log.info(
        "[{}] Getting all issue types with host '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl());

    try {
      final ResponseEntity<IssueTypeDetails[]> response =
          jiraRestClient.authenticatedGet(
              atlassianHost, JIRA_ISSUE_TYPES_URL, IssueTypeDetails[].class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Jira getAllIssueTypes: response status is not successful, repsonse: {}",
            contextHelper.getUserIdForLog(),
            response);
        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

      return Arrays.asList(response.getBody());

    } catch (final ServiceError serviceError) {
      throw serviceError;
    } catch (final Exception exception) {
      log.error(exception.getClass().getName(), exception);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
    }
  }

  @Override
  public IdentifiedJiraObject createIssueType(
      final AtlassianHost atlassianHost, final CreateIssueTypeRequest request) {

    HostUtil.checkAtlassianHost(atlassianHost);

    Objects.requireNonNull(request, "Request parameter can not be null");

    log.info(
        "[{}] Creating an issue type with host '{}' and issue type '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        request.getName());

    try {
      final ResponseEntity<IdentifiedJiraObject> response =
          jiraRestClient.authenticatedPost(
              atlassianHost, JIRA_ISSUE_TYPES_URL, request, IdentifiedJiraObject.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Jira createIssueType: response status is not successful, response: {}",
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
  public void addIssueTypesToIssueTypeSchemeForProject(
      final AtlassianHost atlassianHost, final String jiraProjectId, final IssueType issueType) {

    HostUtil.checkAtlassianHost(atlassianHost);

    if (StringUtils.isBlank(jiraProjectId)) {
      throw new IllegalArgumentException(
          "Jira project identifier parameter can not be null or empty");
    }

    Objects.requireNonNull(issueType, "IssueType parameter can not be null");

    log.info(
        "[{}] Adding issue types to issue type scheme with host '{}' and projectId '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        jiraProjectId);

    try {
      final String issueTypeSchemeId = getIssueTypeSchemeIdForProject(atlassianHost, jiraProjectId);

      final Set<String> issueTypesIds = getIssueTypeIdsForScheme(atlassianHost, issueTypeSchemeId);

      final AddIssueTypeRequest addIssueTypeRequest = new AddIssueTypeRequest();
      if (!issueTypesIds.contains(issueType.getAdviceGroupIssueTypeId())) {
        addIssueTypeRequest.add(issueType.getAdviceGroupIssueTypeId());
      }

      if (!issueTypesIds.contains(issueType.getAdviceIssueTypeId())) {
        addIssueTypeRequest.add(issueType.getAdviceIssueTypeId());
      }

      if (addIssueTypeRequest.getIssueTypeIds().isEmpty()) {
        log.info(
            "[{}] The issue types are already added to the scheme, issueTypeSchemeId: {}",
            contextHelper.getUserIdForLog(),
            issueTypeSchemeId);
      } else {
        final ResponseEntity<Object> response =
            jiraRestClient.authenticatedPut(
                atlassianHost,
                JIRA_ADD_ISSUE_TYPES_TO_SCHEME_URL,
                addIssueTypeRequest,
                Object.class,
                issueTypeSchemeId);

        if (!response.getStatusCode().is2xxSuccessful()) {
          log.error(
              "[{}] Jira addIssueTypes: response status is not successful, response: {}",
              contextHelper.getUserIdForLog(),
              response);
          throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
        }
      }
    } catch (final ServiceError serviceError) {
      throw serviceError;
    } catch (final Exception exception) {
      log.error(exception.getClass().getName(), exception);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
    }
  }

  private String getIssueTypeSchemeIdForProject(
      final AtlassianHost atlassianHost, final String jiraProjectId) {

    log.info(
        "[{}] Getting issue type scheme identifier for project, with host '{}' and projectId '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        jiraProjectId);

    // Assuming that a project has one scheme, don't start paginated process
    final ResponseEntity<PageBeanIssueTypeSchemeProjects> response =
        jiraRestClient.authenticatedGet(
            atlassianHost,
            JIRA_GET_ISSUE_TYPE_SCHEME_FOR_PROJECT_URL,
            PageBeanIssueTypeSchemeProjects.class,
            jiraProjectId);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error(
          "[{}] Jira getIssueTypeSchemeId: response status is not successful, response: {}",
          contextHelper.getUserIdForLog(),
          response);
      throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
    }

    final List<IssueTypeSchemeProjects> issueTypeSchemes = response.getBody().getValues();

    if (issueTypeSchemes == null
        || issueTypeSchemes.size() != 1
        || null == issueTypeSchemes.get(0).getIssueTypeScheme()
        || StringUtils.isBlank(issueTypeSchemes.get(0).getIssueTypeScheme().getId())) {
      throw new IllegalStateException("Jira project's scheme cannot be identified");
    }

    return issueTypeSchemes.get(0).getIssueTypeScheme().getId();
  }

  private Set<String> getIssueTypeIdsForScheme(
      final AtlassianHost atlassianHost, final String issueTypeSchemeId) {

    log.info(
        "[{}] Getting issue type identifiers  with host '{}' and issueTypeSchemeId '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl(),
        issueTypeSchemeId);

    boolean isLast = false;
    final Set<String> issueTypeIds = new HashSet<>();
    int startAt = 0;

    while (!isLast) {
      final ResponseEntity<PageBeanIssueTypeSchemeMapping> response =
          jiraRestClient.authenticatedGet(
              atlassianHost,
              JIRA_GET_ISSUE_TYPES_FOR_SCHEME_URL,
              PageBeanIssueTypeSchemeMapping.class,
              issueTypeSchemeId,
              startAt);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Jira getIssueTypeIdsForScheme: response status is not successful, response: {}",
            contextHelper.getUserIdForLog(),
            response);
        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

      final List<IssueTypeSchemeMapping> issueTypeSchemeMappings = response.getBody().getValues();
      if (issueTypeSchemeMappings == null) {
        throw new IllegalStateException("Issue types in Jira issue type scheme are not available");
      }

      final List<String> pageIssueTypeIds =
          issueTypeSchemeMappings.stream()
              .map(IssueTypeSchemeMapping::getIssueTypeId)
              .collect(Collectors.toList());

      issueTypeIds.addAll(pageIssueTypeIds);

      isLast = response.getBody().getIsLast();
      if (!isLast) {
        startAt += response.getBody().getTotal();
      }
    }
    return issueTypeIds;
  }
}
