package hu.minhiriathaen.oqcp.api.timeline.v1;

import com.atlassian.connect.spring.AtlassianHostUser;

public interface OpenQualityCheckerTimelineService {

  byte[] getTimeline(AtlassianHostUser atlassianHostUser, String projectName, String branchName);
}
