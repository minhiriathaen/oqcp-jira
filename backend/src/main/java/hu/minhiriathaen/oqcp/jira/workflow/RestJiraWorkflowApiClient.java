package hu.minhiriathaen.oqcp.jira.workflow;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.jira.JiraRestClient;
import hu.minhiriathaen.oqcp.jira.transfer.ContainerOfWorkflowSchemeAssociations;
import hu.minhiriathaen.oqcp.jira.transfer.SetIssueTypesForWorkflowRequest;
import hu.minhiriathaen.oqcp.jira.transfer.WorkflowScheme;
import hu.minhiriathaen.oqcp.jira.transfer.WorkflowSchemeAssociations;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import hu.minhiriathaen.oqcp.util.HostUtil;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestJiraWorkflowApiClient implements JiraWorkflowApiClient {

  public static final String JIRA_GET_WORKFLOW_SCHEME_FOR_PROJECT_URL =
      "/rest/api/3/workflowscheme/project?projectId={projectId}";

  public static final String JIRA_SET_ISSUE_TYPES_FOR_WORKFLOW_IN_SCHEME_URL =
      "/rest/api/3/workflowscheme/{workflowSchemeId}/workflow?workflowName=jira";
  public static final String JIRA_WORKFLOW_NAME = "jira";
  public static final int ALLOWED_ASSOCIATION_NUMBER = 1;

  private final JiraRestClient jiraRestClient;

  private final ContextHelper contextHelper;

  @Override
  public void assignDefaultWorkflowToIssueTypesAtProject(
      final AtlassianHost atlassianHost, final String jiraProjectId, final IssueType issueType) {

    HostUtil.checkAtlassianHost(atlassianHost);

    log.info(
        "[{}] Assigning default workflow to issue types at project '{}'",
        contextHelper.getUserIdForLog(),
        atlassianHost.getBaseUrl());

    try {
      final ContainerOfWorkflowSchemeAssociations workflowSchemeAssociationsContainer =
          getWorkflowSchemeAssociations(atlassianHost, jiraProjectId);

      final WorkflowScheme workflowScheme = getWorkflowScheme(workflowSchemeAssociationsContainer);

      if (isIssueTypesAlreadyInScheme(issueType, workflowSchemeAssociationsContainer)) {

        log.info(
            "[{}] Issue types ({}) are already assigned to workflow: {} - {}",
            contextHelper.getUserIdForLog(),
            issueType,
            workflowScheme.getId(),
            workflowScheme.getName());

        return;
      }

      final ResponseEntity<WorkflowScheme> updatedWorkflowSchemeResponse =
          addIssueTypesToWorkflowScheme(atlassianHost, issueType, workflowScheme);

      if (!updatedWorkflowSchemeResponse.getStatusCode().is2xxSuccessful()) {
        log.error(
            "[{}] Set issue types for workflow in scheme: response status is not successful: {}",
            contextHelper.getUserIdForLog(),
            updatedWorkflowSchemeResponse);
        throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
      }

      log.info(
          "[{}] Workflow scheme updated: {}",
          contextHelper.getUserIdForLog(),
          updatedWorkflowSchemeResponse.getBody());

    } catch (final ServiceError serviceError) {
      throw serviceError;
    } catch (final Exception exception) {
      log.error(exception.getClass().getName(), exception);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR, exception);
    }
  }

  private ResponseEntity<WorkflowScheme> addIssueTypesToWorkflowScheme(
      final AtlassianHost atlassianHost,
      final IssueType issueType,
      final WorkflowScheme workflowScheme) {

    final SetIssueTypesForWorkflowRequest setIssueTypesForWorkflowRequest =
        new SetIssueTypesForWorkflowRequest();
    setIssueTypesForWorkflowRequest.setIssueTypes(new ArrayList<>());
    setIssueTypesForWorkflowRequest.getIssueTypes().add(issueType.getAdviceGroupIssueTypeId());
    setIssueTypesForWorkflowRequest.getIssueTypes().add(issueType.getAdviceIssueTypeId());
    setIssueTypesForWorkflowRequest.setUpdateDraftIfNeeded(true);
    setIssueTypesForWorkflowRequest.setWorkflow(JIRA_WORKFLOW_NAME);

    return jiraRestClient.authenticatedPut(
        atlassianHost,
        JIRA_SET_ISSUE_TYPES_FOR_WORKFLOW_IN_SCHEME_URL,
        setIssueTypesForWorkflowRequest,
        WorkflowScheme.class,
        workflowScheme.getId());
  }

  private ContainerOfWorkflowSchemeAssociations getWorkflowSchemeAssociations(
      final AtlassianHost atlassianHost, final String jiraProjectId) {

    final ResponseEntity<ContainerOfWorkflowSchemeAssociations> response =
        jiraRestClient.authenticatedGet(
            atlassianHost,
            JIRA_GET_WORKFLOW_SCHEME_FOR_PROJECT_URL,
            ContainerOfWorkflowSchemeAssociations.class,
            jiraProjectId);

    if (!response.getStatusCode().is2xxSuccessful() || null == response.getBody()) {
      log.error(
          "[{}] Get workflow scheme associations: status is not successful or body is null: {}",
          contextHelper.getUserIdForLog(),
          response);
      throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
    }
    return response.getBody();
  }

  private boolean isIssueTypesAlreadyInScheme(
      final IssueType issueType,
      final ContainerOfWorkflowSchemeAssociations workflowSchemeAssociationsContainer) {

    if (workflowSchemeAssociationsContainer.getValues().isEmpty()) {
      log.error(
          "[{}] Get workflow scheme project associations: association list is empty",
          contextHelper.getUserIdForLog());
      throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
    }

    if (workflowSchemeAssociationsContainer.getValues().size() > ALLOWED_ASSOCIATION_NUMBER) {
      log.warn(
          "[{}] More than one workflow scheme associated: {}",
          contextHelper.getUserIdForLog(),
          workflowSchemeAssociationsContainer.getValues());
    }

    final WorkflowSchemeAssociations schemeAssociations =
        workflowSchemeAssociationsContainer.getValues().get(0);

    final Map<String, String> issueTypeMappings =
        schemeAssociations.getWorkflowScheme().getIssueTypeMappings();

    final boolean adviceGroupIssueTypeAssigned =
        issueTypeMappings.containsKey(issueType.getAdviceGroupIssueTypeId())
            && issueTypeMappings
                .get(issueType.getAdviceGroupIssueTypeId())
                .equals(JIRA_WORKFLOW_NAME);

    final boolean adviceIssueTypeAssigned =
        issueTypeMappings.containsKey(issueType.getAdviceIssueTypeId())
            && issueTypeMappings.get(issueType.getAdviceIssueTypeId()).equals(JIRA_WORKFLOW_NAME);

    return adviceGroupIssueTypeAssigned && adviceIssueTypeAssigned;
  }

  private WorkflowScheme getWorkflowScheme(
      final ContainerOfWorkflowSchemeAssociations workflowSchemeAssociationsContainer) {

    if (null == workflowSchemeAssociationsContainer.getValues()) {
      log.error(
          "[{}] Get workflow scheme project associations: workflow scheme association list is null",
          contextHelper.getUserIdForLog());
      throw new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
    }

    return workflowSchemeAssociationsContainer.getValues().stream()
        .findFirst()
        .orElseThrow(
            () -> {
              log.error(
                  "[{}] Get workflow scheme project associations: no workflow scheme found in: {}",
                  contextHelper.getUserIdForLog(),
                  workflowSchemeAssociationsContainer);
              return new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.JIRA_CLOUD_ERROR);
            })
        .getWorkflowScheme();
  }
}
