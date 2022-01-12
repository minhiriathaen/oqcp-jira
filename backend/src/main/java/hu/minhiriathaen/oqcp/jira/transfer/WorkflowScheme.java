package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowScheme extends IdentifiedJiraObject {

  private String defaultWorkflow;

  private Map<String, String> issueTypeMappings;

  private Boolean draft;
}
