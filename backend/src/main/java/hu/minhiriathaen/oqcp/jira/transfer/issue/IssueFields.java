package hu.minhiriathaen.oqcp.jira.transfer.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueFields extends BasicIssueFields {

  private Date created;

  private Date updated;

  private IdentifiedJiraObject status;
}
