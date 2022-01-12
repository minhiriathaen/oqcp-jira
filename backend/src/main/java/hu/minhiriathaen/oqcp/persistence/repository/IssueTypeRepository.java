package hu.minhiriathaen.oqcp.persistence.repository;

import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueTypeRepository extends JpaRepository<IssueType, Long> {

  Optional<IssueType> findByAtlassianHostUrl(String atlassianHostUrl);
}
