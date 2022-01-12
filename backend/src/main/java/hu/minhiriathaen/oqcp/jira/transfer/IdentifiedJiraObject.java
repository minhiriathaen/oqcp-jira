package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Generated
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentifiedJiraObject {

  private String id; // NOPMD

  private String key;

  private String name;

  public IdentifiedJiraObject(final String identifier) {
    id = identifier;
  }
}
