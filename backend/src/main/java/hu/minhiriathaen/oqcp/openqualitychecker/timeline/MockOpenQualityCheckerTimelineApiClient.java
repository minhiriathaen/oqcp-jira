package hu.minhiriathaen.oqcp.openqualitychecker.timeline;

import hu.minhiriathaen.oqcp.util.ContextHelper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockOpenQualityCheckerTimelineApiClient
    implements OpenQualityCheckerTimelineApiClient {

  private static final String SVG =
      "<svg height=\"100\" width=\"100\">"
          + "<circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"red\" />"
          + "</svg> ";

  private final ContextHelper contextHelper;

  @Override
  public byte[] getTimeline(
      final String userToken, final String projectName, final String branchName) {

    log.info(
        "[{}] MOCK Getting timeline for user token '{}', project name '{}' and branch name '{} ",
        contextHelper.getUserIdForLog(),
        userToken,
        projectName,
        branchName);

    return SVG.getBytes(StandardCharsets.UTF_8);
  }
}
