package hu.minhiriathaen.oqcp.api.project.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.BadRequestError;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.OpenQualityCheckerProjectConflictError;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import hu.minhiriathaen.oqcp.util.AccountMappingUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

@Slf4j
@SpringBootTest
public class ProjectMappingServiceImplTest extends ServiceTestBase {

  private static final String JIRA_PROJECT_ID = "JIRA_PROJECT_ID";
  private static final String OTHER_JIRA_PROJECT_ID = "OTHER_JIRA_PROJECT_ID";
  private static final String OPEN_QUALITY_CHECKER_PROJECT_ID = "OPEN_QUALITY_CHECKER_PROJECT_ID";
  private static final String OQC_USER_TOKEN = "OPEN_QUALITY_CHECKER_USER_TOKEN";
  private static final String OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID =
      "OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID";
  private static final String THIRD_OPEN_QUALITY_CHECKER_PROJECT_ID =
      "THIRD_OPEN_QUALITY_CHECKER_PROJECT_ID";
  private static final String BASE_URL_REQUIRED_ERROR_MESSAGE =
      "Atlassian host base URL is required";

  @Autowired private transient ProjectMappingServiceImpl projectMappingService;

  @SpyBean protected transient AccountMappingUtil accountMappingUtil;

  @SpyBean protected transient AsyncProjectMappingService asyncProjectMappingService;

  @Test
  public void testGetProjectMappingWithNullHostUser() {
    try {
      projectMappingService.getProjectMapping(null, JIRA_PROJECT_ID);

      fail("No IllegalArgumentException is thrown for wrong parameter");
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host user is required");
    }
  }

  @Test
  public void testGetProjectMappingWithNullBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(null);

