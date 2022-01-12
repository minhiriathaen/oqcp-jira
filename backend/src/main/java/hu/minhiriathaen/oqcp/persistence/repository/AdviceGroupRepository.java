package hu.minhiriathaen.oqcp.persistence.repository;

import hu.minhiriathaen.oqcp.persistence.entity.AdviceGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdviceGroupRepository extends JpaRepository<AdviceGroup, Long> {

  Optional<AdviceGroup>
      findTopByOpenQualityCheckerProjectIdAndBranchNameAndContributorOrderByCreatedAtDesc(
          String openQualityCheckerProjectId, String branchName, String contributor);
}
