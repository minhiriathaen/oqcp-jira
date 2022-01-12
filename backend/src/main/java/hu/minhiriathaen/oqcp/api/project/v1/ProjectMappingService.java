package hu.minhiriathaen.oqcp.api.project.v1;

import com.atlassian.connect.spring.AtlassianHostUser;

public interface ProjectMappingService {

  ProjectMappingTransfer getProjectMapping(
      AtlassianHostUser atlassianHostUser, String jiraProjectId);

  void storeProjectMapping(
      AtlassianHostUser atlassianHostUser,
      String jiraProjectId,
      ProjectMappingTransfer projectMappingTransfer);
}
