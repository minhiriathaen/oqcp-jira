package hu.minhiriathaen.oqcp.openqualitychecker.project;

import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import java.util.List;

public interface OpenQualityCheckerProjectApiClient {
  List<OpenQualityCheckerProject> getPrivateProjects(final String userToken);

  void subscribeToProject(String userToken, String openQualityCheckerProjectId);

  void unsubscribeFromProject(String userToken, String openQualityCheckerProjectId);
}
