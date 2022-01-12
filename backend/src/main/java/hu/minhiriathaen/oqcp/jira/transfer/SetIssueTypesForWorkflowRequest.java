package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetIssueTypesForWorkflowRequest {

  private String workflow;

  private List<String> issueTypes;

  private Boolean defaultMapping;

  private Boolean updateDraftIfNeeded;
}
