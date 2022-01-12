package hu.minhiriathaen.oqcp.api.maintainability.v1;

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
public class MaintainabilityController {

  public static final String GET_MAINTAINABILITIES_URL = "/v1/projects/maintainabilities";

  private final MaintainabilityService maintainabilityService;

  @ContextJwt
  @GetMapping(GET_MAINTAINABILITIES_URL)
  public List<ProjectMaintainabilityTransfer> getMaintainabilities(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser) {

    log.info("getMaintainabilities: {}", atlassianHostUser);
    return maintainabilityService.getMaintainabilities(atlassianHostUser);
  }
}
