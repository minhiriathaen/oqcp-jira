package hu.minhiriathaen.oqcp.openqualitychecker.project;

import hu.minhiriathaen.oqcp.openqualitychecker.OpenQualityCheckerRestTemplate;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerResultWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
public class OpenQualityCheckerProjectRestTemplate extends OpenQualityCheckerRestTemplate {

  public static final String OQC_PROJECTS_URI =
      "/api/projects?privateOnly=true&size=100&page={page}";

  public static final String OQC_PROJECTS_BRANCHED_URI = "/api/project/{projectId}/branches";

  public static final String OQC_SUBSCRIBE_PROJECT_URI =
      "/api/project/{projectId}/subscribeProject";

  public static final String OQC_UNSUBSCRIBE_PROJECT_URI =
      "/api/project/{projectId}/unsubscribeProject";

  public OpenQualityCheckerProjectRestTemplate(final String baseUrl) {
    super(baseUrl);
  }

  public ResponseEntity<OpenQualityCheckerResultWrapper> getPrivateProjects(
      final String userToken, final short page) {
    return authenticatedGet(OQC_PROJECTS_URI, userToken, page);
  }

  public ResponseEntity<OpenQualityCheckerResultWrapper> getBranches(
      final String userToken, final String projectId) {
    return authenticatedGet(OQC_PROJECTS_BRANCHED_URI, userToken, projectId);
  }

  public void subscribeProject(final String userToken, final String projectId) {
    authenticatedPost(OQC_SUBSCRIBE_PROJECT_URI, userToken, projectId);
  }

  public void unsubscribeProject(final String userToken, final String projectId) {
    authenticatedPost(OQC_UNSUBSCRIBE_PROJECT_URI, userToken, projectId);
  }
}
