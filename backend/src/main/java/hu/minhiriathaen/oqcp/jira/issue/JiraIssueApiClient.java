package hu.minhiriathaen.oqcp.jira.issue;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.transfer.CommentRequest;
import hu.minhiriathaen.oqcp.jira.transfer.CreateIssueRequest;
import hu.minhiriathaen.oqcp.jira.transfer.PerformTransitionRequest;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import java.util.List;

public interface JiraIssueApiClient {

  IssueBean getIssue(AtlassianHost atlassianHost, String issueId);

  List<IssueBean> getSubtasksByIssueTypeAndParentIssue(
      AtlassianHost atlassianHost, String issueTypeId, String parentIssueId);

  String createIssue(AtlassianHost atlassianHost, CreateIssueRequest request);

  void performTransition(
      AtlassianHost atlassianHost, String issueId, PerformTransitionRequest request);

  void addComment(AtlassianHost atlassianHost, String issueId, CommentRequest request);
}
