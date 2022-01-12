package hu.minhiriathaen.oqcp.openqualitychecker.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectRestTemplate;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerResultWrapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
class RestOpenQualityCheckerBranchApiClientTest {

  private static final String CAN_NOT_BE_NULL_OR_EMPTY = "can not be null or empty";
  private static final String OQC_USER_TOKEN = "OQC_USER_TOKEN";
  private static final String INVALID_OQC_USER_TOKEN = "INVALID_OQC_USER_TOKEN";
  private static final String PROJECT_ID = "1";

  private static final long BRANCH_1_ID = 11L;
  private static final String BRANCH_1_NAME = "FirstBranch";
  private static final String PROJECT_NAME = "ProjectName";
  private static final double BRANCH_1_MAINTAINABILITY = 1.1;

  private static final long BRANCH_2_ID = 22L;
  private static final String BRANCH_2_NAME = "SecondBranch";
  private static final double BRANCH_2_MAINTAINABILITY = 2.2;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired
  private transient RestOpenQualityCheckerBranchApiClient restOpenQualityCheckerBranchApiClient;

  @Value("classpath:oqc-get-branches-response.json")
  private transient Resource getBranchesResponseResourceJson;

  @MockBean
  private transient OpenQualityCheckerProjectRestTemplate openQualityCheckerProjectRestTemplate;

  @Test
  public void testGetBranchesWithNullTokenAndProjectId() {
    try {
      restOpenQualityCheckerBranchApiClient.getBranches(null, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetBranchesWithNullTokenAndEmptyProjectId() {
    try {
      restOpenQualityCheckerBranchApiClient.getBranches(null, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetBranchesWithEmptyTokenAndNullProjectId() {
    try {
      restOpenQualityCheckerBranchApiClient.getBranches(StringUtils.EMPTY, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetBranchesWithEmptyTokenAndProjectId() {
    try {
      restOpenQualityCheckerBranchApiClient.getBranches(StringUtils.EMPTY, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetBranchesWithNullProjectId() {
    try {
      restOpenQualityCheckerBranchApiClient.getBranches(OQC_USER_TOKEN, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetBranchesWithEmptyProjectId() {
    try {
      restOpenQualityCheckerBranchApiClient.getBranches(OQC_USER_TOKEN, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetBranchesWithInvalidUserToken() {
    when(openQualityCheckerProjectRestTemplate.getBranches(anyString(), anyString()))
        .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

    try {
      restOpenQualityCheckerBranchApiClient.getBranches(INVALID_OQC_USER_TOKEN, PROJECT_ID);

      failBecauseExceptionWasNotThrown(HttpClientErrorException.class);
    } catch (final HttpClientErrorException e) {
      assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Test
  public void testGetBranchesWithValidUserToken() throws JsonProcessingException {
    final OpenQualityCheckerResultWrapper resultWrapper =
        OBJECT_MAPPER.readValue(
            asString(getBranchesResponseResourceJson), OpenQualityCheckerResultWrapper.class);

    when(openQualityCheckerProjectRestTemplate.getBranches(OQC_USER_TOKEN, PROJECT_ID))
        .thenReturn(new ResponseEntity<>(resultWrapper, HttpStatus.OK));

    final List<OpenQualityCheckerBranch> branches =
        restOpenQualityCheckerBranchApiClient.getBranches(OQC_USER_TOKEN, PROJECT_ID);

    assertThat(branches).isNotEmpty();
    assertThat(branches.get(0).getId()).isEqualTo(BRANCH_1_ID);
    assertThat(branches.get(0).getName()).isEqualTo(BRANCH_1_NAME);
    assertThat(branches.get(0).getProject().getId()).isEqualTo(Long.parseLong(PROJECT_ID));
    assertThat(branches.get(0).getProject().getName()).isEqualTo(PROJECT_NAME);
    assertThat(
            branches
                .get(0)
                .getQualificationResult()
                .getQualification()
                .getMaintainability()
                .getValue())
        .isEqualTo(BRANCH_1_MAINTAINABILITY);
    assertThat(branches.get(1).getId()).isEqualTo(BRANCH_2_ID);
    assertThat(branches.get(1).getName()).isEqualTo(BRANCH_2_NAME);
    assertThat(branches.get(1).getProject().getId()).isEqualTo(Long.parseLong(PROJECT_ID));
    assertThat(branches.get(1).getProject().getName()).isEqualTo(PROJECT_NAME);
    assertThat(
            branches
                .get(1)
                .getQualificationResult()
                .getQualification()
                .getMaintainability()
                .getValue())
        .isEqualTo(BRANCH_2_MAINTAINABILITY);
  }

  private String asString(final Resource resource) {
    try (Reader reader = new InputStreamReader(resource.getInputStream())) {
      return FileCopyUtils.copyToString(reader);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
