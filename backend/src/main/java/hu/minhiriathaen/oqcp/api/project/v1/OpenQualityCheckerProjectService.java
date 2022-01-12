package hu.minhiriathaen.oqcp.api.project.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import java.util.List;

public interface OpenQualityCheckerProjectService {

  List<OpenQualityCheckerProjectTransfer> getProjects(final AtlassianHostUser atlassianHostUser);
}
