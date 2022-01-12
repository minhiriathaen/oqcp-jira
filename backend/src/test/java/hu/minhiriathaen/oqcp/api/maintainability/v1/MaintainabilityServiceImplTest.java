package hu.minhiriathaen.oqcp.api.maintainability.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.branch.OpenQualityCheckerBranchApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualification;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualificationResult;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.QualificationValue;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

@SpringBootTest
class MaintainabilityServiceImplTest extends ServiceTestBase {

  private static final String OQC_PROJECT_ID = "1";
  private static final String OQC_PROJECT_NAME = "OPEN_QUALITY_CHECKER_PROJECT_NAME";
  private static final String JIRA_PROJECT_ID = "JIRA_PROJECT_ID";

  private static final String OTHER_OQC_PROJECT_ID = "2";
  private static final String OTHER_OQC_PROJECT_NAME = "OTHER_OPEN_QUALITY_CHECKER_PROJECT_NAME";
  private static final String OTHER_JIRA_PROJECT_ID = "OTHER_JIRA_PROJECT_ID";

  private static final Long MASTER_BRANCH_ID = 1100L;
  private static final String MASTER_BRANCH_NAME = "master";
  private static final Double MASTER_MAINTAINABILITY = 6.6;

  private static final Long BRANCH_ONE_ID = 1111L;
  private static final String BRANCH_ONE_NAME = "branch-one";
  private static final Double BRANCH_ONE_MAINTAINABILITY = 3.5;

  private static final Long BRANCH_TWO_ID = 1122L;
  private static final String BRANCH_TWO_NAME = "branch-two";
  private static final Double BRANCH_TWO_MAINTAINABILITY = 1.5;

  @Autowired private transient MaintainabilityServiceImpl maintainabilityService;

  @MockBean private transient OpenQualityCheckerBranchApiClient openQualityCheckerBranchApiClient;

  @SpyBean private transient MaintainabilityConverter maintainabilityConverter;

  @Test
  public void testGetMaintainabilitiesWithNullHostUser() {
    try {
      maintainabilityService.getMaintainabilities(null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host user is required");
    }
  }

  @Test
  public void testGetMaintainabilitiesWithNullBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(null);

    try {
      maintainabilityService.getMaintainabilities(hostUser);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host base URL is required");
    }
  }

  @Test
  public void testGetMaintainabilitiesWithEmptyBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(StringUtils.EMPTY);

