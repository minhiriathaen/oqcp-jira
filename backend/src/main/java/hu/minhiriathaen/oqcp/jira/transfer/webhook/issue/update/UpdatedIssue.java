package hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Generated
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatedIssue extends IdentifiedJiraObject {

  private UpdatedIssueFields fields;
}
