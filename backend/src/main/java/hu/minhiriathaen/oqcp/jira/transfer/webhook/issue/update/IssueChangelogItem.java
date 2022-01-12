package hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Generated
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueChangelogItem {

  private String field;

  private String from;

  private String fromString;

  private String to; // NOPMD

  private String toString;
}
