package hu.minhiriathaen.oqcp.api.project.v1;

import static hu.minhiriathaen.oqcp.openqualitychecker.project.MockOpenQualityCheckerProjectApiClient.PROJECT_ID_1;
import static hu.minhiriathaen.oqcp.openqualitychecker.project.MockOpenQualityCheckerProjectApiClient.PROJECT_ID_2;
import static hu.minhiriathaen.oqcp.openqualitychecker.project.MockOpenQualityCheckerProjectApiClient.PROJECT_NAME_1;
import static hu.minhiriathaen.oqcp.openqualitychecker.project.MockOpenQualityCheckerProjectApiClient.PROJECT_NAME_2;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Slf4j
@SpringBootTest
public class OpenQualityCheckerProjectControllerTest extends ApplicationIntegrationTestBase {

  @MockBean private transient OpenQualityCheckerProjectApiClient openQualityCheckerProjectApiClient;

  /** Test case: OQCPD_25_BCK_02_IT */
  @Test
  public void testGetProjectListWithoutAccountMapping() throws Exception {
    mockDefaultAuthentication();

    assertError(
        mvc.perform(createGetProjectListRequestBuilder()),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_25_BCK_03_IT */
  @Test
  public void testGetGetProjectListWithoutUserMapping() throws Exception {
    mockDefaultAuthentication();
    mockDefaultAccountMapping();

    assertError(
        mvc.perform(createGetProjectListRequestBuilder()),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.USER_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_25_BCK_01_IT */
  @Test
  public void testGetProjectList() throws Exception {
    mockDefaultAuthentication();
    mockDefaultUserMapping(mockDefaultAccountMapping());

    mockProjectList();

    mvc.perform(createGetProjectListRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(PROJECT_ID_1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(PROJECT_NAME_1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(PROJECT_ID_2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(PROJECT_NAME_2));
  }

  /** Test case: OQCPD_25_BCK_04_IT */
  @Test
  public void testGetProjectListWithServerError() throws Exception {
    mockDefaultAuthentication();
    mockDefaultUserMapping(mockDefaultAccountMapping());

    when(openQualityCheckerProjectApiClient.getPrivateProjects(anyString()))
        .thenThrow(new RuntimeException("OpenQualityChecker API server error"));

    assertError(
        mvc.perform(createGetProjectListRequestBuilder()),
        MockMvcResultMatchers.status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        ErrorCode.OPEN_QUALITY_CHECKER_ERROR);
  }

  private void mockProjectList() {
    final List<OpenQualityCheckerProject> projectList = new ArrayList<>();
    projectList.add(createOpenQualityCheckerProject(PROJECT_ID_1, PROJECT_NAME_1));
    projectList.add(createOpenQualityCheckerProject(PROJECT_ID_2, PROJECT_NAME_2));

    when(openQualityCheckerProjectApiClient.getPrivateProjects(anyString()))
        .thenReturn(projectList);
  }

  private OpenQualityCheckerProject createOpenQualityCheckerProject(
      final Long projectId, final String projectName) {
    final OpenQualityCheckerProject openQualityCheckerProject = new OpenQualityCheckerProject();
    openQualityCheckerProject.setId(projectId);
    openQualityCheckerProject.setName(projectName);

    return openQualityCheckerProject;
  }

  private MockHttpServletRequestBuilder createGetProjectListRequestBuilder() {
    return MockMvcRequestBuilders.get(
            OpenQualityCheckerProjectController.GET_OPEN_QUALITY_CHECKER_PROJECT_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }
}
