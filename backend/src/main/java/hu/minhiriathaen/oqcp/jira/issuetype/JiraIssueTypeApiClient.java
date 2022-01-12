package hu.minhiriathaen.oqcp.jira.issuetype;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.transfer.CreateIssueTypeRequest;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeDetails;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import java.util.List;

public interface JiraIssueTypeApiClient {

  List<IssueTypeDetails> getAllIssueTypes(AtlassianHost atlassianHost);

  IdentifiedJiraObject createIssueType(AtlassianHost atlassianHost, CreateIssueTypeRequest request);

  void addIssueTypesToIssueTypeSchemeForProject(
      AtlassianHost atlassianHost, String jiraProjectId, IssueType issueType);
}
