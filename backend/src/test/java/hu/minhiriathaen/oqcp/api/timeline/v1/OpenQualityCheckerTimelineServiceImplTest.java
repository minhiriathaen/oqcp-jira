package hu.minhiriathaen.oqcp.api.timeline.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.openqualitychecker.timeline.OpenQualityCheckerTimelineApiClient;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class OpenQualityCheckerTimelineServiceImplTest extends ServiceTestBase {

  private static final String OPEN_QUALITY_CHECKER_PROJECT_NAME =
      "OPEN_QUALITY_CHECKER_PROJECT_NAME";

  private static final String MASTER_BRANCH_NAME = "master";

  private static final byte[] SVG = "<svg></svg>".getBytes();

  @Autowired private transient OpenQualityCheckerTimelineService openQualityCheckerTimelineService;

  @MockBean
  private transient OpenQualityCheckerTimelineApiClient openQualityCheckerTimelineApiClient;

  @Test
  public void testGetTimelineWithNullHostUser() {
    try {
      openQualityCheckerTimelineService.getTimeline(
          null, OPEN_QUALITY_CHECKER_PROJECT_NAME, MASTER_BRANCH_NAME);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host user is required");
    }
  }

  @Test
  public void testGetTimelineWithNullBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(null);

    try {
      openQualityCheckerTimelineService.getTimeline(
          hostUser, OPEN_QUALITY_CHECKER_PROJECT_NAME, MASTER_BRANCH_NAME);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host base URL is required");
    }
  }

  @Test
  public void testGetTimelineWithEmptyBaseUrl() {
    final AtlassianHostUser hostUser = createAtlassianHostUser(StringUtils.EMPTY);

    try {
      openQualityCheckerTimelineService.getTimeline(
          hostUser, OPEN_QUALITY_CHECKER_PROJECT_NAME, MASTER_BRANCH_NAME);

      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Atlassian host base URL is required");
    }
  }

  /** Test case: OQCPD_27_BCK_09_UT */
  @Test
  public void testGetTimelineWithExistingUserMapping() {

    final AtlassianHostUser atlassianHostUser = createDefaultHostUser();
    final AccountMapping accountMapping =
        mockAccountMapping(atlassianHostUser.getHost().getBaseUrl());

    final UserMapping userMapping =
        mockUserMappingByAtlassianHostUrl(accountMapping, OQC_USER_TOKEN);
    mockUserMappingByAtlassianHostUrl(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    openQualityCheckerTimelineService.getTimeline(
        atlassianHostUser, OPEN_QUALITY_CHECKER_PROJECT_NAME, MASTER_BRANCH_NAME);

    verify(openQualityCheckerTimelineApiClient)
        .getTimeline(
            userMapping.getOpenQualityCheckerUserToken(),
            OPEN_QUALITY_CHECKER_PROJECT_NAME,
            MASTER_BRANCH_NAME);
  }

  /** Test case: OQCPD_27_BCK_10_UT */
  @Test
  public void testGetTimelineSucceeded() {

    final AtlassianHostUser atlassianHostUser = createDefaultHostUser();
    final AccountMapping accountMapping =
        mockAccountMapping(atlassianHostUser.getHost().getBaseUrl());

    mockUserMappingByAtlassianHostUrl(accountMapping, OQC_USER_TOKEN);
    mockUserMappingByAtlassianHostUrl(mockOtherAccountMapping(), OTHER_OQC_USER_TOKEN);

    when(openQualityCheckerTimelineApiClient.getTimeline(
            OQC_USER_TOKEN, OPEN_QUALITY_CHECKER_PROJECT_NAME, MASTER_BRANCH_NAME))
        .thenReturn(SVG);

    final byte[] svg =
        openQualityCheckerTimelineService.getTimeline(
            atlassianHostUser, OPEN_QUALITY_CHECKER_PROJECT_NAME, MASTER_BRANCH_NAME);

    assertThat(svg).isEqualTo(SVG);
  }
}
