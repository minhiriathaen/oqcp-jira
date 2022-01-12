package hu.minhiriathaen.oqcp.jira.issue;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.JiraRestClient;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import hu.minhiriathaen.oqcp.test.util.AtlassianHostBuilder;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ResourceUtil;
import java.text.MessageFormat;
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
public class RestJiraIssueApiClientTest extends ApplicationIntegrationTestBase {
  private static final String ISSUE_TYPE_ID = "10045";
  private static final String PARENT_ISSUE_ID = "10040";

  private transient AtlassianHost atlassianHost;
  private transient MockRestServiceServer mockServer;

  @Autowired private transient RestJiraIssueApiClient restJiraIssueApiClient;
  @Autowired private transient JiraRestClient jiraRestClient;

  @Value("classpath:jira-issue-list-for-search-response.json")
  private transient Resource issueListResponse;

  @BeforeEach
  public void setUp() {
    atlassianHost = new AtlassianHostBuilder().withBaseUrl(AtlassianUtil.BASE_URL).build();
    mockServer = MockRestServiceServer.createServer(jiraRestClient.getRestTemplate(atlassianHost));
  }

  @Test
  public void testGetSubtasksByIssueTypeAndParentIssue() {

    final String jql =
        MessageFormat.format("issuetype={0}%20AND%20parent={1}", ISSUE_TYPE_ID, PARENT_ISSUE_ID);

    mockServer
        .expect(
            requestTo(
                startsWith(AtlassianUtil.BASE_URL + RestJiraIssueApiClient.JIRA_SEARCH_URL_BASE)))
        .andExpect(queryParam("jql", jql))
        .andExpect(queryParam("startAt", "0"))
        .andExpect(
            queryParam("maxResults", Integer.toString(RestJiraIssueApiClient.SEARCH_MAX_RESULTS)))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(ResourceUtil.asString(issueListResponse), MediaType.APPLICATION_JSON));

    restJiraIssueApiClient.getSubtasksByIssueTypeAndParentIssue(
        atlassianHost, ISSUE_TYPE_ID, PARENT_ISSUE_ID);

    mockServer.verify();
  }
}
