package hu.minhiriathaen.oqcp.jira.workflow;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;

public interface JiraWorkflowApiClient {

  void assignDefaultWorkflowToIssueTypesAtProject(
      AtlassianHost atlassianHost, String jiraProjectId, IssueType issueType);
}
