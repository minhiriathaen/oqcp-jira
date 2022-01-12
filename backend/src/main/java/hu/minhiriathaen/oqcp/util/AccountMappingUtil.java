package hu.minhiriathaen.oqcp.util;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.repository.AccountMappingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMappingUtil {

  private final AccountMappingRepository accountMappingRepository;

  public AccountMapping findAccountMapping(final AtlassianHostUser atlassianHostUser) {
    final AtlassianHost atlassianHost = HostUtil.unwrapHost(atlassianHostUser);
    final Optional<AccountMapping> accountMapping =
        accountMappingRepository.findByAtlassianHostUrl(atlassianHost.getBaseUrl());

    if (accountMapping.isEmpty()) {
      throw new ServiceError(HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
    }
    return accountMapping.get();
  }
}
