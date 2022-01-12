package hu.minhiriathaen.oqcp.api.timeline.v1;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.openqualitychecker.timeline.OpenQualityCheckerTimelineApiClient;
import hu.minhiriathaen.oqcp.test.util.ApplicationIntegrationTestBase;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Slf4j
@SpringBootTest
public class OpenQualityCheckerTimelineControllerTest extends ApplicationIntegrationTestBase {

  private static final String OPEN_QUALITY_CHECKER_PROJECT_NAME =
      "OPEN_QUALITY_CHECKER_PROJECT_NAME";

  private static final String MASTER_BRANCH_NAME = "master";

  private static final byte[] SVG = "<svg></svg>".getBytes();

  @MockBean
  private transient OpenQualityCheckerTimelineApiClient openQualityCheckerTimelineApiClient;

  @Test
  public void testGetTimelineWithUnauthenticated() throws Exception {

    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mvc.perform(createGetTimelineRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  public void testGetTimelineWithNullBaseUrl() throws Exception {
    mockAuthentication(null);

    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mvc.perform(createGetTimelineRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  @Test
  public void testGetTimelineWithEmptyBaseUrl() throws Exception {
    mockAuthentication(StringUtils.EMPTY);

    mockDefaultUserMapping(mockDefaultAccountMapping());
    mockOtherUserMapping(mockOtherAccountMapping());

    mvc.perform(createGetTimelineRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
  }

  /** Test case: OQCPD_27_BCK_06_IT (Best case) */
  @Test
  public void testGetTimelineWithExistingUserMapping() throws Exception {
    mockDefaultAuthentication();

    mockDefaultUserMapping(mockDefaultAccountMapping());

    mockOpenQualityCheckerTimelineClient();

    mvc.perform(createGetTimelineRequestBuilder())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().bytes(SVG));
  }

  /** Test case: OQCPD_27_BCK_07_IT */
  @Test
  public void testGetTimelineWithoutExistingOpenQualityCheckerUserToken() throws Exception {
    mockDefaultAuthentication();

    assertError(
        mvc.perform(createGetTimelineRequestBuilder()),
        MockMvcResultMatchers.status().is(HttpStatus.FORBIDDEN.value()),
        ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_NOT_FOUND);
  }

  /** Test case: OQCPD_27_BCK_08_IT */
  @Test
  public void testGetTimelineWithOpenQualityCheckerApiError() throws Exception {
    mockDefaultAuthentication();

    mockDefaultUserMapping(mockDefaultAccountMapping());

    when(openQualityCheckerTimelineApiClient.getTimeline(anyString(), anyString(), anyString()))
        .thenThrow(new RuntimeException("OpenQualityChecker API server error"));

    assertError(
        mvc.perform(createGetTimelineRequestBuilder()),
        MockMvcResultMatchers.status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()),
        ErrorCode.OPEN_QUALITY_CHECKER_ERROR);
  }

  private void mockOpenQualityCheckerTimelineClient() {

    when(openQualityCheckerTimelineApiClient.getTimeline(anyString(), anyString(), anyString()))
        .thenReturn(SVG);
  }

  private RequestBuilder createGetTimelineRequestBuilder() {
    return MockMvcRequestBuilders.get(
            OpenQualityCheckerTimelineController.GET_TIMELINE_URL,
            OPEN_QUALITY_CHECKER_PROJECT_NAME,
            MASTER_BRANCH_NAME)
        .contentType("image/svg+xml")
        .characterEncoding(StandardCharsets.UTF_8.displayName());
  }
}
