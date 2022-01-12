package hu.minhiriathaen.oqcp.util;

import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.UserMappingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMappingUtil {

  private final UserMappingRepository userMappingRepository;

  public UserMapping findUserMapping(
      final AccountMapping accountMapping, final String atlassianUserAccountId) {

    final Optional<UserMapping> userMapping =
        userMappingRepository.findByAccountMappingAndAtlassianUserAccountId(
            accountMapping, atlassianUserAccountId);

    if (userMapping.isEmpty()) {
      throw new ServiceError(HttpStatus.FORBIDDEN, ErrorCode.USER_MAPPING_NOT_FOUND);
    }
    return userMapping.get();
  }
}
