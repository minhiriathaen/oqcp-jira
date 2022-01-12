package hu.minhiriathaen.oqcp.api.project.v1;

import static org.assertj.core.api.Assertions.assertThat;

import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.repository.ProjectMappingRepository;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Slf4j
@SpringBootTest
public class ProjectMappingControllerTest extends ApplicationIntegrationTestBase {

  private static final String OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OPEN_QUALITY_CHECKER_ACCOUNT_NAME";
  private static final String OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME";

  private static final String OPEN_QUALITY_CHECKER_PROJECT_ID = "OPEN_QUALITY_CHECKER_PROJECT_ID";
  private static final String JIRA_PROJECT_ID = "JIRA_PROJECT_ID";
  private static final String OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID =
      "OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID";
  private static final String OTHER_JIRA_PROJECT_ID = "OTHER_JIRA_PROJECT_ID";

  @Autowired
  @SuppressWarnings("SpringJavaAutowiredMembersInspection")
  protected transient ProjectMappingRepository projectMappingRepository;

  @Test
  public void testGetProjectMappingWithUnauthenticated() throws Exception {
    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        JIRA_PROJECT_ID);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.OTHER_BASE_URL, OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_JIRA_PROJECT_ID);

    mvc.perform(createGetProjectMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testGetProjectMappingWithNullBaseUrl() throws Exception {
    mockAuthentication(null);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        JIRA_PROJECT_ID);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.OTHER_BASE_URL, OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_JIRA_PROJECT_ID);

    mvc.perform(createGetProjectMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testGetProjectMappingWithWithEmptyBaseUrl() throws Exception {
    mockAuthentication(StringUtils.EMPTY);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        JIRA_PROJECT_ID);

    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.OTHER_BASE_URL, OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID,
        OTHER_JIRA_PROJECT_ID);

    mvc.perform(createGetProjectMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testStoreProjectMappingWithUnauthenticated() throws Exception {

    final ProjectMappingTransfer transfer = createMappingTransfer(null);

    mvc.perform(createStoreProjectMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testStoreProjectMappingWithNullBaseUrl() throws Exception {
    mockAuthentication(null);

    final ProjectMappingTransfer transfer =
        createMappingTransfer(Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID));

    mvc.perform(createStoreProjectMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testStoreProjectMappingWithEmptyBaseUrl() throws Exception {
    mockAuthentication(StringUtils.EMPTY);

    final ProjectMappingTransfer transfer =
        createMappingTransfer(Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID));

    mvc.perform(createStoreProjectMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  /** Test case: OQCPD_25_BCK_05_IT */
  @Test
  public void testGetProjectMappingWithExistingProjectMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockProjectMapping(
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME),
        OPEN_QUALITY_CHECKER_PROJECT_ID,
        JIRA_PROJECT_ID);

    mvc.perform(createGetProjectMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.openQualityCheckerProjectIds[0]")
                .value(OPEN_QUALITY_CHECKER_PROJECT_ID));
  }

  @Test
  public void testGetProjectMappingWithTwoExistingProjectMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    final AccountMapping accountMapping =
        mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME);
    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);

    mockProjectMapping(accountMapping, OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);

    mvc.perform(createGetProjectMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.openQualityCheckerProjectIds[0]")
                .value(OPEN_QUALITY_CHECKER_PROJECT_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.openQualityCheckerProjectIds[1]")
                .value(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));
  }

  /** Test case: OQCPD_25_BCK_06_IT */
  @Test
  public void testGetProjectMappingNotExistingAccountMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);

    assertError(
        mvc.perform(createGetProjectMappingRequestBuilder()),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_25_BCK_07_IT */
  @Test
  public void testGetProjectMappingWithExistingUserMappingWithoutProjectMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME);

    mvc.perform(createGetProjectMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.openQualityCheckerProjectIds").isEmpty());
  }

  /** Test case: OQCPD_25_BCK_08_IT (Best Case) */
  @Test
  public void testStoreProjectMappingWithAccountMappingAndOpenQualityCheckerProjectIds()
      throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);

    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockDefaultUserMapping(accountMapping);

    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.singletonList(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));

    mvc.perform(
            createStoreProjectMappingRequestBuilder()
                .content(toRequestBody(projectMappingTransfer)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    final List<ProjectMapping> savedProjectMapping =
        projectMappingRepository.findByAccountMapping(accountMapping);

    assertThat(savedProjectMapping.stream().map(ProjectMapping::getOpenQualityCheckerProjectId))
        .contains(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID);
    assertThat(savedProjectMapping.stream().map(ProjectMapping::getOpenQualityCheckerProjectId))
        .doesNotContain(OPEN_QUALITY_CHECKER_PROJECT_ID);
  }

  /** Test case: OQCPD_25_BCK_09_IT */
  @Test
  public void testStoreProjectMappingWithNullOpenQualityCheckerProjectIds() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);

    mockAccountMapping(AtlassianUtil.BASE_URL, OPEN_QUALITY_CHECKER_ACCOUNT_NAME);

    final ProjectMappingTransfer projectMappingTransfer = createMappingTransfer(null);

    assertError(
        mvc.perform(
            createStoreProjectMappingRequestBuilder()
                .content(toRequestBody(projectMappingTransfer))),
        MockMvcResultMatchers.status().isBadRequest(),
        ErrorCode.OPEN_QUALITY_CHECKER_PROJECT_IDS_REQUIRED);
  }

  @Test
  public void testStoreProjectMappingWithEmptyOpenQualityCheckerProjectIds() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);

    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockDefaultUserMapping(accountMapping);

    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(Collections.emptyList());

    mvc.perform(
            createStoreProjectMappingRequestBuilder()
                .content(toRequestBody(projectMappingTransfer)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    final List<ProjectMapping> projectMappings =
        projectMappingRepository.findByAccountMapping(accountMapping);

    assertThat(projectMappings).isEmpty();
  }

  /** Test case: OQCPD_25_BCK_10_IT */
  @Test
  public void testStoreProjectMappingNotExistingAccountMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);

    final List<String> openQualityCheckerProjectIds =
        Collections.singletonList(OPEN_QUALITY_CHECKER_PROJECT_ID);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(openQualityCheckerProjectIds);

    assertError(
        mvc.perform(
            createStoreProjectMappingRequestBuilder()
                .content(toRequestBody(projectMappingTransfer))),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_25_BCK_11_IT */
  @Test
  public void
      testStoreProjectMappingWithAccountMappingAndOpenQualityCheckerProjectIdsOneIdIsAlreadyMapped()
          throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);

    final AccountMapping accountMapping = mockDefaultAccountMapping();

    mockProjectMapping(accountMapping, OPEN_QUALITY_CHECKER_PROJECT_ID, JIRA_PROJECT_ID);
    mockProjectMapping(
        accountMapping, OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID, OTHER_JIRA_PROJECT_ID);

    final ProjectMappingTransfer projectMappingTransfer =
        createMappingTransfer(
            Arrays.asList(OPEN_QUALITY_CHECKER_PROJECT_ID, OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID));

    assertError(
            mvc.perform(
                createStoreProjectMappingRequestBuilder()
                    .content(toRequestBody(projectMappingTransfer))),
            MockMvcResultMatchers.status().isConflict(),
            ErrorCode.OPEN_QUALITY_CHECKER_PROJECTS_ALREADY_MAPPED)
        .andExpect(
            MockMvcResultMatchers.jsonPath("mappedOpenQualityCheckerProjectIds[0]")
                .value(OTHER_OPEN_QUALITY_CHECKER_PROJECT_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("mappedOpenQualityCheckerProjectIds[1]").doesNotExist());
  }

  private MockHttpServletRequestBuilder createGetProjectMappingRequestBuilder() {
    return MockMvcRequestBuilders.get(ProjectMappingController.PROJECT_MAPPING_URL, JIRA_PROJECT_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }

  private MockHttpServletRequestBuilder createStoreProjectMappingRequestBuilder() {
    return MockMvcRequestBuilders.put(ProjectMappingController.PROJECT_MAPPING_URL, JIRA_PROJECT_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }

  private ProjectMappingTransfer createMappingTransfer(
      final List<String> openQualityCheckerProjectIds) {
    final ProjectMappingTransfer request = new ProjectMappingTransfer();
    request.setOpenQualityCheckerProjectIds(openQualityCheckerProjectIds);

    return request;
  }
}
