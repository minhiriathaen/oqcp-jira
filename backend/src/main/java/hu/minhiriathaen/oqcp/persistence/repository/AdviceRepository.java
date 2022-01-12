package hu.minhiriathaen.oqcp.persistence.repository;

import hu.minhiriathaen.oqcp.persistence.entity.Advice;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdviceRepository extends JpaRepository<Advice, Long> {

  Optional<Advice> findByAdviceIdAndGroupOpenQualityCheckerProjectId(
      String adviceId, String openQualityCheckerProjectId);

  Optional<Advice> findByJiraIssueIdAndGroupOpenQualityCheckerProjectIdIn(
      String jiraIssueId, Collection<String> openQualityCheckerProjectIds);
}
