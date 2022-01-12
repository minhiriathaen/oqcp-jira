package hu.minhiriathaen.oqcp.persistence.repository;

import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMappingRepository extends JpaRepository<ProjectMapping, Long> {

  List<ProjectMapping> findByOpenQualityCheckerProjectIdInAndJiraProjectIdNot(
      Collection<String> openQualityCheckerProjectIds, String jiraProjectId);

  List<ProjectMapping> findByAccountMappingAndJiraProjectId(
      AccountMapping accountMapping, String jiraProjectId);

  Long deleteByAccountMappingAndOpenQualityCheckerProjectIdIn(
      AccountMapping accountMapping, Collection<String> openQualityCheckerProjectIds);

  List<ProjectMapping> findByAccountMapping(AccountMapping accountMapping);

  List<ProjectMapping> findByAccountMappingAndOpenQualityCheckerProjectIdIn(
      AccountMapping accountMapping, Collection<String> openQualityCheckerProjectIds);

  Optional<ProjectMapping> findByOpenQualityCheckerProjectId(String openQualityCheckerProjectId);
}
