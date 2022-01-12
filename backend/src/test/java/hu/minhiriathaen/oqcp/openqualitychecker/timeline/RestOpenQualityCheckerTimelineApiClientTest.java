package hu.minhiriathaen.oqcp.openqualitychecker.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.failBecauseExceptionWasNotThrown;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
public class RestOpenQualityCheckerTimelineApiClientTest {

  private static final String CAN_NOT_BE_NULL_OR_EMPTY = "can not be null or empty";
  private static final String OQC_USER_TOKEN = "OQC_USER_TOKEN";
  private static final String INVALID_OQC_USER_TOKEN = "INVALID_OQC_USER_TOKEN";
  private static final String BRANCH_NAME = "FirstBranch";
  private static final String PROJECT_NAME = "ProjectName";
  private static final byte[] SVG = "<svg></svg>".getBytes();

  @Autowired
  private transient RestOpenQualityCheckerTimelineApiClient restOpenQualityCheckerTimelineApiClient;

  @Test
  public void testGetTimelineWithNullTokenProjectNameAndBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(null, null, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithEmptyTokenNullProjectNameAndNullBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(StringUtils.EMPTY, null, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithNullTokenEmptyProjectNameAndNullBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(null, StringUtils.EMPTY, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithNullTokenNullProjectNameAndEmptyBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(null, null, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithEmptyTokenEmptyProjectNameAndNullBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(
          StringUtils.EMPTY, StringUtils.EMPTY, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithEmptyTokenNullProjectNameAndEmptyBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(
          StringUtils.EMPTY, null, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithNullTokenEmptyProjectNameAndEmptyBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(
          null, StringUtils.EMPTY, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithEmptyTokenEmptyProjectNameAndEmptyBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(
          StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithEmptyProjectNameAndEmptyBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(
          OQC_USER_TOKEN, StringUtils.EMPTY, StringUtils.EMPTY);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  public void testGetTimelineWithNullProjectNameAndNullBranchName() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(OQC_USER_TOKEN, null, null);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).contains(CAN_NOT_BE_NULL_OR_EMPTY);
    }
  }

  @Test
  @Disabled
  public void testGetTimelineWithInvalidUserToken() {
    try {
      restOpenQualityCheckerTimelineApiClient.getTimeline(
          INVALID_OQC_USER_TOKEN, PROJECT_NAME, BRANCH_NAME);

      failBecauseExceptionWasNotThrown(HttpClientErrorException.class);
    } catch (final HttpClientErrorException e) {
      assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Test
  @Disabled
  public void testGetTimelineWithValidUserToken() {
    final byte[] svg =
        restOpenQualityCheckerTimelineApiClient.getTimeline(
            OQC_USER_TOKEN, PROJECT_NAME, BRANCH_NAME);

    assertThat(svg).isNotEmpty();
    assertThat(svg).isEqualTo(SVG);
  }
}
