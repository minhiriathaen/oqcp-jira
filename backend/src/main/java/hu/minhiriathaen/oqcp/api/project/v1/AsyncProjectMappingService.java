package hu.minhiriathaen.oqcp.api.project.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import java.util.List;
import java.util.Set;

public interface AsyncProjectMappingService {

  void processDeletedProjectMappings(
      final String openQualityCheckerUserToken, final Set<String> openQualityCheckerProjectIds);

  void processCreatedProjectMappings(
      final AtlassianHostUser atlassianHostUser,
      final String openQualityCheckerUserToken,
      final List<ProjectMapping> projectMappings);
}
