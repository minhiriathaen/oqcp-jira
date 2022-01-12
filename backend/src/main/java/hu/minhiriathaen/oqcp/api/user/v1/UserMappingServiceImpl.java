package hu.minhiriathaen.oqcp.api.user.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.BadRequestError;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.token.OpenQualityCheckerTokenValidator;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.UserMappingRepository;
import hu.minhiriathaen.oqcp.util.AccountMappingUtil;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import hu.minhiriathaen.oqcp.util.HostUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMappingServiceImpl implements UserMappingService {

  private final AccountMappingUtil accountMappingUtil;

  private final UserMappingRepository userMappingRepository;

  private final ContextHelper contextHelper;

  private final OpenQualityCheckerTokenValidator openQualityCheckerTokenValidator;

  @Override
  public UserMappingTransfer getUserMapping(final AtlassianHostUser atlassianHostUser) {

    final String atlassianUserAccountId = HostUtil.unwrapAccountId(atlassianHostUser);

    final AccountMapping accountMapping = accountMappingUtil.findAccountMapping(atlassianHostUser);

    final Optional<UserMapping> userMapping =
        userMappingRepository.findByAccountMappingAndAtlassianUserAccountId(
            accountMapping, atlassianUserAccountId);

    final UserMappingTransfer transfer = new UserMappingTransfer();

    userMapping.ifPresentOrElse(
        mapping -> {
          log.info("[{}] Item found: {}", contextHelper.getUserIdForLog(), mapping);

          transfer.setOpenQualityCheckerUserToken(mapping.getOpenQualityCheckerUserToken());
        },
        () -> log.info("[{}] No UserMapping found", contextHelper.getUserIdForLog()));

    return transfer;
  }

  @Override
  public void storeUserMapping(
      final AtlassianHostUser atlassianHostUser, final UserMappingTransfer transfer) {

    if (Strings.isBlank(transfer.getOpenQualityCheckerUserToken())) {
      throw new BadRequestError(ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_REQUIRED);
    }

    final AccountMapping accountMapping = accountMappingUtil.findAccountMapping(atlassianHostUser);

    validateOpenQualityCheckerToken(transfer, accountMapping);

    final String atlassianUserAccountId = HostUtil.unwrapAccountId(atlassianHostUser);

    final Optional<UserMapping> userMapping =
        userMappingRepository.findByAccountMappingAndAtlassianUserAccountId(
            accountMapping, atlassianUserAccountId);

    if (userMapping.isPresent()) {
      updateUserMapping(userMapping.get(), transfer.getOpenQualityCheckerUserToken());
    } else {
      saveUserMapping(
          accountMapping, transfer.getOpenQualityCheckerUserToken(), atlassianUserAccountId);
    }
  }

  private void validateOpenQualityCheckerToken(
      final UserMappingTransfer transfer, final AccountMapping accountMapping) {
    final boolean tokenValid =
        openQualityCheckerTokenValidator.isUserTokenValidForAccountName(
            transfer.getOpenQualityCheckerUserToken(),
            accountMapping.getOpenQualityCheckerAccountName());

    if (!tokenValid) {
      throw new ServiceError(
          HttpStatus.FORBIDDEN, ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_VERIFICATION_FAILED);
    }
  }

  private void saveUserMapping(
      final AccountMapping accountMapping,
      final String openQualityCheckerUserToken,
      final String atlassianUserAccountId) {

    final UserMapping userMapping = new UserMapping();
    userMapping.setAccountMapping(accountMapping);
    userMapping.setOpenQualityCheckerUserToken(openQualityCheckerUserToken);
    userMapping.setAtlassianUserAccountId(atlassianUserAccountId);

    userMappingRepository.save(userMapping);

    log.info("[{}] New item saved: {}", contextHelper.getUserIdForLog(), userMapping);
  }

  private void updateUserMapping(
      final UserMapping userMapping, final String openQualityCheckerUserToken) {
    final String originalToken = userMapping.getOpenQualityCheckerUserToken();

    userMapping.setOpenQualityCheckerUserToken(openQualityCheckerUserToken);

    userMappingRepository.save(userMapping);

    log.info(
        "[{}] Open Quality Checker user token updated from '{}' to '{}'",
        contextHelper.getUserIdForLog(),
        originalToken,
        openQualityCheckerUserToken);
  }
}
