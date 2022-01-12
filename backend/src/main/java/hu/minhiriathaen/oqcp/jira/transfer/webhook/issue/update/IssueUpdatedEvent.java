package hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Generated
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueUpdatedEvent {

  private UpdatedIssue issue;

  private IssueChangelog changelog;
}
