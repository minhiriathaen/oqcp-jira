package hu.minhiriathaen.oqcp.api.maintainability.v1;

import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerQualificationResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintainabilityConverter {

  public static final String MASTER_BRANCH_NAME = "master";

  public ProjectMaintainabilityTransfer convert(final List<OpenQualityCheckerBranch> branches) {

    if (null == branches || branches.isEmpty()) {
      return null;
    }

    final OpenQualityCheckerBranch firstBranch = findFirstBranch(branches);

    final Optional<OpenQualityCheckerBranch> masterBranch = findMasterBranch(branches);

    final ProjectMaintainabilityTransfer transfer =
        createProjectMaintainabilityTransfer(masterBranch.orElse(firstBranch));

    transfer.setBranches(
        branches.stream()
            .map(this::createBranchMaintainabilityTransfer)
            .collect(Collectors.toList()));

    return transfer;
  }

  private Optional<OpenQualityCheckerBranch> findMasterBranch(
      final List<OpenQualityCheckerBranch> branches) {
    return branches.stream()
        .filter(branch -> MASTER_BRANCH_NAME.equals(branch.getName()))
        .findFirst();
  }

  private OpenQualityCheckerBranch findFirstBranch(final List<OpenQualityCheckerBranch> branches) {
    final OpenQualityCheckerBranch firstBranch = branches.get(0);

    if (null == firstBranch.getProject() || null == firstBranch.getProject().getId()) {
      throw new IllegalArgumentException("First branch has no Project or Project has no Id");
    }

    return firstBranch;
  }

  private BranchMaintainabilityTransfer createBranchMaintainabilityTransfer(
      final OpenQualityCheckerBranch branch) {

    if (null == branch.getId()) {
      throw new IllegalArgumentException(
          "Branch has no id, unable to create BranchMaintainabilityTransfer");
    }

    final OpenQualityCheckerQualificationResult qualificationResult =
        branch.getQualificationResult();

    final BranchMaintainabilityTransfer transfer = new BranchMaintainabilityTransfer();

    transfer.setId(branch.getId().toString());
    transfer.setName(branch.getName());

    if (null == qualificationResult
        || null == qualificationResult.getQualification()
        || null == qualificationResult.getQualification().getMaintainability()) {

      log.warn(
          "[{} - {}] Unable to convert maintainability index for the branch",
          branch.getProject().getName(),
          branch.getName());

      transfer.setMaintainabilityIndex(null);
    } else {
      transfer.setMaintainabilityIndex(
          qualificationResult.getQualification().getMaintainability().getValue());

      log.info(
          "[{} - {}] Branch maintainability index: {}",
          branch.getProject().getName(),
          branch.getName(),
          transfer.getMaintainabilityIndex());
    }

    return transfer;
  }

  private ProjectMaintainabilityTransfer createProjectMaintainabilityTransfer(
      final OpenQualityCheckerBranch branch) {

    final OpenQualityCheckerQualificationResult qualificationResult =
        branch.getQualificationResult();

    final ProjectMaintainabilityTransfer transfer = new ProjectMaintainabilityTransfer();

    transfer.setId(branch.getProject().getId().toString());
    transfer.setName(branch.getProject().getName());

    transfer.setMainBranchName(branch.getName());

    if (null == qualificationResult
        || null == qualificationResult.getQualification()
        || null == qualificationResult.getQualification().getMaintainability()) {

      log.warn(
          "[{} - {}] Unable to convert maintainability index for the project",
          branch.getProject().getName(),
          branch.getName());

      transfer.setMaintainabilityIndex(null);
    } else {
      transfer.setMaintainabilityIndex(
          qualificationResult.getQualification().getMaintainability().getValue());

      log.info(
          "[{} - {}] Project maintainability index: {}",
          branch.getProject().getName(),
          branch.getName(),
          transfer.getMaintainabilityIndex());
    }

    return transfer;
  }
}
