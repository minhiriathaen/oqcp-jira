package hu.minhiriathaen.oqcp.api.project.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.ContextJwt;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpenQualityCheckerProjectController {
  public static final String GET_OPEN_QUALITY_CHECKER_PROJECT_URL =
      "/v1/projects/openqualitychecker";

  private final OpenQualityCheckerProjectService openQualityCheckerProjectService;

  @ContextJwt
  @GetMapping(GET_OPEN_QUALITY_CHECKER_PROJECT_URL)
  public List<OpenQualityCheckerProjectTransfer> getProjects(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser) {
    log.info("getProjects: {}", atlassianHostUser);

    return openQualityCheckerProjectService.getProjects(atlassianHostUser);
  }
}
