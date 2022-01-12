package hu.minhiriathaen.oqcp.openqualitychecker.issue;

import hu.minhiriathaen.oqcp.openqualitychecker.OpenQualityCheckerRestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenQualityCheckerIssueRestTemplate extends OpenQualityCheckerRestTemplate {

  public static final String OQC_ISSUE_CLOSED_URI =
      "/api/assessment/issueClosed?projectId={projectId}"
          + "&branchName={branchName}&adviceUid={adviceUid}";

  public OpenQualityCheckerIssueRestTemplate(final String baseUrl) {
    super(baseUrl);
  }

  public void notifyIssueClosed(
      final String userToken,
      final String projectId,
      final String branchName,
      final String adviceId) {
    authenticatedPost(OQC_ISSUE_CLOSED_URI, userToken, projectId, branchName, adviceId);
  }
}
