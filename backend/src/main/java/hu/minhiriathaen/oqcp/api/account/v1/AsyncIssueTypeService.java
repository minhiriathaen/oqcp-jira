package hu.minhiriathaen.oqcp.api.account.v1;

import com.atlassian.connect.spring.AtlassianHost;

public interface AsyncIssueTypeService {

  void createIssueTypes(AtlassianHost atlassianHost);
}
