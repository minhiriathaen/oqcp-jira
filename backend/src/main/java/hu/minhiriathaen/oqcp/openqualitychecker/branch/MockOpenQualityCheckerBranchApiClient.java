package hu.minhiriathaen.oqcp.openqualitychecker.branch;

import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualification;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualificationResult;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.QualificationValue;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockOpenQualityCheckerBranchApiClient implements OpenQualityCheckerBranchApiClient {

  public static final Long PROJECT_ID = 1L;
  public static final String PROJECT_NAME = "Project name";

  public static final String FIRST_BRANCH = "FirstBranch";
  public static final String SECOND_BRANCH = "SecondBranch";

  public static final Long FIRST_BRANCH_ID = 11L;
  public static final Long SECOND_BRANCH_ID = 22L;

  public static final double FIRST_MAINTAINABILITY = 4.1;
  public static final double SECOND_MAINTAINABILITY = 5.5;

  private final ContextHelper contextHelper;

  @Override
  public List<OpenQualityCheckerBranch> getBranches(
      final String userToken, final String projectId) {

    log.info(
        "[{}] MOCK Getting branches for user token '{}' and project id '{} ",
        contextHelper.getUserIdForLog(),
        userToken,
        projectId);

    final OpenQualityCheckerProject project = createProject();

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(project, FIRST_BRANCH_ID, FIRST_BRANCH, FIRST_MAINTAINABILITY));
    branches.add(createBranch(project, SECOND_BRANCH_ID, SECOND_BRANCH, SECOND_MAINTAINABILITY));

    log.info(
        "[{}] MOCK Getting branches for user token '{}' and project id '{}, result: {}",
        contextHelper.getUserIdForLog(),
        userToken,
        projectId,
        branches);

    return branches;
  }

  private OpenQualityCheckerProject createProject() {
    final OpenQualityCheckerProject project = new OpenQualityCheckerProject();
    project.setId(PROJECT_ID);
    project.setName(PROJECT_NAME);
    return project;
  }

  private OpenQualityCheckerBranch createBranch(
      final OpenQualityCheckerProject project,
      final long branchId,
      final String branchName,
      final Double maintainability) {

    final OpenQualityCheckerBranch branch = new OpenQualityCheckerBranch();
    branch.setId(branchId);
    branch.setName(branchName);
    branch.setProject(project);
    branch.setQualificationResult(createQualificationResult(maintainability));

    return branch;
  }

  private OpenQualityCheckerQualificationResult createQualificationResult(
      final Double maintainability) {
    final OpenQualityCheckerQualificationResult openQualityCheckerQualificationResult =
        new OpenQualityCheckerQualificationResult();
    openQualityCheckerQualificationResult.setQualification(createQualification(maintainability));
    return openQualityCheckerQualificationResult;
  }

  private OpenQualityCheckerQualification createQualification(final Double maintainability) {
    final OpenQualityCheckerQualification qualification = new OpenQualityCheckerQualification();
    qualification.setMaintainability(new QualificationValue());
    qualification.getMaintainability().setValue(maintainability);
    return qualification;
  }
}
