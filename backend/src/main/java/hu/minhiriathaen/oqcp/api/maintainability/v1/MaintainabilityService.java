package hu.minhiriathaen.oqcp.api.maintainability.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import java.util.List;

public interface MaintainabilityService {

  List<ProjectMaintainabilityTransfer> getMaintainabilities(AtlassianHostUser atlassianHostUser);
}
