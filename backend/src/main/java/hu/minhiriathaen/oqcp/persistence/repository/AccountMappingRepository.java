package hu.minhiriathaen.oqcp.persistence.repository;

import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountMappingRepository extends JpaRepository<AccountMapping, Long> {

  Optional<AccountMapping> findByAtlassianHostUrl(String atlassianHostUrl);

  Optional<AccountMapping> findByOpenQualityCheckerAccountName(
      String openQualityCheckerAccountName);
}
