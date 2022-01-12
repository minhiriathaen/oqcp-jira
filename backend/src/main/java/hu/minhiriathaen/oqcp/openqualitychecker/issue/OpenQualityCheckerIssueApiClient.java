package hu.minhiriathaen.oqcp.openqualitychecker.issue;

public interface OpenQualityCheckerIssueApiClient {

  void notifyIssueClosed(String userToken, String projectId, String branchName, String adviceId);
}
