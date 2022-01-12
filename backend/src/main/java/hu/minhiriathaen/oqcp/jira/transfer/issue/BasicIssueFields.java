package hu.minhiriathaen.oqcp.jira.transfer.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.document.RootDocumentNode;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicIssueFields {

  private String summary;

  private RootDocumentNode description;

  private IdentifiedJiraObject project;

  @JsonProperty("issuetype")
  private IdentifiedJiraObject issueType;

  private IdentifiedJiraObject parent;
}
