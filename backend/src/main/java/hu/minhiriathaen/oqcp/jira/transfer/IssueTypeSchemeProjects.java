package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.Generated;

/** Issue type scheme with a list of the projects that use it. */
@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueTypeSchemeProjects {

  /** Details of an issue type scheme. */
  private IssueTypeScheme issueTypeScheme;

  /** The IDs of the projects using the issue type scheme. */
  private List<String> projectIds;
}
