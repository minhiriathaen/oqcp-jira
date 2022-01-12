package hu.minhiriathaen.oqcp.api.account.v1;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.exception.BadRequestError;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.repository.AccountMappingRepository;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountMappingServiceImpl implements AccountMappingService {

  private final AccountMappingRepository accountMappingRepository;

  private final ContextHelper contextHelper;

  private final AsyncIssueTypeService asyncIssueTypeService;

  @Override
  public AccountMappingTransfer getAccountMapping(final AtlassianHost atlassianHost) {

    final Optional<AccountMapping> accountMapping =
        accountMappingRepository.findByAtlassianHostUrl(atlassianHost.getBaseUrl());

    final AccountMappingTransfer transfer = new AccountMappingTransfer();

    accountMapping.ifPresent(
        mapping ->
            transfer.setOpenQualityCheckerAccountName(mapping.getOpenQualityCheckerAccountName()));

    return transfer;
  }

  @Override
  public void storeAccountMapping(
      final AtlassianHost atlassianHost, final AccountMappingTransfer transfer) {

    if (Strings.isBlank(transfer.getOpenQualityCheckerAccountName())) {
      throw new BadRequestError(ErrorCode.OPEN_QUALITY_CHECKER_ACCOUNT_NAME_REQUIRED);
    }

    final Optional<AccountMapping> usedAccountMapping =
        accountMappingRepository.findByOpenQualityCheckerAccountName(
            transfer.getOpenQualityCheckerAccountName());

    if (usedAccountMapping.isPresent()
        && !usedAccountMapping.get().getAtlassianHostUrl().equals(atlassianHost.getBaseUrl())) {
      throw new ServiceError(
          HttpStatus.CONFLICT, ErrorCode.OPEN_QUALITY_CHECKER_ACCOUNT_ALREADY_MAPPED);
    }

    final Optional<AccountMapping> accountMapping =
        accountMappingRepository.findByAtlassianHostUrl(atlassianHost.getBaseUrl());

    if (accountMapping.isPresent()) {
      updateMapping(accountMapping.get(), transfer.getOpenQualityCheckerAccountName());
    } else {
      saveMapping(atlassianHost.getBaseUrl(), transfer.getOpenQualityCheckerAccountName());
    }

    asyncIssueTypeService.createIssueTypes(atlassianHost);
  }

  private void saveMapping(
      final String atlassianHostUrl, final String openQualityCheckerAccountName) {
    final AccountMapping newMapping = new AccountMapping();
    newMapping.setAtlassianHostUrl(atlassianHostUrl);
    newMapping.setOpenQualityCheckerAccountName(openQualityCheckerAccountName);

    accountMappingRepository.save(newMapping);

    log.info("[{}] New item saved: {}", contextHelper.getUserIdForLog(), newMapping);
  }

  private void updateMapping(
      final AccountMapping accountMapping, final String openQualityCheckerAccountName) {
    final String originalAccountName = accountMapping.getOpenQualityCheckerAccountName();

    accountMapping.setOpenQualityCheckerAccountName(openQualityCheckerAccountName);

    accountMappingRepository.save(accountMapping);

    log.info(
        "[{}] Account name updated from '{}' to '{}'",
        contextHelper.getUserIdForLog(),
        originalAccountName,
        openQualityCheckerAccountName);
  }
}
