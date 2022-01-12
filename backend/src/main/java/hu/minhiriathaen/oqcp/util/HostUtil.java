package hu.minhiriathaen.oqcp.util;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HostUtil {

  public static AtlassianHost unwrapHost(final AtlassianHostUser atlassianHostUser) {

    if (null == atlassianHostUser) {
      throw new IllegalArgumentException("Atlassian host user is required");
    }

    final AtlassianHost atlassianHost = atlassianHostUser.getHost();

    if (StringUtils.isBlank(atlassianHost.getBaseUrl())) {
      throw new IllegalArgumentException("Atlassian host base URL is required");
    }

    return atlassianHost;
  }

  public static String unwrapAccountId(final AtlassianHostUser atlassianHostUser) {

    if (null == atlassianHostUser) {
      throw new IllegalArgumentException("Atlassian host user is required");
    }

    return atlassianHostUser
        .getUserAccountId()
        .orElseThrow(() -> new IllegalArgumentException("User account id is required"));
  }

  public static void checkAtlassianHost(final AtlassianHost atlassianHost) {
    if (atlassianHost == null) {
      throw new IllegalArgumentException("Atlassian host is required");
    }
  }
}
