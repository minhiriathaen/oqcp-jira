package hu.minhiriathaen.oqcp.api.maintainability.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.failBecauseExceptionWasNotThrown;

import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualification;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualificationResult;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.QualificationValue;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MaintainabilityConverterTest {

  public static final String PROJECT_NAME = "Project name";
  public static final String FIRST_BRANCH = "FirstBranch";
  public static final String SECOND_BRANCH = "SecondBranch";
  public static final Long FIRST_BRANCH_ID = 11L;
  public static final Long SECOND_BRANCH_ID = 22L;
  public static final Long PROJECT_ID = 1L;
  public static final double FIRST_MAINTAINABILITY = 4.1;
  public static final double SECOND_MAINTAINABILITY = 5.5;

  @Autowired private transient MaintainabilityConverter maintainabilityConverter;

  @Test
  public void testConvertWithNull() {
    final ProjectMaintainabilityTransfer transfer = maintainabilityConverter.convert(null);

    assertThat(transfer).isNull();
  }

  @Test
  public void testConvertWithEmpty() {
    final ProjectMaintainabilityTransfer transfer =
        maintainabilityConverter.convert(Collections.emptyList());

    assertThat(transfer).isNull();
  }

  @Test
  public void testConvertWithNullProject() {
    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(new OpenQualityCheckerBranch());

    try {
      maintainabilityConverter.convert(branches);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("First branch has no Project or Project has no Id");
    }
  }

  @Test
  public void testConvertWithEmptyProject() {
    final OpenQualityCheckerBranch branch = new OpenQualityCheckerBranch();
    branch.setProject(new OpenQualityCheckerProject());

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(branch);

    try {
      maintainabilityConverter.convert(branches);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("First branch has no Project or Project has no Id");
    }
  }

  @Test
  public void testConvertWithoutQualificationResult() {
    final OpenQualityCheckerBranch branch = new OpenQualityCheckerBranch();
    branch.setProject(createProject());
    branch.setId(FIRST_BRANCH_ID);
    branch.setName(FIRST_BRANCH);

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(branch);

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isNull();
    assertThat(result.getBranches().get(0).getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(result.getBranches().get(0).getName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getBranches().get(0).getMaintainabilityIndex()).isNull();
  }

  @Test
  public void testConvertWithQualificationResult() {
    final OpenQualityCheckerBranch branch = new OpenQualityCheckerBranch();
    branch.setProject(createProject());
    branch.setId(FIRST_BRANCH_ID);
    branch.setName(FIRST_BRANCH);
    branch.setQualificationResult(new OpenQualityCheckerQualificationResult());

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(branch);

    maintainabilityConverter.convert(branches);

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isNull();
    assertThat(result.getBranches().get(0).getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(result.getBranches().get(0).getName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getBranches().get(0).getMaintainabilityIndex()).isNull();
  }

  @Test
  public void testConvertWithNullOpenQualityCheckerQualification() {
    final OpenQualityCheckerProject project = createProject();

    final OpenQualityCheckerBranch secondBranch = new OpenQualityCheckerBranch();
    secondBranch.setProject(project);
    secondBranch.setId(SECOND_BRANCH_ID);
    secondBranch.setName(SECOND_BRANCH);

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(project, FIRST_BRANCH_ID, FIRST_BRANCH, FIRST_MAINTAINABILITY));
    branches.add(secondBranch);

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(0).getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(result.getBranches().get(0).getName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getBranches().get(0).getMaintainabilityIndex())
        .isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(1).getId()).isEqualTo(SECOND_BRANCH_ID.toString());
    assertThat(result.getBranches().get(1).getName()).isEqualTo(SECOND_BRANCH);
    assertThat(result.getBranches().get(1).getMaintainabilityIndex()).isNull();
  }

  @Test
  public void testConvertWithNullQualification() {
    final OpenQualityCheckerProject project = createProject();

    final OpenQualityCheckerBranch secondBranch = new OpenQualityCheckerBranch();
    secondBranch.setProject(project);
    secondBranch.setId(SECOND_BRANCH_ID);
    secondBranch.setName(SECOND_BRANCH);
    secondBranch.setQualificationResult(new OpenQualityCheckerQualificationResult());

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(project, FIRST_BRANCH_ID, FIRST_BRANCH, FIRST_MAINTAINABILITY));
    branches.add(secondBranch);

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(0).getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(result.getBranches().get(0).getName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getBranches().get(0).getMaintainabilityIndex())
        .isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(1).getId()).isEqualTo(SECOND_BRANCH_ID.toString());
    assertThat(result.getBranches().get(1).getName()).isEqualTo(SECOND_BRANCH);
    assertThat(result.getBranches().get(1).getMaintainabilityIndex()).isNull();
  }

  @Test
  public void testConvertWithNullMaintainability() {
    final OpenQualityCheckerProject project = createProject();

    final OpenQualityCheckerBranch secondBranch = new OpenQualityCheckerBranch();
    secondBranch.setProject(project);
    secondBranch.setId(SECOND_BRANCH_ID);
    secondBranch.setName(SECOND_BRANCH);
    secondBranch.setQualificationResult(new OpenQualityCheckerQualificationResult());
    secondBranch.getQualificationResult().setQualification(new OpenQualityCheckerQualification());

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(project, FIRST_BRANCH_ID, FIRST_BRANCH, FIRST_MAINTAINABILITY));
    branches.add(secondBranch);

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(0).getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(result.getBranches().get(0).getName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getBranches().get(0).getMaintainabilityIndex())
        .isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(1).getId()).isEqualTo(SECOND_BRANCH_ID.toString());
    assertThat(result.getBranches().get(1).getName()).isEqualTo(SECOND_BRANCH);
    assertThat(result.getBranches().get(1).getMaintainabilityIndex()).isNull();
  }

  @Test
  public void testConvertWithNullMaintainabilityValue() {
    final OpenQualityCheckerProject project = createProject();

    final OpenQualityCheckerBranch secondBranch = new OpenQualityCheckerBranch();
    secondBranch.setProject(project);
    secondBranch.setId(SECOND_BRANCH_ID);
    secondBranch.setName(SECOND_BRANCH);
    secondBranch.setQualificationResult(new OpenQualityCheckerQualificationResult());
    secondBranch.getQualificationResult().setQualification(new OpenQualityCheckerQualification());
    secondBranch
        .getQualificationResult()
        .getQualification()
        .setMaintainability(new QualificationValue());

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(project, FIRST_BRANCH_ID, FIRST_BRANCH, FIRST_MAINTAINABILITY));
    branches.add(secondBranch);

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(0).getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(result.getBranches().get(0).getName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getBranches().get(0).getMaintainabilityIndex())
        .isEqualTo(FIRST_MAINTAINABILITY);
    assertThat(result.getBranches().get(1).getId()).isEqualTo(SECOND_BRANCH_ID.toString());
    assertThat(result.getBranches().get(1).getName()).isEqualTo(SECOND_BRANCH);
    assertThat(result.getBranches().get(1).getMaintainabilityIndex()).isNull();
  }

  @Test
  public void testConvertWithNullBranchId() {
    final OpenQualityCheckerBranch firstBranch = new OpenQualityCheckerBranch();
    firstBranch.setProject(createProject());
    firstBranch.setQualificationResult(createQualificationResult(null));

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(firstBranch);

    try {
      maintainabilityConverter.convert(branches);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage())
          .isEqualTo("Branch has no id, unable to create BranchMaintainabilityTransfer");
    }
  }

  @Test
  public void testConvertWithBranchId() {
    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(createProject(), FIRST_BRANCH_ID, FIRST_BRANCH, null));

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isNull();
    assertThat(result.getBranches().get(0).getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(result.getBranches().get(0).getName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getBranches().get(0).getMaintainabilityIndex()).isNull();
  }

  @Test
  public void testConvertWithMasterBranch() {
    final OpenQualityCheckerProject project = createProject();

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(project, FIRST_BRANCH_ID, FIRST_BRANCH, FIRST_MAINTAINABILITY));
    branches.add(
        createBranch(
            project,
            SECOND_BRANCH_ID,
            MaintainabilityConverter.MASTER_BRANCH_NAME,
            SECOND_MAINTAINABILITY));

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(MaintainabilityConverter.MASTER_BRANCH_NAME);
    assertThat(result.getMaintainabilityIndex()).isEqualTo(SECOND_MAINTAINABILITY);

    final BranchMaintainabilityTransfer firstBranch = result.getBranches().get(0);
    assertThat(firstBranch.getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(firstBranch.getName()).isEqualTo(FIRST_BRANCH);
    assertThat(firstBranch.getMaintainabilityIndex()).isEqualTo(FIRST_MAINTAINABILITY);

    final BranchMaintainabilityTransfer secondBranch = result.getBranches().get(1);
    assertThat(secondBranch.getId()).isEqualTo(SECOND_BRANCH_ID.toString());
    assertThat(secondBranch.getName()).isEqualTo(MaintainabilityConverter.MASTER_BRANCH_NAME);
    assertThat(secondBranch.getMaintainabilityIndex()).isEqualTo(SECOND_MAINTAINABILITY);
  }

  @Test
  public void testConvertWithoutMasterBranch() {
    final OpenQualityCheckerProject project = createProject();

    final ArrayList<OpenQualityCheckerBranch> branches = new ArrayList<>();
    branches.add(createBranch(project, FIRST_BRANCH_ID, FIRST_BRANCH, FIRST_MAINTAINABILITY));
    branches.add(createBranch(project, SECOND_BRANCH_ID, SECOND_BRANCH, SECOND_MAINTAINABILITY));

    final ProjectMaintainabilityTransfer result = maintainabilityConverter.convert(branches);

    assertThat(result.getId()).isEqualTo(PROJECT_ID.toString());
    assertThat(result.getName()).isEqualTo(PROJECT_NAME);
    assertThat(result.getMainBranchName()).isEqualTo(FIRST_BRANCH);
    assertThat(result.getMaintainabilityIndex()).isEqualTo(FIRST_MAINTAINABILITY);

    final BranchMaintainabilityTransfer firstBranch = result.getBranches().get(0);
    assertThat(firstBranch.getId()).isEqualTo(FIRST_BRANCH_ID.toString());
    assertThat(firstBranch.getName()).isEqualTo(FIRST_BRANCH);
    assertThat(firstBranch.getMaintainabilityIndex()).isEqualTo(FIRST_MAINTAINABILITY);

    final BranchMaintainabilityTransfer secondBranch = result.getBranches().get(1);
    assertThat(secondBranch.getId()).isEqualTo(SECOND_BRANCH_ID.toString());
    assertThat(secondBranch.getName()).isEqualTo(SECOND_BRANCH);
    assertThat(secondBranch.getMaintainabilityIndex()).isEqualTo(SECOND_MAINTAINABILITY);
  }

  private OpenQualityCheckerBranch createBranch(
      final OpenQualityCheckerProject project,
      final long branchId,
      final String branchName,
      final Double maintainability) {

    final OpenQualityCheckerBranch firstBranch = new OpenQualityCheckerBranch();
    firstBranch.setId(branchId);
    firstBranch.setName(branchName);
    firstBranch.setProject(project);
    firstBranch.setQualificationResult(createQualificationResult(maintainability));

    return firstBranch;
  }

  private OpenQualityCheckerQualification createQualification(final Double maintainability) {
    final OpenQualityCheckerQualification qualification = new OpenQualityCheckerQualification();
    qualification.setMaintainability(new QualificationValue());
    qualification.getMaintainability().setValue(maintainability);
    return qualification;
  }

  private OpenQualityCheckerQualificationResult createQualificationResult(
      final Double maintainability) {
    final OpenQualityCheckerQualificationResult openQualityCheckerQualificationResult =
        new OpenQualityCheckerQualificationResult();
    openQualityCheckerQualificationResult.setQualification(createQualification(maintainability));
    return openQualityCheckerQualificationResult;
  }

  private OpenQualityCheckerProject createProject() {
    final OpenQualityCheckerProject project = new OpenQualityCheckerProject();
    project.setId(PROJECT_ID);
    project.setName(PROJECT_NAME);
    return project;
  }
}
