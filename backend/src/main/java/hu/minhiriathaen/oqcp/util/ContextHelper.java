package hu.minhiriathaen.oqcp.util;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.AtlassianConnectSecurityContextHelper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContextHelper {

  private final AtlassianConnectSecurityContextHelper securityContextHelper;

  public String getUserIdForLog() {
    return securityContextHelper
        .getHostUserFromSecurityContext()
        .map(AtlassianHostUser::getUserAccountId)
        .map(Optional::get)
        .orElse("No user account id");
  }
}
