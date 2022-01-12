package hu.minhiriathaen.oqcp.api.user.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.ContextJwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserMappingController {

  public static final String GET_USER_MAPPING_URL = "/v1/usermappings/current";
  public static final String STORE_USER_MAPPING_URL = "/v1/usermappings/current";

  private final UserMappingService userMappingService;

  @ContextJwt
  @GetMapping(GET_USER_MAPPING_URL)
  public UserMappingTransfer getUserMapping(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser) {

    log.info("getUserMapping: {}", atlassianHostUser);

    return userMappingService.getUserMapping(atlassianHostUser);
  }

  @ContextJwt
  @PutMapping(STORE_USER_MAPPING_URL)
  public void storeUserMapping(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser,
      @RequestBody final UserMappingTransfer userMappingTransfer) {

    log.info("storeUserMapping: {} - {}", atlassianHostUser, userMappingTransfer);

    userMappingService.storeUserMapping(atlassianHostUser, userMappingTransfer);
  }
}
