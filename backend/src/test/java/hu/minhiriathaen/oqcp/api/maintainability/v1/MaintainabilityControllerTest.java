package hu.minhiriathaen.oqcp.api.maintainability.v1;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.openqualitychecker.branch.OpenQualityCheckerBranchApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualification;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualificationResult;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.QualificationValue;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@SpringBootTest
public class MaintainabilityControllerTest extends ApplicationIntegrationTestBase {

  private static final String OPEN_QUALITY_CHECKER_PROJECT_ID = "1";
  private static final String OPEN_QUALITY_CHECKER_PROJECT_NAME =
      "OPEN_QUALITY_CHECKER_PROJECT_NAME";

  private static final String JIRA_PROJECT_ID = "JIRA_PROJECT_ID";

  private static final String OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID = "2";
  private static final String OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME";
  private static final String OTHER_JIRA_PROJECT_ID = "OTHER_JIRA_PROJECT_ID";

  private static final String UNKNOWN_OPEN_QUALITY_CHECKER_PROJECT_ID = "3";
  private static final String UNKNOWN_JIRA_PROJECT_ID = "UNKNOWN_JIRA_PROJECT_ID";

  private static final Long MASTER_BRANCH_ID = 1100L;
  private static final String MASTER_BRANCH_NAME = "master";
  private static final Double MASTER_MAINTAINABILITY = 6.6;

  private static final Long BRANCH_ONE_ID = 1111L;
  private static final String BRANCH_ONE_NAME = "branch-one";
  private static final Double BRANCH_ONE_MAINTAINABILITY = 3.5;

  private static final Long BRANCH_TWO_ID = 1122L;
  private static final String BRANCH_TWO_NAME = "branch-two";
  private static final Double BRANCH_TWO_MAINTAINABILITY = 1.5;

  @MockBean private transient OpenQualityCheckerBranchApiClient openQualityCheckerBranchApiClient;

