package hu.minhiriathaen.oqcp.api.account.v1;

import static org.assertj.core.api.Assertions.assertThat;

import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Slf4j
@SpringBootTest
public class AccountMappingControllerTest extends ApplicationIntegrationTestBase {

  private static final String OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OPEN_QUALITY_CHECKER_ACCOUNT_NAME";
  private static final String OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME";
  private static final String RESPONSE_BODY_OQC_ACCOUNT_NAME = "$.openQualityCheckerAccountName";

  @Test
  public void testGetAccountMappingWithUnauthenticated() throws Exception {
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    mvc.perform(createGetAccountMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testGetAccountMappingWithNullBaseUrl() throws Exception {
    mockAuthentication(null);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    mvc.perform(createGetAccountMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testGetAccountMappingWithEmptyBaseUrl() throws Exception {
    mockAuthentication(Strings.EMPTY);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    mvc.perform(createGetAccountMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testGetAccountMappingWithNotExistingOpenQualityCheckerName() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockOtherAccountMapping();

    mvc.perform(createGetAccountMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath(RESPONSE_BODY_OQC_ACCOUNT_NAME).isEmpty());
  }

  @Test
  public void testGetAccountMappingWithValidOpenQualityCheckerName() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    mvc.perform(createGetAccountMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath(RESPONSE_BODY_OQC_ACCOUNT_NAME)
                .value(OPEN_QUALITY_CHECKER_ACCOUNT_NAME));
  }

  @Test
  public void testStoreAccountMappingWithUnauthenticated() throws Exception {
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    mvc.perform(createStoreAccountMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testStoreAccountMappingWithNullBaseUrl() throws Exception {
    mockAuthentication(null);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    final AccountMappingTransfer transfer = createMappingTransfer(null);

    mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testStoreAccountMappingWithEmptyBaseUrl() throws Exception {
    mockAuthentication(StringUtils.EMPTY);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    final AccountMappingTransfer transfer = createMappingTransfer(null);

    mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testStoreAccountMappingWithEmptyRequest() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    mvc.perform(createStoreAccountMappingRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  public void testStoreAccountMappingWithNullOpenQualityCheckerAccountName() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    final AccountMappingTransfer transfer = createMappingTransfer(null);

    assertError(
        mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer))),
        MockMvcResultMatchers.status().isBadRequest(),
        ErrorCode.OPEN_QUALITY_CHECKER_ACCOUNT_NAME_REQUIRED);
  }

  @Test
  public void testStoreAccountMappingWithEmptyOpenQualityCheckerAccountName() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    final AccountMappingTransfer transfer = createMappingTransfer(StringUtils.EMPTY);

    assertError(
        mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer))),
        MockMvcResultMatchers.status().isBadRequest(),
        ErrorCode.OPEN_QUALITY_CHECKER_ACCOUNT_NAME_REQUIRED);
  }

  @Test
  public void testStoreAccountMappingWithValidOpenQualityCheckerAccountName() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockOtherAccountMapping();

    final AccountMappingTransfer transfer =
        createMappingTransfer(OPEN_QUALITY_CHECKER_ACCOUNT_NAME);

    mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    accountMappingRepository.findByAtlassianHostUrl(AtlassianUtil.BASE_URL);

    final Optional<AccountMapping> newMapping =
        accountMappingRepository.findByAtlassianHostUrl(AtlassianUtil.BASE_URL);

    assertThat(newMapping.orElseThrow().getOpenQualityCheckerAccountName())
        .isEqualTo(OPEN_QUALITY_CHECKER_ACCOUNT_NAME);
  }

  @Test
  public void testStoreAccountMappingUpdatingWithSameOpenQualityCheckerAccountName()
      throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    final AccountMappingTransfer transfer =
        createMappingTransfer(OPEN_QUALITY_CHECKER_ACCOUNT_NAME);

    mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    final Optional<AccountMapping> newMapping =
        accountMappingRepository.findByAtlassianHostUrl(AtlassianUtil.BASE_URL);

    assertThat(newMapping.orElseThrow().getOpenQualityCheckerAccountName())
        .isEqualTo(OPEN_QUALITY_CHECKER_ACCOUNT_NAME);
  }

  @Test
  public void testStoreAccountMappingUpdatingWithNewOpenQualityCheckerAccountName()
      throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();

    final AccountMappingTransfer transfer =
        createMappingTransfer(OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME);

    mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer)))
        .andExpect(MockMvcResultMatchers.status().isOk());

    final Optional<AccountMapping> newMapping =
        accountMappingRepository.findByAtlassianHostUrl(AtlassianUtil.BASE_URL);

    assertThat(newMapping.orElseThrow().getOpenQualityCheckerAccountName())
        .isEqualTo(OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME);
  }

  @Test
  public void testStoreAccountMappingUpdatingWithUsedHostUrl() throws Exception {
    mockAuthentication(AtlassianUtil.BASE_URL);
    mockDefaultAccountMapping();
    mockOtherAccountMapping();

    final AccountMappingTransfer transfer =
        createMappingTransfer(OTHER_OPEN_QUALITY_CHECKER_ACCOUNT_NAME);

    assertError(
        mvc.perform(createStoreAccountMappingRequestBuilder().content(toRequestBody(transfer))),
        MockMvcResultMatchers.status().isConflict(),
        ErrorCode.OPEN_QUALITY_CHECKER_ACCOUNT_ALREADY_MAPPED);
  }

  private MockHttpServletRequestBuilder createGetAccountMappingRequestBuilder() {
    return MockMvcRequestBuilders.get(AccountMappingController.GET_ACCOUNT_MAPPING_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }

  private MockHttpServletRequestBuilder createStoreAccountMappingRequestBuilder() {
    return MockMvcRequestBuilders.put(AccountMappingController.STORE_ACCOUNT_MAPPING_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }

  private AccountMappingTransfer createMappingTransfer(final String accountName) {
    final AccountMappingTransfer request = new AccountMappingTransfer();
    request.setOpenQualityCheckerAccountName(accountName);

    return request;
  }
}
