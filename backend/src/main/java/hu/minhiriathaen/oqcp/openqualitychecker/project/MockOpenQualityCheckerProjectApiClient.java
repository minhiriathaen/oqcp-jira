package hu.minhiriathaen.oqcp.openqualitychecker.project;

import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class MockOpenQualityCheckerProjectApiClient implements OpenQualityCheckerProjectApiClient {

  public static final Long PROJECT_ID_1 = 1L;
  public static final String PROJECT_NAME_1 = "Project name 1";

  public static final Long PROJECT_ID_2 = 2L;
  public static final String PROJECT_NAME_2 = "Project name 2";

  private final ContextHelper contextHelper;

  @Override
  public List<OpenQualityCheckerProject> getPrivateProjects(final String userToken) {
    log.info("[{}] MOCK getPrivateProjects '{}'", contextHelper.getUserIdForLog(), userToken);

    final List<OpenQualityCheckerProject> projectList = new ArrayList<>();
    projectList.add(createOpenQualityCheckerProject(PROJECT_ID_1, PROJECT_NAME_1));
    projectList.add(createOpenQualityCheckerProject(PROJECT_ID_2, PROJECT_NAME_2));
    return projectList;
  }

  private OpenQualityCheckerProject createOpenQualityCheckerProject(
      final Long projectId, final String projectName) {
    final OpenQualityCheckerProject openQualityCheckerProject = new OpenQualityCheckerProject();
    openQualityCheckerProject.setId(projectId);
    openQualityCheckerProject.setName(projectName);

    return openQualityCheckerProject;
  }

  @Override
  public void subscribeToProject(final String userToken, final String openQualityCheckerProjectId) {
    log.info(
        "[{}] MOCK Subscribe to project for user token '{}' and project id '{}'",
        contextHelper.getUserIdForLog(),
        userToken,
        openQualityCheckerProjectId);
  }

  @Override
  public void unsubscribeFromProject(
      final String userToken, final String openQualityCheckerProjectId) {
    log.info(
        "[{}] MOCK Unsubscribe to project for user token '{}' and project id '{}'",
        contextHelper.getUserIdForLog(),
        userToken,
        openQualityCheckerProjectId);
  }
}