    try {
      projectMappingService.getProjectMapping(hostUser, JIRA_PROJECT_ID);

      fail("No IllegalArgumentException is thrown for wrong parameter");
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo(BASE_URL_REQUIRED_ERROR_MESSAGE);
    }
  }

  @Test
  public void testGetProjectMappingWithEmptyBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(StringUtils.EMPTY);

    try {
      projectMappingService.getProjectMapping(hostUser, JIRA_PROJECT_ID);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo(BASE_URL_REQUIRED_ERROR_MESSAGE);
    }
  }

  @Test
  public void testStoreProjectMappingWithNullHostUser() {
    try {

      final ProjectMappingTransfer projectMappingTransfer =
          createMappingTransfer(Collections.singletonList(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));

      projectMappingService.storeProjectMapping(null, JIRA_PROJECT_ID, projectMappingTransfer);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host user is required");
    }
  }

  @Test
  public void testStoreProjectMappingWithNullBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(null);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.singletonList(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));

    try {
      projectMappingService.storeProjectMapping(hostUser, JIRA_PROJECT_ID, projectMappingTransfer);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo(BASE_URL_REQUIRED_ERROR_MESSAGE);
    }
  }

  @Test
  public void testStoreProjectMappingWithEmptyBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(StringUtils.EMPTY);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.singletonList(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));

    try {
      projectMappingService.storeProjectMapping(hostUser, JIRA_PROJECT_ID, projectMappingTransfer);

      fail("No IllegalArgumentException is thrown for wrong parameter");
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo(BASE_URL_REQUIRED_ERROR_MESSAGE);
    }
  }

  /** Test case: OQCPD_25_BCK_04_UT */
  @Test
  public void testGetProjectMappingWithoutAccountMapping() {
    mockAccountMapping(AtlassianUtil.OTHER_BASE_URL);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    try {
      projectMappingService.getProjectMapping(atlassianHostUser, JIRA_PROJECT_ID);

      fail("Process should thrown ServiceError");
    } catch (final ServiceError error) {
      assertServiceError(error, HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
    }
  }

  /** Test case: OQCPD_25_BCK_05_UT */
  @Test
  public void testGetProjectMappingExistingAccountMapping() {
    mockAccountMapping(AtlassianUtil.BASE_URL);
    mockAccountMapping(AtlassianUtil.OTHER_BASE_URL);

    final AtlassianHostUser hostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    try {
      final ProjectMappingTransfer transfer =
          projectMappingService.getProjectMapping(hostUser, JIRA_PROJECT_ID);
      assertThat(transfer.getOpenQualityCheckerProjectIds()).isEmpty();
    } catch (final ServiceError error) {
      fail("AccountMapping not found but it should");
    }
  }

  /** Test case: OQCPD_25_BCK_06_UT */
  @Test
  public void testGetProjectMappingWithoutProjectMapping() {
    final AccountMapping accountMapping = mockAccountMapping(AtlassianUtil.BASE_URL);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    final ProjectMappingTransfer transfer =
        projectMappingService.getProjectMapping(atlassianHostUser, JIRA_PROJECT_ID);

    verify(projectMappingRepository)
        .findByAccountMappingAndJiraProjectId(accountMapping, JIRA_PROJECT_ID);
    assertThat(transfer.getOpenQualityCheckerProjectIds()).isEmpty();
  }

  /** Test case: OQCPD_25_BCK_07_UT */
  @Test
  public void testGetProjectMappingWithExistingProjectMapping() {
    final AccountMapping accountMapping = mockAccountMapping(AtlassianUtil.BASE_URL);
    mockProjectMapping(accountMapping, JIRA_PROJECT_ID, OPEN_QUALITY_CHECKER_PROJECT_ID);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    final ProjectMappingTransfer transfer =
        projectMappingService.getProjectMapping(atlassianHostUser, JIRA_PROJECT_ID);

    verify(projectMappingRepository)
        .findByAccountMappingAndJiraProjectId(accountMapping, JIRA_PROJECT_ID);
    assertThat(transfer.getOpenQualityCheckerProjectIds())
        .contains(OPEN_QUALITY_CHECKER_PROJECT_ID);
  }

  @Test
  public void testGetProjectMappingWithTwoExistingProjectMapping() {
    final AccountMapping accountMapping = mockAccountMapping(AtlassianUtil.BASE_URL);

    mockProjectMapping(
        accountMapping,
        JIRA_PROJECT_ID,
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    final ProjectMappingTransfer transfer =
        projectMappingService.getProjectMapping(atlassianHostUser, JIRA_PROJECT_ID);

    verify(projectMappingRepository)
        .findByAccountMappingAndJiraProjectId(accountMapping, JIRA_PROJECT_ID);
    assertThat(transfer.getOpenQualityCheckerProjectIds())
        .contains(OPEN_QUALITY_CHECKER_PROJECT_ID, OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID);
  }

  /** Test case: OQCPD_25_BCK_08_UT */
  @Test
  public void testStoreProjectMappingWithNullOpenQualityCheckerProjectIds() {
    mockAccountMapping(AtlassianUtil.BASE_URL);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    final ProjectMappingTransfer projectMappingTransfer = createMappingTransfer(null);

    try {
      projectMappingService.storeProjectMapping(
          atlassianHostUser, JIRA_PROJECT_ID, projectMappingTransfer);

      failBecauseExceptionWasNotThrown(BadRequestError.class);
    } catch (final BadRequestError e) {
      assertThat(e.getErrorCode()).isEqualTo(ErrorCode.OPEN_QUALITY_CHECKER_PROJECT_IDS_REQUIRED);
    }
  }

  /** Test case: OQCPD_25_BCK_09_UT */
  @Test
  public void testStoreProjectMappingWithOpenQualityCheckerProjectIds() {
    final AccountMapping accountMapping = mockAccountMapping(AtlassianUtil.BASE_URL);
    mockUserMapping(accountMapping, OQC_USER_TOKEN);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID));

    projectMappingService.storeProjectMapping(
        atlassianHostUser, JIRA_PROJECT_ID, projectMappingTransfer);

    verify(accountMappingUtil).findAccountMapping(atlassianHostUser);
  }

  /** Test case: OQCPD_25_BCK_10_UT */
  @Test
  public void testStoreProjectMappingWithAccountMapping() {
    final AccountMapping accountMapping = mockAccountMapping(AtlassianUtil.BASE_URL);
    mockUserMapping(accountMapping, OQC_USER_TOKEN);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID));

    projectMappingService.storeProjectMapping(
        atlassianHostUser, JIRA_PROJECT_ID, projectMappingTransfer);

    verify(accountMappingUtil).findAccountMapping(atlassianHostUser);
    verify(projectMappingRepository)
        .findByOpenQualityCheckerProjectIdInAndJiraProjectIdNot(
            Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID), JIRA_PROJECT_ID);

    verify(asyncProjectMappingService)
        .processDeletedProjectMappings(eq(OQC_USER_TOKEN), eq(Collections.emptySet()));
  }

  /** Test case: OQCPD_25_BCK_11_UT */
  @Test
  public void testStoreProjectMappingWithoutAccountMapping() {

    mockAccountMapping(AtlassianUtil.OTHER_BASE_URL);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID));

    try {
      projectMappingService.storeProjectMapping(
          atlassianHostUser, JIRA_PROJECT_ID, projectMappingTransfer);

      failBecauseExceptionWasNotThrown(ServiceError.class);
    } catch (final ServiceError error) {
      assertServiceError(error, HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
    }
  }

  /** Test case: OQCPD_25_BCK_12_UT */
  @Test
  public void testStoreProjectMappingWithoutProjectMappingConflict() {
    final AccountMapping accountMapping = mockAccountMapping(AtlassianUtil.BASE_URL);
    mockUserMapping(accountMapping, OQC_USER_TOKEN);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    mockProjectMapping(
        accountMapping,
        JIRA_PROJECT_ID,
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID);

    mockProjectMappingJiraProjectIdNot(accountMapping, JIRA_PROJECT_ID);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(
            Arrays.asList(OPEN_QUALITY_CHECKER_PROJECT_ID, THIRD_OPEN_QUALITY_CHECKER_PROJECT_ID));

    projectMappingService.storeProjectMapping(
        atlassianHostUser, JIRA_PROJECT_ID, projectMappingTransfer);

    final ProjectMapping expectedProjectMapping = new ProjectMapping();
    expectedProjectMapping.setOpenQualityCheckerProjectId(THIRD_OPEN_QUALITY_CHECKER_PROJECT_ID);
    expectedProjectMapping.setAccountMapping(accountMapping);
    expectedProjectMapping.setJiraProjectId(JIRA_PROJECT_ID);

    verify(projectMappingRepository)
        .deleteByAccountMappingAndOpenQualityCheckerProjectIdIn(
            accountMapping, Set.of(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));
    verify(projectMappingRepository).save(expectedProjectMapping);
    verify(asyncProjectMappingService)
        .processDeletedProjectMappings(
            OQC_USER_TOKEN, Collections.singleton(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));
  }

  /** Test case: OQCPD_25_BCK_13_UT */
  @Test
  public void testStoreProjectMappingWithProjectMappingConflict() {
    final AccountMapping accountMapping = mockAccountMapping(AtlassianUtil.BASE_URL);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    mockProjectMappingJiraProjectIdNot(
        accountMapping, OTHER_JIRA_PROJECT_ID, OPEN_QUALITY_CHECKER_PROJECT_ID);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID));

    try {
      projectMappingService.storeProjectMapping(
          atlassianHostUser, JIRA_PROJECT_ID, projectMappingTransfer);

      failBecauseExceptionWasNotThrown(OpenQualityCheckerProjectConflictError.class);
    } catch (final OpenQualityCheckerProjectConflictError error) {
      assertServiceError(
          error, HttpStatus.CONFLICT, ErrorCode.OPEN_QUALITY_CHECKER_PROJECTS_ALREADY_MAPPED);
      assertThat(error.getMappedOpenQualityCheckerProjectIds())
          .contains(OPEN_QUALITY_CHECKER_PROJECT_ID);
    }
  }

  private void mockProjectMapping(
      final AccountMapping accountMapping,
      final String jiraProjectId,
      final String... openQualityCheckerProjectIds) {

    when(projectMappingRepository.findByAccountMappingAndJiraProjectId(
            accountMapping, JIRA_PROJECT_ID))
        .thenReturn(
            Arrays.stream(openQualityCheckerProjectIds)
                .map(
                    openQualityCheckerProjectId -> {
                      final ProjectMapping projectMapping = new ProjectMapping();
                      projectMapping.setAccountMapping(accountMapping);
                      projectMapping.setOpenQualityCheckerProjectId(openQualityCheckerProjectId);
                      projectMapping.setJiraProjectId(jiraProjectId);
                      return projectMapping;
                    })
                .collect(Collectors.toList()));
  }

  private void mockProjectMappingJiraProjectIdNot(
      final AccountMapping accountMapping,
      final String jiraProjectId,
      final String... openQualityCheckerProjectIds) {

    when(projectMappingRepository.findByOpenQualityCheckerProjectIdInAndJiraProjectIdNot(
            Arrays.asList(openQualityCheckerProjectIds), JIRA_PROJECT_ID))
        .thenReturn(
            Arrays.stream(openQualityCheckerProjectIds)
                .map(
                    openQualityCheckerProjectId -> {
                      final ProjectMapping projectMapping = new ProjectMapping();
                      projectMapping.setAccountMapping(accountMapping);
                      projectMapping.setOpenQualityCheckerProjectId(openQualityCheckerProjectId);
                      projectMapping.setJiraProjectId(jiraProjectId);
                      return projectMapping;
                    })
                .collect(Collectors.toList()));
  }

  private ProjectMappingTransfer createMappingTransfer(
      final List<String> openQualityCheckerProjectIds) {
    final ProjectMappingTransfer request = new ProjectMappingTransfer();
    request.setOpenQualityCheckerProjectIds(openQualityCheckerProjectIds);

    return request;
  }
}
