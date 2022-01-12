package hu.minhiriathaen.oqcp.openqualitychecker.issue;

import hu.minhiriathaen.oqcp.util.ContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class MockOpenQualityCheckerIssueApiClient implements OpenQualityCheckerIssueApiClient {

  private final ContextHelper contextHelper;

  @Override
  public void notifyIssueClosed(
      final String userToken,
      final String projectId,
      final String branchName,
      final String adviceId) {

    log.info(
        "[{}] MOCK Notifying Open Quality Checker about closed issue with user token '{}', "
            + "project id '{}', branch name '{}' and advice id '{}'",
        contextHelper.getUserIdForLog(),
        userToken,
        projectId,
        branchName,
        adviceId);
  }
}
