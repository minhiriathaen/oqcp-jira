package hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Generated
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatedIssueFields {

  private IdentifiedJiraObject project;

  @JsonProperty("issuetype")
  private IdentifiedJiraObject issueType;

  private IdentifiedJiraObject parent;
}
