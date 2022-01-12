package hu.minhiriathaen.oqcp.jira.transfer.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BlockNode extends DocumentNode {

  /**
   * An array containing inline and block nodes that define the content of a section of the
   * document. Required.
   */
  protected List<DocumentNode> content;
}
