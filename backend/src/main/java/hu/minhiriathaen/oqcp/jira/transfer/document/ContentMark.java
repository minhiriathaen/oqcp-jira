package hu.minhiriathaen.oqcp.jira.transfer.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentMark {

  /** Defines the type of mark such as code, link, and alike. Required. */
  private String type;

  private Attributes attrs;
}
