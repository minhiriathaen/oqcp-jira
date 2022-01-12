package hu.minhiriathaen.oqcp.openqualitychecker.timeline;

import hu.minhiriathaen.oqcp.util.ContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class RestOpenQualityCheckerTimelineApiClient
    implements OpenQualityCheckerTimelineApiClient {

  private final ContextHelper contextHelper;

  @Override
  public byte[] getTimeline(
      final String userToken, final String projectName, final String branchName) {

    log.info(
        "[{}] Getting timeline for user token '{}', project name '{}' and branch name '{} ",
        contextHelper.getUserIdForLog(),
        userToken,
        projectName,
        branchName);

    if (StringUtils.isBlank(userToken)) {
      throw new IllegalArgumentException("User token parameter can not be null or empty");
    }

    if (StringUtils.isBlank(projectName)) {
      throw new IllegalArgumentException("Project name parameter can not be null or empty");
    }

    if (StringUtils.isBlank(branchName)) {
      throw new IllegalArgumentException("Branch name parameter can not be null or empty");
    }

    // TODO use rest template to call OQC API

    return new byte[] {};
  }
}
