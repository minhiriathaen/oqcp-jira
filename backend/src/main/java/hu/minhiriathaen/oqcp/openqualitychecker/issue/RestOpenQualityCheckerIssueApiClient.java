package hu.minhiriathaen.oqcp.openqualitychecker.issue;

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
public class RestOpenQualityCheckerIssueApiClient implements OpenQualityCheckerIssueApiClient {

  private final ContextHelper contextHelper;

  private final OpenQualityCheckerIssueRestTemplate openQualityCheckerIssueRestTemplate;

  @Override
  public void notifyIssueClosed(
      final String userToken,
      final String projectId,
      final String branchName,
      final String adviceId) {

    if (StringUtils.isBlank(userToken)) {
      throw new IllegalArgumentException("User token parameter cannot be null or empty");
    }
    if (StringUtils.isBlank(projectId)) {
      throw new IllegalArgumentException("Project id parameter cannot be null or empty");
    }
    if (StringUtils.isBlank(branchName)) {
      throw new IllegalArgumentException("Branch name parameter cannot be null or empty");
    }
    if (StringUtils.isBlank(adviceId)) {
      throw new IllegalArgumentException("Advice id parameter cannot be null or empty");
    }

    log.info(
        "[{}] Notifying Open Quality Checker about closed issue with user token '{}', "
            + "project id '{}', branch name '{}' and advice id '{}'",
        contextHelper.getUserIdForLog(),
        userToken,
        projectId,
        branchName,
        adviceId);

    openQualityCheckerIssueRestTemplate.notifyIssueClosed(
        userToken, projectId, branchName, adviceId);
  }
}
