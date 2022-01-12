package hu.minhiriathaen.oqcp.openqualitychecker.branch;

import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import java.util.List;

public interface OpenQualityCheckerBranchApiClient {

  List<OpenQualityCheckerBranch> getBranches(String userToken, String projectId);
}
