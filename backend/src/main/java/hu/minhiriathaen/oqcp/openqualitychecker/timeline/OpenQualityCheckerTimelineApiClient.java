package hu.minhiriathaen.oqcp.openqualitychecker.timeline;

public interface OpenQualityCheckerTimelineApiClient {

  byte[] getTimeline(String userToken, String projectName, String branchName);
}
