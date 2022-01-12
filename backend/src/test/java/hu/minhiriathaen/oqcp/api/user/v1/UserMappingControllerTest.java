package hu.minhiriathaen.oqcp.api.user.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.openqualitychecker.token.OpenQualityCheckerTokenValidator;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Slf4j
@SpringBootTest
public class UserMappingControllerTest extends ApplicationIntegrationTestBase {

  private static final String VALID_OPEN_QUALITY_CHECKER_TOKEN = "VALID_OPEN_QUALITY_CHECKER_TOKEN";
  private static final String INVALID_OPEN_QUALITY_CHECKER_TOKEN =
      "INVALID_OPEN_QUALITY_CHECKER_TOKEN";

  @MockBean private transient OpenQualityCheckerTokenValidator openQualityCheckerTokenValidator;

  @Test
  public void testGetUserMappingWithUnauthenticated() throws Exception {
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mvc.perform(createGetUserMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testGetUserMappingWithNullBaseUrl() throws Exception {
    mockAuthentication(null);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mvc.perform(createGetUserMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testGetUserMappingWithEmptyBaseUrl() throws Exception {
    mockAuthentication(StringUtils.EMPTY);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mvc.perform(createGetUserMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  /** Test case: OQCPD_24_BCK_01_IT */
  @Test
  public void testGetUserMappingNotExistingAccountMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);

    assertError(
        mvc.perform(createGetUserMappingRequestBuilder()),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_24_BCK_03_IT */
  @Test
  public void testGetUserMappingWithNotExistingUserMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();

    mvc.perform(createGetUserMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.openQualityCheckerUserToken").isEmpty());
  }

  /** Test case: OQCPD_24_BCK_02_IT */
  @Test
  public void testGetUserMappingWithExistingUserMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mvc.perform(createGetUserMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.openQualityCheckerUserToken")
                .value(OPEN_QUALITY_CHECKER_USER_TOKEN));
  }

  @Test
  public void testStoreUserMappingWithUnauthenticated() throws Exception {
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());
    mvc.perform(createStoreUserMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testStoreUserMappingWithNullBaseUrl() throws Exception {
    mockAuthentication(null);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    final UserMappingTransfer transfer = createMappingTransfer(OPEN_QUALITY_CHECKER_USER_TOKEN);

    mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testStoreUserMappingWithEmptyBaseUrl() throws Exception {
    mockAuthentication(StringUtils.EMPTY);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    final UserMappingTransfer transfer = createMappingTransfer(OPEN_QUALITY_CHECKER_USER_TOKEN);

    mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  /** Test case: OQCPD_24_BCK_04_IT */
  @Test
  public void testStoreUserMappingWithEmptyToken() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    final UserMappingTransfer transfer = createMappingTransfer(StringUtils.EMPTY);

    assertError(
        mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer))),
        MockMvcResultMatchers.status().isBadRequest(),
        ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_REQUIRED);
  }

  /** Test case: OQCPD_24_BCK_05_IT */
  @Test
  public void testStoreUserMappingWithNullToken() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    final UserMappingTransfer transfer = createMappingTransfer(null);

    assertError(
        mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer))),
        MockMvcResultMatchers.status().isBadRequest(),
        ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_REQUIRED);
  }

  /** Test case: OQCPD_24_BCK_06_IT */
  @Test
  public void testStoreUserMappingWithValidTokenAndExistingUserMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(accountMapping, OPEN_QUALITY_CHECKER_USER_TOKEN);
    mockOtherUserMapping(mockOtherAccountMapping());
    mockOpenQualityCheckerTokenValidator(
        VALID_OPEN_QUALITY_CHECKER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, true);

    final UserMappingTransfer transfer = createMappingTransfer(VALID_OPEN_QUALITY_CHECKER_TOKEN);

    mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    final Optional<UserMapping> savedUserMapping =
        userMappingRepository.findByAccountMappingAndAtlassianUserAccountId(
            accountMapping, AtlassianUtil.USER_ID);

    assertThat(savedUserMapping.orElseThrow().getOpenQualityCheckerUserToken())
        .isEqualTo(VALID_OPEN_QUALITY_CHECKER_TOKEN);
  }

  /** Test case: OQCPD_24_BCK_07_IT */
  @Test
  public void testStoreUserMappingNotExistingAccountMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockOtherUserMapping(mockOtherAccountMapping());

    final UserMappingTransfer transfer = createMappingTransfer(VALID_OPEN_QUALITY_CHECKER_TOKEN);

    assertError(
        mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer))),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.ACCOUNT_MAPPING_NOT_FOUND);
  }

  /** Test case: OQCPD_24_BCK_08_IT */
  @Test
  public void testStoreUserMappingInvalidToken() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mockOpenQualityCheckerTokenValidator(
        OPEN_QUALITY_CHECKER_USER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, false);

    final UserMappingTransfer transfer = createMappingTransfer(INVALID_OPEN_QUALITY_CHECKER_TOKEN);

    assertError(
        mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer))),
        MockMvcResultMatchers.status().isForbidden(),
        ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_VERIFICATION_FAILED);
  }

  /** Test case: OQCPD_24_BCK_09_IT */
  @Test
  public void testStoreUserMappingWithValidTokenAndNotExistingUserMapping() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    final AccountMapping accountMapping = mockDefaultAccountMapping();
    mockUserMapping(accountMapping, OPEN_QUALITY_CHECKER_USER_TOKEN);
    mockOtherUserMapping(mockOtherAccountMapping());
    mockOpenQualityCheckerTokenValidator(
        VALID_OPEN_QUALITY_CHECKER_TOKEN, OPEN_QUALITY_CHECKER_ACCOUNT_NAME, true);

    final UserMappingTransfer transfer = createMappingTransfer(VALID_OPEN_QUALITY_CHECKER_TOKEN);

    mvc.perform(createStoreUserMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    final Optional<UserMapping> savedUserMapping =
        userMappingRepository.findByAccountMappingAndAtlassianUserAccountId(
            accountMapping, AtlassianUtil.USER_ID);

    assertThat(savedUserMapping.orElseThrow().getOpenQualityCheckerUserToken())
        .isEqualTo(VALID_OPEN_QUALITY_CHECKER_TOKEN);
  }

  private MockHttpServletRequestBuilder createGetUserMappingRequestBuilder() {
    return MockMvcRequestBuilders.get(UserMappingController.GET_USER_MAPPING_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }

  private MockHttpServletRequestBuilder createStoreUserMappingRequestBuilder() {
    return MockMvcRequestBuilders.put(UserMappingController.STORE_USER_MAPPING_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }

  private UserMappingTransfer createMappingTransfer(final String userToken) {
    final UserMappingTransfer request = new UserMappingTransfer();
    request.setOpenQualityCheckerUserToken(userToken);
    return request;
  }

  private void mockOpenQualityCheckerTokenValidator(
      final String userToken, final String accountName, final boolean isValid) {
    when(openQualityCheckerTokenValidator.isUserTokenValidForAccountName(userToken, accountName))
        .thenReturn(isValid);
  }
}
