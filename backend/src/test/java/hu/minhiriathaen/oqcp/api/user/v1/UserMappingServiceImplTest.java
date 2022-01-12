package hu.minhiriathaen.oqcp.api.user.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.token.OpenQualityCheckerTokenValidator;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

@Slf4j
@SpringBootTest
public class UserMappingServiceImplTest extends ServiceTestBase {

  private static final String NEW_OPEN_QUALITY_CHECKER_USER_TOKEN =
      "NEW_OPEN_QUALITY_CHECKER_USER_TOKEN";

  @Autowired private transient UserMappingServiceImpl userMappingService;

  @MockBean private transient OpenQualityCheckerTokenValidator openQualityCheckerTokenValidator;

  @Test
  public void testGetUserMappingWithNullHostUser() {
    try {
      userMappingService.getUserMapping(null);

      fail("No IllegalArgumentException is thrown for wrong parameter");
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host user is required");
    }
  }

  @Test
  public void testGetUserMappingWithNullBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(null);

    try {
      userMappingService.getUserMapping(hostUser);

      fail("No IllegalArgumentException is thrown for wrong parameter");
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host base URL is required");
    }
  }

  @Test
  public void testGetUserMappingWithEmptyBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(StringUtils.EMPTY);

    try {
      userMappingService.getUserMapping(hostUser);

      fail("No IllegalArgumentException is thrown for wrong parameter");
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host base URL is required");
    }
  }

  @Test
  public void testGetUserMappingNotExistingAccountMapping() {
    mockOtherAccountMapping();

    final AtlassianHostUser hostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    try {
      userMappingService.getUserMapping(hostUser);
    } catch (final ServiceError error) {
      assertServiceError(error, HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
    }
  }

  /** Test case: OQCPD_24_BCK_01_UT */
  @Test
  public void testGetUserMappingExistingAccountMapping() {
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    final AtlassianHostUser hostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    try {
      userMappingService.getUserMapping(hostUser);
    } catch (final ServiceError error) {
      fail("AccountMapping not found but it should");
    }
  }

  /** Test case: OQCPD_24_BCK_02_UT */
  @Test
  public void testGetUserMappingExistingUserMapping() {
    mockUserMapping(mockDefaultAccountMapping(), OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    final UserMappingTransfer userMapping =
        userMappingService.getUserMapping(createAtlassianHostUser(AtlassianUtil.BASE_URL));

    assertThat(userMapping.getOpenQualityCheckerUserToken()).isEqualTo(OQC_USER_TOKEN);
  }

  /** Test case: OQCPD_24_BCK_03_UT */
  @Test
  public void testGetUserMappingNotExistingUserMapping() {
    mockDefaultAccountMapping();
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    final UserMappingTransfer userMapping =
        userMappingService.getUserMapping(createAtlassianHostUser(AtlassianUtil.BASE_URL));

    assertThat(userMapping.getOpenQualityCheckerUserToken()).isNull();
  }

  /** Test case: OQCPD_24_BCK_04_UT */
  @Test
  public void testStoreUserMappingWithNotEmptyToken() {
    mockDefaultAccountMapping();
    mockOtherAccountMapping();
    mockOpenQualityCheckerTokenValidator(OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, true);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);
    final UserMappingTransfer userMappingTransfer = new UserMappingTransfer();
    userMappingTransfer.setOpenQualityCheckerUserToken(OQC_USER_TOKEN);

    userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);

    verify(accountMappingRepository).findByAtlassianHostUrl(AtlassianUtil.BASE_URL);
  }

  /** Test case: OQCPD_24_BCK_05_UT */
  @Test
  public void testStoreUserMappingWithoutAccountMapping() {
    mockOtherAccountMapping();

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);
    final UserMappingTransfer userMappingTransfer = new UserMappingTransfer();
    userMappingTransfer.setOpenQualityCheckerUserToken(OQC_USER_TOKEN);

    try {
      userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);

      fail("Process should thrown ServiceError");
    } catch (final ServiceError error) {
      assertServiceError(error, HttpStatus.FORBIDDEN, ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
    }
  }

  /** Test case: OQCPD_24_BCK_06_UT */
  @Test
  public void testStoreUserMappingWithExistingAccountMapping() {
    mockDefaultAccountMapping();
    mockOtherAccountMapping();
    mockOpenQualityCheckerTokenValidator(OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, true);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);
    final UserMappingTransfer userMappingTransfer = new UserMappingTransfer();
    userMappingTransfer.setOpenQualityCheckerUserToken(OQC_USER_TOKEN);

    userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);

    verify(openQualityCheckerTokenValidator)
        .isUserTokenValidForAccountName(OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME);
  }

  /** Test case: OQCPD_24_BCK_07_UT */
  @Test
  public void testStoreUserMappingWithInvalidToken() {
    mockDefaultAccountMapping();
    mockOtherAccountMapping();
    mockOpenQualityCheckerTokenValidator(OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, false);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);
    final UserMappingTransfer userMappingTransfer = new UserMappingTransfer();
    userMappingTransfer.setOpenQualityCheckerUserToken(OQC_USER_TOKEN);

    try {
      userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);

      fail("Process should thrown ServiceError");
    } catch (final ServiceError error) {
      assertServiceError(
          error,
          HttpStatus.FORBIDDEN,
          ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_VERIFICATION_FAILED);
    }
  }

  /** Test case: OQCPD_24_BCK_08_UT */
  @Test
  public void testStoreUserMappingWithValidToken() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockOtherAccountMapping();
    mockOpenQualityCheckerTokenValidator(OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, true);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);
    final UserMappingTransfer userMappingTransfer = new UserMappingTransfer();
    userMappingTransfer.setOpenQualityCheckerUserToken(OQC_USER_TOKEN);

    userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);

    verify(userMappingRepository)
        .findByAccountMappingAndAtlassianUserAccountId(accountMapping, AtlassianUtil.USER_ID);
  }

  /** Test case: OQCPD_24_BCK_09_UT */
  @Test
  public void testStoreUserMappingWithExistingUserMapping() {
    final UserMapping userMapping = mockUserMapping(mockDefaultAccountMapping(), OQC_USER_TOKEN);
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockOpenQualityCheckerTokenValidator(
        NEW_OPEN_QUALITY_CHECKER_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, true);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);
    final UserMappingTransfer userMappingTransfer = new UserMappingTransfer();
    userMappingTransfer.setOpenQualityCheckerUserToken(NEW_OPEN_QUALITY_CHECKER_USER_TOKEN);

    userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);

    verify(userMappingRepository).save(userMapping);
    assertThat(userMapping.getOpenQualityCheckerUserToken())
        .isEqualTo(NEW_OPEN_QUALITY_CHECKER_USER_TOKEN);
  }

  /** Test case: OQCPD_24_BCK_10_UT */
  @Test
  public void testStoreUserMappingWithNotExistingUserMapping() {
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    mockOpenQualityCheckerTokenValidator(OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, true);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);
    final UserMappingTransfer userMappingTransfer = new UserMappingTransfer();
    userMappingTransfer.setOpenQualityCheckerUserToken(OQC_USER_TOKEN);

    userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);

    final UserMapping userMapping = new UserMapping();
    userMapping.setAccountMapping(accountMapping);
    userMapping.setOpenQualityCheckerUserToken(OQC_USER_TOKEN);
    userMapping.setAtlassianUserAccountId(AtlassianUtil.USER_ID);

    verify(userMappingRepository).save(userMapping);
  }

  private void mockOpenQualityCheckerTokenValidator(
      final String userToken, final String accountName, final boolean isValid) {
    when(openQualityCheckerTokenValidator.isUserTokenValidForAccountName(userToken, accountName))
        .thenReturn(isValid);
  }
}
