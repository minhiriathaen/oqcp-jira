package hu.minhiriathaen.oqcp.api.maintainability.v1;

import java.util.List;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
public class ProjectMaintainabilityTransfer {

  private String id; // NOPMD

  private String name;

  private String mainBranchName;

  private Double maintainabilityIndex;

  private List<BranchMaintainabilityTransfer> branches;
}
