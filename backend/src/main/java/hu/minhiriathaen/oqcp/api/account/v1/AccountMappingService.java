package hu.minhiriathaen.oqcp.api.account.v1;

import com.atlassian.connect.spring.AtlassianHost;

public interface AccountMappingService {

  AccountMappingTransfer getAccountMapping(AtlassianHost atlassianHost);

  void storeAccountMapping(
      AtlassianHost atlassianHost, final AccountMappingTransfer accountMappingTransfer);
}
