package hu.minhiriathaen.oqcp.jira.workflow;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.JiraRestClient;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import hu.minhiriathaen.oqcp.test.util.AtlassianHostBuilder;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ResourceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@SpringBootTest
class RestJiraWorkflowApiClientTest extends ApplicationIntegrationTestBase {

  private static final String JIRA_PROJECT_ID = "JIRA_PROJECT_ID";
  public static final int WORKFLOW_SCHEME_ID = 1001;
  public static final String ADVICE_GROUP_ISSUE_TYPE_ID = "10044";
  public static final String ADVICE_ISSUE_TYPE_ID = "10045";

  @Autowired private transient RestJiraWorkflowApiClient restJiraWorkflowApiClient;

  @Autowired private transient JiraRestClient jiraRestClient;

  private transient MockRestServiceServer mockServer;

  private transient AtlassianHost atlassianHost;

  @Value("classpath:jira-get-workflow-scheme-for-project-already-assigned-response.json")
  private transient Resource workflowSchemeAlreadyAssignedResponse;

  @Value("classpath:jira-get-workflow-scheme-for-project-not-assigned-response.json")
  private transient Resource workflowSchemeNotAssignedResponse;

  @Value("classpath:jira-set-issue-types-for-workflow-expected-request.json")
  private transient Resource setIssueTypeRequest;

  @Value("classpath:jira-set-issue-types-for-workflow-response.json")
  private transient Resource setIssueTypeResponse;

  @BeforeEach
  public void setUp() {
    atlassianHost = new AtlassianHostBuilder().withBaseUrl(AtlassianUtil.BASE_URL).build();
    mockServer = MockRestServiceServer.createServer(jiraRestClient.getRestTemplate(atlassianHost));
  }

  @Test
  public void testWithWorkflowAssignIsRequired() {
    mockGetWorkflowSchemeForProject(workflowSchemeNotAssignedResponse);

    mockServer
        .expect(
            requestToUriTemplate(
                AtlassianUtil.BASE_URL
                    + RestJiraWorkflowApiClient.JIRA_SET_ISSUE_TYPES_FOR_WORKFLOW_IN_SCHEME_URL,
                WORKFLOW_SCHEME_ID))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(ResourceUtil.asString(setIssueTypeRequest)))
        .andRespond(
            withSuccess(ResourceUtil.asString(setIssueTypeResponse), MediaType.APPLICATION_JSON));

    final IssueType issueType = new IssueType();
    issueType.setAdviceGroupIssueTypeId(ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(ADVICE_ISSUE_TYPE_ID);

    restJiraWorkflowApiClient.assignDefaultWorkflowToIssueTypesAtProject(
        atlassianHost, JIRA_PROJECT_ID, issueType);

    mockServer.verify();
  }

  @Test
  public void testWithWorkflowAssignIsNotRequired() {
    mockGetWorkflowSchemeForProject(workflowSchemeAlreadyAssignedResponse);

    final IssueType issueType = new IssueType();
    issueType.setAdviceGroupIssueTypeId(ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(ADVICE_ISSUE_TYPE_ID);

    restJiraWorkflowApiClient.assignDefaultWorkflowToIssueTypesAtProject(
        atlassianHost, JIRA_PROJECT_ID, issueType);

    mockServer.verify();
  }

  private void mockGetWorkflowSchemeForProject(final Resource workflowSchemeNotAssignedResponse) {
    mockServer
        .expect(
            requestToUriTemplate(
                AtlassianUtil.BASE_URL
                    + RestJiraWorkflowApiClient.JIRA_GET_WORKFLOW_SCHEME_FOR_PROJECT_URL,
                JIRA_PROJECT_ID))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                ResourceUtil.asString(workflowSchemeNotAssignedResponse),
                MediaType.APPLICATION_JSON));
  }
}