  @Test
  public void testGetMaintainabilitiesWithUnauthenticated() throws Exception {
    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        JIRA_PROJECT_ID);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.OTHER_BASE_URL, OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_JIRA_PROJECT_ID);

    mvc.perform(createGetMaintainabilitiesRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testGetMaintainabilitiesWithNullBaseUrl() throws Exception {
    mockAuthentication(null);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        JIRA_PROJECT_ID);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.OTHER_BASE_URL, OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_JIRA_PROJECT_ID);

    mvc.perform(createGetMaintainabilitiesRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testGetMaintainabilitiesWithEmptyBaseUrl() throws Exception {
    mockAuthentication(StringUtils.EMPTY);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        JIRA_PROJECT_ID);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.OTHER_BASE_URL, OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_JIRA_PROJECT_ID);

    mvc.perform(createGetMaintainabilitiesRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  /** Test case: OQCPD_27_BCK_01_IT (Best case) */
  @Test
  public void testGetMaintainabilitiesWithExistingMasterBranch() throws Exception {
    mockDefaultAuthentication();

    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockOtherAccountMapping();

    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);
    mockProjectMapping(
        accountMapping, OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID, OTHER_JIRA_PROJECT_ID);

    mockDefaultUserMapping(accountMapping);

    mockOpenQualityCheckerBranchClient(
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        OPEN_QUALITY_CHECKER_PROJECT_NAME,
        createOpenQualityCheckerBranch(
            MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY),
        createOpenQualityCheckerBranch(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
        createOpenQualityCheckerBranch(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    mockOpenQualityCheckerBranchClient(
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_JIRA_PROJECT_ID,
        createOpenQualityCheckerBranch(
            MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY),
        createOpenQualityCheckerBranch(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
        createOpenQualityCheckerBranch(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    mvc.perform(createGetMaintainabilitiesRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(OPEN_QUALITY_CHECKER_PROJECT_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].name").value(OPEN_QUALITY_CHECKER_PROJECT_NAME))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].mainBranchName").value(MASTER_BRANCH_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].maintainabilityIndex")
                .value(MASTER_MAINTAINABILITY))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[0].id").value(MASTER_BRANCH_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].branches[0].name").value(MASTER_BRANCH_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].branches[0].maintainabilityIndex")
                .value(MASTER_MAINTAINABILITY))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[1].id").value(BRANCH_ONE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[1].name").value(BRANCH_ONE_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].branches[1].maintainabilityIndex")
                .value(BRANCH_ONE_MAINTAINABILITY))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[2].id").value(BRANCH_TWO_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[2].name").value(BRANCH_TWO_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].branches[2].maintainabilityIndex")
                .value(BRANCH_TWO_MAINTAINABILITY))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[1].id").value(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(OTHER_JIRA_PROJECT_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].mainBranchName").value(MASTER_BRANCH_NAME))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].branches[0].id").value(MASTER_BRANCH_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[1].branches[0].name").value(MASTER_BRANCH_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[1].branches[0].maintainabilityIndex")
                .value(MASTER_MAINTAINABILITY))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].branches[1].id").value(BRANCH_ONE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].branches[1].name").value(BRANCH_ONE_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[1].branches[1].maintainabilityIndex")
                .value(BRANCH_ONE_MAINTAINABILITY))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].branches[2].id").value(BRANCH_TWO_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].branches[2].name").value(BRANCH_TWO_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[1].branches[2].maintainabilityIndex")
                .value(BRANCH_TWO_MAINTAINABILITY));
  }

  /** Test case: OQCPD_27_BCK_02_IT */
  @Test
  public void testGetMaintainabilitiesWithNotExistingMasterBranch() throws Exception {
    mockDefaultAuthentication();

    final AccountMapping accountMapping = mockDefaultAccountMapping();
    final AccountMapping otherAccountMapping = mockOtherAccountMapping();

    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);
    mockProjectMapping(
        otherAccountMapping, OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID, OTHER_JIRA_PROJECT_ID);

    mockDefaultUserMapping(accountMapping);
    mockOtherUserMapping(otherAccountMapping);

    mockOpenQualityCheckerBranchClient(
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        OPEN_QUALITY_CHECKER_PROJECT_NAME,
        createOpenQualityCheckerBranch(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
        createOpenQualityCheckerBranch(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    mvc.perform(createGetMaintainabilitiesRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(OPEN_QUALITY_CHECKER_PROJECT_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].name").value(OPEN_QUALITY_CHECKER_PROJECT_NAME))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].mainBranchName").value(BRANCH_ONE_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].maintainabilityIndex")
                .value(BRANCH_ONE_MAINTAINABILITY))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[0].id").value(BRANCH_ONE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[0].name").value(BRANCH_ONE_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].branches[0].maintainabilityIndex")
                .value(BRANCH_ONE_MAINTAINABILITY))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[1].id").value(BRANCH_TWO_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].branches[1].name").value(BRANCH_TWO_NAME))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].branches[1].maintainabilityIndex")
                .value(BRANCH_TWO_MAINTAINABILITY));
  }

  /** Test case: OQCPD_27_BCK_03_IT */
  @Test
  public void testGetMaintainabilitiesWithNotExistingAccountMapping() throws Exception {
    mockDefaultAuthentication();

    assertError(
        mvc.perform(createGetMaintainabilitiesRequestBuilder()),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_27_BCK_04_IT */
  @Test
  public void testGetMaintainabilitiesWithNotExistingUserMapping() throws Exception {
    mockDefaultAuthentication();
    mockDefaultAccountMapping();

    assertError(
        mvc.perform(createGetMaintainabilitiesRequestBuilder()),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.USER_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_27_BCK_05_IT */
  @Test
  public void testGetMaintainabilitiesWithOpenQualityCheckerApiError() throws Exception {
    mockDefaultAuthentication();
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockDefaultUserMapping(accountMapping);
    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);

    when(openQualityCheckerBranchApiClient.getBranches(anyString(), anyString()))
        .thenThrow(new RuntimeException("OpenQualityChecker API server error"));

    assertError(
        mvc.perform(createGetMaintainabilitiesRequestBuilder()),
        MockMvcResultMatchers.status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        ErrorCode.OPEN_QUALITY_CHECKER_ERROR);
  }

  /** Test case: OQCPD_239 */
  @Test
  public void testGetMaintainabilitiesWithUnknownOpenQualityCheckerProject() throws Exception {
    mockDefaultAuthentication();

    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockDefaultUserMapping(accountMapping);

    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);
    mockProjectMapping(
        accountMapping, OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID, OTHER_JIRA_PROJECT_ID);
    mockProjectMapping(
        accountMapping, UNKNOWN_OPEN_QUALITY_CHECKER_PROJECT_ID, UNKNOWN_JIRA_PROJECT_ID);

    mockOpenQualityCheckerBranchClient(
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        OPEN_QUALITY_CHECKER_PROJECT_NAME,
        createOpenQualityCheckerBranch(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
        createOpenQualityCheckerBranch(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    mockOpenQualityCheckerBranchClientForbiddenResponse(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID);

    mvc.perform(createGetMaintainabilitiesRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(OPEN_QUALITY_CHECKER_PROJECT_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());
  }

  private void mockOpenQualityCheckerBranchClient(
      final String openQualityCheckerProjectId,
      final String openQualityCheckerProjectName,
      final OpenQualityCheckerBranch... openQualityCheckerBranches) {

    final OpenQualityCheckerProject openQualityCheckerProject = new OpenQualityCheckerProject();
    openQualityCheckerProject.setId(Long.parseLong(openQualityCheckerProjectId));
    openQualityCheckerProject.setName(openQualityCheckerProjectName);

    Arrays.stream(openQualityCheckerBranches)
        .forEach(
            openQualityCheckerBranch ->
                openQualityCheckerBranch.setProject(openQualityCheckerProject));

    when(openQualityCheckerBranchApiClient.getBranches(
            anyString(), eq(openQualityCheckerProjectId)))
        .thenReturn(Arrays.asList(openQualityCheckerBranches));
  }

  private void mockOpenQualityCheckerBranchClientForbiddenResponse(
      final String openQualityCheckerProjectId) {

    when(openQualityCheckerBranchApiClient.getBranches(
            anyString(), eq(openQualityCheckerProjectId)))
        .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));
  }

  private OpenQualityCheckerBranch createOpenQualityCheckerBranch(
      final Long branchId, final String branchName, final Double maintainability) {

    final OpenQualityCheckerBranch openQualityCheckerBranch = new OpenQualityCheckerBranch();
    openQualityCheckerBranch.setId(branchId);
    openQualityCheckerBranch.setName(branchName);
    openQualityCheckerBranch.setQualificationResult(new OpenQualityCheckerQualificationResult());
    openQualityCheckerBranch
        .getQualificationResult()
        .setQualification(new OpenQualityCheckerQualification());
    openQualityCheckerBranch
        .getQualificationResult()
        .getQualification()
        .setMaintainability(new QualificationValue());

    openQualityCheckerBranch
        .getQualificationResult()
        .getQualification()
        .getMaintainability()
        .setValue(maintainability);

    return openQualityCheckerBranch;
  }

  private RequestBuilder createGetMaintainabilitiesRequestBuilder() {
    return MockMvcRequestBuilders.get(MaintainabilityController.GET_MAINTAINABILITIES_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }
}
