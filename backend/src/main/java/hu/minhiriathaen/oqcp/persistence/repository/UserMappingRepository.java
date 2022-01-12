package hu.minhiriathaen.oqcp.persistence.repository;

import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMappingRepository extends JpaRepository<UserMapping, Long> {

  Optional<UserMapping> findByAccountMappingAndAtlassianUserAccountId(
      AccountMapping accountMapping, String atlassianUserAccountId);

  Optional<UserMapping> findByAccountMappingAtlassianHostUrlAndAtlassianUserAccountId(
      String atlassianHostUrl, String atlassianUserAccountId);
}
