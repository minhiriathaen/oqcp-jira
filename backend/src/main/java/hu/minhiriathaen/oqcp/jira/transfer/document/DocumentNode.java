package hu.minhiriathaen.oqcp.jira.transfer.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentNode {

  /** Defines the type of block node such as paragraph, table, and alike. Required. */
  protected String type;

  protected List<ContentMark> marks;

  protected Attributes attrs;
}
