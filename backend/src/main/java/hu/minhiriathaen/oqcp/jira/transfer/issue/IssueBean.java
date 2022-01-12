package hu.minhiriathaen.oqcp.jira.transfer.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueBean extends IdentifiedJiraObject {

  private IssueFields fields;

  private List<IssueTransition> transitions;
}
