package hu.minhiriathaen.oqcp.api.account.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.ContextJwt;
import hu.minhiriathaen.oqcp.util.HostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountMappingController {

  public static final String GET_ACCOUNT_MAPPING_URL = "/v1/accountmappings/current";

  public static final String STORE_ACCOUNT_MAPPING_URL = "/v1/accountmappings/current";

  private final AccountMappingService accountMappingService;

  @ContextJwt
  @GetMapping(GET_ACCOUNT_MAPPING_URL)
  public AccountMappingTransfer getAccountMapping(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser) {
    log.info("getAccountMapping: {}", atlassianHostUser);

    return accountMappingService.getAccountMapping(HostUtil.unwrapHost(atlassianHostUser));
  }

  @ContextJwt
  @PutMapping(STORE_ACCOUNT_MAPPING_URL)
  public void storeAccountMapping(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser,
      @RequestBody final AccountMappingTransfer accountMappingTransfer) {
    log.info("storeAccountMapping: {} - {}", atlassianHostUser, accountMappingTransfer);

    accountMappingService.storeAccountMapping(
        HostUtil.unwrapHost(atlassianHostUser), accountMappingTransfer);
  }
}
