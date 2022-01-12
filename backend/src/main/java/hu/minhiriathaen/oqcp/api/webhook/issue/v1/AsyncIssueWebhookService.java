package hu.minhiriathaen.oqcp.api.webhook.issue.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.IssueUpdatedEvent;

public interface AsyncIssueWebhookService {

  void handleIssueUpdated(final AtlassianHostUser atlassianHostUser, final IssueUpdatedEvent event);
}
