package hu.minhiriathaen.oqcp.test.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.AccountMappingRepository;
import hu.minhiriathaen.oqcp.persistence.repository.AdviceGroupRepository;
import hu.minhiriathaen.oqcp.persistence.repository.AdviceRepository;
import hu.minhiriathaen.oqcp.persistence.repository.IssueTypeRepository;
import hu.minhiriathaen.oqcp.persistence.repository.ProjectMappingRepository;
import hu.minhiriathaen.oqcp.persistence.repository.UserMappingRepository;
import java.util.Optional;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

public class ServiceTestBase {

  protected static final String OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "OPEN_QUALITY_CHECKER_ACCOUNT_NAME";
  protected static final String OQC_USER_TOKEN = "OPEN_QUALITY_CHECKER_USER_TOKEN";
  protected static final String OTHER_OQC_USER_TOKEN = "OTHER_OPEN_QUALITY_CHECKER_USER_TOKEN";

  @MockBean protected transient AccountMappingRepository accountMappingRepository;
  @MockBean protected transient UserMappingRepository userMappingRepository;
  @MockBean protected transient IssueTypeRepository issueTypeRepository;
  @MockBean protected transient ProjectMappingRepository projectMappingRepository;
  @MockBean protected transient AtlassianHostRepository atlassianHostRepository;
  @MockBean protected transient AdviceRepository adviceRepository;
  @MockBean protected transient AdviceGroupRepository adviceGroupRepository;

  protected void assertServiceError(
      final ServiceError error, final HttpStatus expectedStatus, final ErrorCode expectedError) {
    assertThat(error.getStatus()).isEqualTo(expectedStatus);
    assertThat(error.getErrorCode()).isEqualTo(expectedError);
  }

  protected AtlassianHost createAtlassianHost(final String baseUrl) {
    return new AtlassianHostBuilder().withBaseUrl(baseUrl).build();
  }

  protected AtlassianHostUser createAtlassianHostUser(final String baseUrl) {
    final AtlassianHost atlassianHost = createAtlassianHost(baseUrl);

    return AtlassianHostUser.builder(atlassianHost)
        .withUserAccountId(AtlassianUtil.USER_ID)
        .build();
  }

  protected UserMapping mockUserMapping(
      final AccountMapping accountMapping, final String openQualityCheckerUserToken) {

    final UserMapping userMapping = new UserMapping();
    userMapping.setAccountMapping(accountMapping);
    userMapping.setOpenQualityCheckerUserToken(openQualityCheckerUserToken);
    userMapping.setAtlassianUserAccountId(AtlassianUtil.USER_ID);

    when(userMappingRepository.findByAccountMappingAndAtlassianUserAccountId(
            accountMapping, AtlassianUtil.USER_ID))
        .thenReturn(Optional.of(userMapping));

    return userMapping;
  }

  protected UserMapping mockOtherUserMapping(
      final AccountMapping accountMapping, final String openQualityCheckerUserToken) {

    final UserMapping userMapping = new UserMapping();
    userMapping.setAccountMapping(accountMapping);
    userMapping.setOpenQualityCheckerUserToken(openQualityCheckerUserToken);
    userMapping.setAtlassianUserAccountId(AtlassianUtil.OTHER_USER_ID);

    when(userMappingRepository.findByAccountMappingAndAtlassianUserAccountId(
            accountMapping, AtlassianUtil.OTHER_USER_ID))
        .thenReturn(Optional.of(userMapping));

    return userMapping;
  }

  protected UserMapping mockUserMappingByAtlassianHostUrl(
      final AccountMapping accountMapping, final String openQualityCheckerUserToken) {

    final UserMapping userMapping = new UserMapping();
    userMapping.setAccountMapping(accountMapping);
    userMapping.setOpenQualityCheckerUserToken(openQualityCheckerUserToken);
    userMapping.setAtlassianUserAccountId(AtlassianUtil.USER_ID);

    when(userMappingRepository.findByAccountMappingAtlassianHostUrlAndAtlassianUserAccountId(
            accountMapping.getAtlassianHostUrl(), AtlassianUtil.USER_ID))
        .thenReturn(Optional.of(userMapping));

    return userMapping;
  }

  protected AccountMapping mockDefaultAccountMapping() {
    return mockAccountMapping(AtlassianUtil.BASE_URL);
  }

  protected AccountMapping mockOtherAccountMapping() {
    return mockAccountMapping(AtlassianUtil.OTHER_BASE_URL);
  }

  protected AccountMapping mockAccountMapping(final String atlassianHostUrl) {
    final AccountMapping accountMapping = new AccountMapping();
    accountMapping.setAtlassianHostUrl(atlassianHostUrl);
    accountMapping.setOpenQualityCheckerAccountName(OPEN_QUALITY_CHECKER_ACCOUNT_NAME);

    when(accountMappingRepository.findByAtlassianHostUrl(atlassianHostUrl))
        .thenReturn(Optional.of(accountMapping));

    return accountMapping;
  }

  protected AtlassianHostUser createDefaultHostUser() {
    return createAtlassianHostUser(AtlassianUtil.BASE_URL);
  }
}
