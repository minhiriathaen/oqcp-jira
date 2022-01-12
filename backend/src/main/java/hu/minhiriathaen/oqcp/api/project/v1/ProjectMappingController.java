package hu.minhiriathaen.oqcp.api.project.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.ContextJwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProjectMappingController {

  public static final String PROJECT_MAPPING_URL = "/v1/projectmappings/{jiraProjectId}";

  private final ProjectMappingService projectMappingService;

  @ContextJwt
  @GetMapping(PROJECT_MAPPING_URL)
  public ProjectMappingTransfer getProjectMapping(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser,
      @PathVariable final String jiraProjectId) {

    log.info("getProjectMapping: {} - {}", atlassianHostUser, jiraProjectId);

    return projectMappingService.getProjectMapping(atlassianHostUser, jiraProjectId);
  }

  @ContextJwt
  @PutMapping(PROJECT_MAPPING_URL)
  public void storeProjectMapping(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser,
      @PathVariable final String jiraProjectId,
      @RequestBody final ProjectMappingTransfer projectMappingTransfer) {
    log.info(
        "storeProjectMapping: {} - {}, {}",
        atlassianHostUser,
        jiraProjectId,
        projectMappingTransfer);

    projectMappingService.storeProjectMapping(
        atlassianHostUser, jiraProjectId, projectMappingTransfer);
  }
}
