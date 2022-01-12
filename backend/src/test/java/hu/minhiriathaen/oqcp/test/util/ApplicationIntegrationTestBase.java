package hu.minhiriathaen.oqcp.test.util;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.AtlassianHostUser.AtlassianHostUserBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.AccountMappingRepository;
import hu.minhiriathaen.oqcp.persistence.repository.ProjectMappingRepository;
import hu.minhiriathaen.oqcp.persistence.repository.UserMappingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class ApplicationIntegrationTestBase {

  public static final String OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OPEN_QUALITY_CHECKER_ACCOUNT_NAME";
  public static final String OPEN_QUALITY_CHECKER_USER_TOKEN = "OPEN_QUALITY_CHECKER_USER_TOKEN";

  public static final String OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME";
  public static final String OTHER_OPEN_QUALITY_CHECKER_USER_TOKEN =
      "OTHER_OPEN_QUALITY_CHECKER_USER_TOKEN";

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected static final String RESPONSE_BODY_ERROR_CODE = "$.code";
  protected static final String RESPONSE_BODY_ERROR_MESSAGE = "$.message";

  protected static final String ADMIN_JIRA_USER_ID = "1";

  @SuppressWarnings("SpringJavaAutowiredMembersInspection")
  @Autowired
  protected transient AccountMappingRepository accountMappingRepository;

  @SuppressWarnings("SpringJavaAutowiredMembersInspection")
  @Autowired
  protected transient UserMappingRepository userMappingRepository;

  @Autowired protected transient ProjectMappingRepository projectMappingRepository;

  @SuppressWarnings("SpringJavaAutowiredMembersInspection")
  @Autowired
  protected transient WebApplicationContext webApplicationContext;

  protected transient MockMvc mvc;

  @BeforeEach
  public void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).alwaysDo(print()).build();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
    userMappingRepository.deleteAll();
    projectMappingRepository.deleteAll();
    accountMappingRepository.deleteAll();
  }

  protected void mockDefaultAuthentication() {
    mockAuthentication(AtlassianUtil.BASE_URL);
  }

  protected void mockAuthentication(final String baseUrl) {
    setJwtAuthenticatedPrincipal(
        new AtlassianHostBuilder().withBaseUrl(baseUrl).build(), AtlassianUtil.USER_ID);
  }

  protected AccountMapping mockDefaultAccountMapping() {
    return mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME);
  }

  protected AccountMapping mockOtherAccountMapping() {
    return mockAccountMapping(
        AtlassianUtil.OTHER_BASE_URL, OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME);
  }

  protected AccountMapping mockAccountMapping(
      final String atlassianHostUrl, final String openQualityCheckerAccountName) {

    final AccountMapping accountMapping = new AccountMapping();
    accountMapping.setAtlassianHostUrl(atlassianHostUrl);
    accountMapping.setOpenQualityCheckerAccountName(openQualityCheckerAccountName);

    accountMappingRepository.save(accountMapping);

    return accountMapping;
  }

  protected void mockProjectMapping(
      final AccountMapping accountMapping,
      final String openQualityCheckerProjectId,
      final String jiraProjectId) {
    mockProjectMapping(
        accountMapping, openQualityCheckerProjectId, jiraProjectId, ADMIN_JIRA_USER_ID);
  }

  protected void mockProjectMapping(
      final AccountMapping accountMapping,
      final String openQualityCheckerProjectId,
      final String jiraProjectId,
      final String creatorAtlassianUserAccountId) {

    final ProjectMapping projectMapping = new ProjectMapping();
    projectMapping.setAccountMapping(accountMapping);
    projectMapping.setOpenQualityCheckerProjectId(openQualityCheckerProjectId);
    projectMapping.setJiraProjectId(jiraProjectId);
    projectMapping.setCreatorAtlassianUserAccountId(creatorAtlassianUserAccountId);

    projectMappingRepository.save(projectMapping);
  }

  protected void mockDefaultUserMapping(final AccountMapping accountMapping) {
    mockUserMapping(accountMapping, OPEN_QUALITY_CHECKER_USER_TOKEN);
  }

  protected void mockOtherUserMapping(final AccountMapping accountMapping) {
    mockUserMapping(accountMapping, OTHER_OPEN_QUALITY_CHECKER_USER_TOKEN);
  }

  protected void mockUserMapping(
      final AccountMapping accountMapping, final String openQualityCheckerUserToken) {

    final UserMapping userMapping = new UserMapping();
    userMapping.setAccountMapping(accountMapping);
    userMapping.setAtlassianUserAccountId(AtlassianUtil.USER_ID);
    userMapping.setOpenQualityCheckerUserToken(openQualityCheckerUserToken);

    userMappingRepository.save(userMapping);
  }

  protected void setJwtAuthenticatedPrincipal(
      final AtlassianHost host, final String userAccountId) {

    final AtlassianHostUserBuilder hostUserBuilder = AtlassianHostUser.builder(host);
    if (userAccountId != null) {
      hostUserBuilder.withUserAccountId(userAccountId);
    }

    final TestingAuthenticationToken authentication =
        new TestingAuthenticationToken(hostUserBuilder.build(), null);
    authentication.setAuthenticated(true);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  protected String toRequestBody(final Object transfer) throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(transfer);
  }

  protected ResultActions assertError(
      final ResultActions resultActions,
      final ResultMatcher resultMatcher,
      final ErrorCode errorCode)
      throws Exception {

    return resultActions
        .andExpect(resultMatcher)
        .andExpect(MockMvcResultMatchers.jsonPath(RESPONSE_BODY_ERROR_CODE).value(errorCode.name()))
        .andExpect(
            MockMvcResultMatchers.jsonPath(RESPONSE_BODY_ERROR_MESSAGE)
                .value(errorCode.getMessage()));
  }
}
