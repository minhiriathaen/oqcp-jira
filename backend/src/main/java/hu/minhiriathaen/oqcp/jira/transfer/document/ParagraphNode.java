package hu.minhiriathaen.oqcp.jira.transfer.document;

import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ParagraphNode extends BlockNode {

  public ParagraphNode() {
    super();
    type = "paragraph";
    content = new ArrayList<>();
  }
}
