package hu.minhiriathaen.oqcp.api.timeline.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.ContextJwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpenQualityCheckerTimelineController {

  public static final String GET_TIMELINE_URL =
      "/v1/projects/{projectName}/branches/{branchName}/timeline";

  private final OpenQualityCheckerTimelineService openQualityCheckerTimelineService;

  @ContextJwt
  @GetMapping(value = GET_TIMELINE_URL, produces = "image/svg+xml;charset=UTF-8")
  public byte[] getTimeline(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser,
      @PathVariable final String projectName,
      @PathVariable final String branchName) {

    log.info("getTimeline: {} - {}, {}", atlassianHostUser, projectName, branchName);

    return openQualityCheckerTimelineService.getTimeline(
        atlassianHostUser, projectName, branchName);
  }
}