    try {
      maintainabilityService.getMaintainabilities(hostUser);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host base URL is required");
    }
  }

  /** Test case: OQCPD_27_BCK_01_UT */
  @Test
  public void testGetMaintainabilitiesWithExistingAccountMapping() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    final AtlassianHostUser hostUser = createDefaultHostUser();

    maintainabilityService.getMaintainabilities(hostUser);

    verify(userMappingRepository)
        .findByAccountMappingAndAtlassianUserAccountId(accountMapping, AtlassianUtil.USER_ID);
  }

  /** Test case: OQCPD_27_BCK_02_UT */
  @Test
  public void testGetMaintainabilitiesWithNotExistingAccountMapping() {
    mockAccountMapping(AtlassianUtil.OTHER_BASE_URL);

    final AtlassianHostUser hostUser = createDefaultHostUser();

    try {
      maintainabilityService.getMaintainabilities(hostUser);

      failBecauseExceptionWasNotThrown(ServiceError.class);
    } catch (final ServiceError error) {
      assertServiceError(error, HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
    }
  }

  /** Test case: OQCPD_27_BCK_03_UT */
  @Test
  public void testGetMaintainabilitiesWithExistingUserMapping() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    maintainabilityService.getMaintainabilities(createDefaultHostUser());

    final List<String> openQualityCheckerIds = Arrays.asList(OQC_PROJECT_ID, OTHER_OQC_PROJECT_ID);

    verify(projectMappingRepository)
        .findByAccountMappingAndOpenQualityCheckerProjectIdIn(
            accountMapping, openQualityCheckerIds);
  }

  /** Test case: OQCPD_27_BCK_04_UT */
  @Test
  public void testGetMaintainabilitiesWithNotExistingUserMapping() {
    mockDefaultAccountMapping();
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    final AtlassianHostUser hostUser = createDefaultHostUser();

    try {
      maintainabilityService.getMaintainabilities(hostUser);

      failBecauseExceptionWasNotThrown(ServiceError.class);
    } catch (final ServiceError error) {
      assertServiceError(error, HttpStatus.FORBIDDEN, ErrorCode.USER_MAPPING_NOT_FOUND);
    }
  }

  /** Test case: OQCPD_27_BCK_05_UT */
  @Test
  public void testGetMaintainabilitiesWithSuccessfulOpenQualityCheckerApiCall() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    final UserMapping userMapping = mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockProjectMappings(accountMapping);

    final OpenQualityCheckerProject project =
        createOpenQualityCheckerProject(OQC_PROJECT_ID, OQC_PROJECT_NAME);
    final OpenQualityCheckerProject otherProject =
        createOpenQualityCheckerProject(OTHER_OQC_PROJECT_ID, OTHER_OQC_PROJECT_NAME);

    final List<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(
        createBranch(project, MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY));
    branches.add(createBranch(project, BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY));
    branches.add(createBranch(project, BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    final List<OpenQualityCheckerBranch> otherBranches = new ArrayList<>();
    otherBranches.add(
        createBranch(otherProject, MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY));
    otherBranches.add(
        createBranch(otherProject, BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY));
    otherBranches.add(
        createBranch(otherProject, BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    mockOpenQualityCheckerBranchApi(
        userMapping, OQC_PROJECT_ID, branches.toArray(new OpenQualityCheckerBranch[0]));
    mockOpenQualityCheckerBranchApi(
        userMapping, OTHER_OQC_PROJECT_ID, otherBranches.toArray(new OpenQualityCheckerBranch[0]));

    maintainabilityService.getMaintainabilities(createDefaultHostUser());

    verify(maintainabilityConverter).convert(branches);
    verify(maintainabilityConverter).convert(otherBranches);
  }

  /** Test case: OQCPD_27_BCK_06_UT */
  @Test
  public void testGetMaintainabilitiesWithNotSuccessfulOpenQualityCheckerApiCall() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockProjectMappings(accountMapping);

    when(openQualityCheckerBranchApiClient.getBranches(anyString(), anyString()))
        .thenThrow(new RuntimeException("OpenQualityChecker API server error"));

    try {
      maintainabilityService.getMaintainabilities(createDefaultHostUser());

      failBecauseExceptionWasNotThrown(ServiceError.class);
    } catch (final ServiceError error) {
      assertServiceError(
          error, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR);
    }
  }

  /** Test case: OQCPD_27_BCK_07_UT */
  @Test
  public void testGetMaintainabilitiesWithMasterBranch() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    final UserMapping userMapping = mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockProjectMappings(accountMapping);

    final OpenQualityCheckerProject project =
        createOpenQualityCheckerProject(OQC_PROJECT_ID, OQC_PROJECT_NAME);
    final OpenQualityCheckerProject otherProject =
        createOpenQualityCheckerProject(OTHER_OQC_PROJECT_ID, OTHER_OQC_PROJECT_NAME);

    mockOpenQualityCheckerBranchApi(
        userMapping,
        OQC_PROJECT_ID,
        createBranch(project, MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY),
        createBranch(project, BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
        createBranch(project, BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    mockOpenQualityCheckerBranchApi(
        userMapping,
        OTHER_OQC_PROJECT_ID,
        createBranch(otherProject, MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY),
        createBranch(otherProject, BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
        createBranch(otherProject, BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    final ProjectMaintainabilityTransfer transfer =
        createProjectTransfer(
            OQC_PROJECT_ID,
            OQC_PROJECT_NAME,
            MASTER_BRANCH_NAME,
            MASTER_MAINTAINABILITY,
            createBranchTransfer(MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY),
            createBranchTransfer(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
            createBranchTransfer(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    final ProjectMaintainabilityTransfer otherTransfer =
        createProjectTransfer(
            OTHER_OQC_PROJECT_ID,
            OTHER_OQC_PROJECT_NAME,
            MASTER_BRANCH_NAME,
            MASTER_MAINTAINABILITY,
            createBranchTransfer(MASTER_BRANCH_ID, MASTER_BRANCH_NAME, MASTER_MAINTAINABILITY),
            createBranchTransfer(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
            createBranchTransfer(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    final List<ProjectMaintainabilityTransfer> maintainabilities =
        maintainabilityService.getMaintainabilities(createDefaultHostUser());

    assertThat(maintainabilities).contains(transfer, otherTransfer);
  }

  /** Test case: OQCPD_27_BCK_08_UT */
  @Test
  public void testGetMaintainabilitiesWithNoMasterBranch() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    final UserMapping userMapping = mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockProjectMappings(accountMapping);

    final OpenQualityCheckerProject project =
        createOpenQualityCheckerProject(OQC_PROJECT_ID, OQC_PROJECT_NAME);
    final OpenQualityCheckerProject otherProject =
        createOpenQualityCheckerProject(OTHER_OQC_PROJECT_ID, OTHER_OQC_PROJECT_NAME);

    mockOpenQualityCheckerBranchApi(
        userMapping,
        OQC_PROJECT_ID,
        createBranch(project, BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
        createBranch(project, BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    mockOpenQualityCheckerBranchApi(
        userMapping,
        OTHER_OQC_PROJECT_ID,
        createBranch(otherProject, BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY),
        createBranch(otherProject, BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY));

    final List<ProjectMaintainabilityTransfer> maintainabilities =
        maintainabilityService.getMaintainabilities(createDefaultHostUser());

    final ProjectMaintainabilityTransfer expectedTransfer =
        createProjectTransfer(
            OQC_PROJECT_ID,
            OQC_PROJECT_NAME,
            BRANCH_ONE_NAME,
            BRANCH_ONE_MAINTAINABILITY,
            createBranchTransfer(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY),
            createBranchTransfer(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY));

    final ProjectMaintainabilityTransfer expectedOtherTransfer =
        createProjectTransfer(
            OTHER_OQC_PROJECT_ID,
            OTHER_OQC_PROJECT_NAME,
            BRANCH_TWO_NAME,
            BRANCH_TWO_MAINTAINABILITY,
            createBranchTransfer(BRANCH_TWO_ID, BRANCH_TWO_NAME, BRANCH_TWO_MAINTAINABILITY),
            createBranchTransfer(BRANCH_ONE_ID, BRANCH_ONE_NAME, BRANCH_ONE_MAINTAINABILITY));

    assertThat(maintainabilities).contains(expectedTransfer, expectedOtherTransfer);
  }

  /** Test case: */
  @Test
  public void testGetMaintainabilitiesWithNullBranchList() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockProjectMappings(accountMapping);

    when(openQualityCheckerBranchApiClient.getBranches(anyString(), anyString())).thenReturn(null);

    final List<ProjectMaintainabilityTransfer> maintainabilities =
        maintainabilityService.getMaintainabilities(createDefaultHostUser());

    assertThat(maintainabilities).isEmpty();
  }

  /** Test case: */
  @Test
  public void testGetMaintainabilitiesWithEmptyBranchList() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(accountMapping, OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockProjectMappings(accountMapping);

    when(openQualityCheckerBranchApiClient.getBranches(anyString(), anyString()))
        .thenReturn(Collections.emptyList());

    final List<ProjectMaintainabilityTransfer> maintainabilities =
        maintainabilityService.getMaintainabilities(createDefaultHostUser());

    assertThat(maintainabilities).isEmpty();
  }

  private ProjectMaintainabilityTransfer createProjectTransfer(
      final String openQualityCheckerProjectId,
      final String openQualityCheckerProjectName,
      final String mainBranchName,
      final Double maintainability,
      final BranchMaintainabilityTransfer... branchTransfers) {

    final ProjectMaintainabilityTransfer transfer = new ProjectMaintainabilityTransfer();
    transfer.setId(openQualityCheckerProjectId);
    transfer.setName(openQualityCheckerProjectName);

    transfer.setMainBranchName(mainBranchName);
    transfer.setMaintainabilityIndex(maintainability);

    transfer.setBranches(Arrays.asList(branchTransfers));

    return transfer;
  }

  private BranchMaintainabilityTransfer createBranchTransfer(
      final Long branchId, final String branchName, final Double maintainability) {

    final BranchMaintainabilityTransfer branchMaintainabilityTransfer =
        new BranchMaintainabilityTransfer();
    branchMaintainabilityTransfer.setId(branchId.toString());
    branchMaintainabilityTransfer.setName(branchName);
    branchMaintainabilityTransfer.setMaintainabilityIndex(maintainability);
    return branchMaintainabilityTransfer;
  }

  private void mockOpenQualityCheckerBranchApi(
      final UserMapping userMapping,
      final String openQualityCheckerProjectId,
      final OpenQualityCheckerBranch... branches) {

    when(openQualityCheckerBranchApiClient.getBranches(
            userMapping.getOpenQualityCheckerUserToken(), openQualityCheckerProjectId))
        .thenReturn(Arrays.asList(branches));
  }

  private void mockProjectMappings(final AccountMapping accountMapping) {
    final ArrayList<ProjectMapping> projectMappings = new ArrayList<>();
    projectMappings.add(createProjectMapping(accountMapping, JIRA_PROJECT_ID, OQC_PROJECT_ID));
    projectMappings.add(
        createProjectMapping(accountMapping, OTHER_JIRA_PROJECT_ID, OTHER_OQC_PROJECT_ID));

    final List<String> openQualityCheckerIds = Arrays.asList(OQC_PROJECT_ID, OTHER_OQC_PROJECT_ID);

    when(projectMappingRepository.findByAccountMappingAndOpenQualityCheckerProjectIdIn(
            accountMapping, openQualityCheckerIds))
        .thenReturn(projectMappings);
  }

  private OpenQualityCheckerBranch createBranch(
      final OpenQualityCheckerProject project,
      final Long branchId,
      final String branchName,
      final Double maintainability) {

    final OpenQualityCheckerBranch branch = new OpenQualityCheckerBranch();
    branch.setProject(project);
    branch.setId(branchId);
    branch.setName(branchName);
    branch.setQualificationResult(new OpenQualityCheckerQualificationResult());
    branch.getQualificationResult().setQualification(new OpenQualityCheckerQualification());
    branch.getQualificationResult().getQualification().setMaintainability(new QualificationValue());
    branch
        .getQualificationResult()
        .getQualification()
        .getMaintainability()
        .setValue(maintainability);
    return branch;
  }

  private OpenQualityCheckerProject createOpenQualityCheckerProject(
      final String openQualityCheckerProjectId, final String openQualityCheckerProjectName) {
    final OpenQualityCheckerProject project = new OpenQualityCheckerProject();
    project.setId(Long.parseLong(openQualityCheckerProjectId));
    project.setName(openQualityCheckerProjectName);
    return project;
  }

  private ProjectMapping createProjectMapping(
      final AccountMapping accountMapping,
      final String jiraProjectId,
      final String openQualityCheckerProjectId) {
    final ProjectMapping projectMapping = new ProjectMapping();
    projectMapping.setAccountMapping(accountMapping);
    projectMapping.setJiraProjectId(jiraProjectId);
    projectMapping.setOpenQualityCheckerProjectId(openQualityCheckerProjectId);
    return projectMapping;
  }
}
