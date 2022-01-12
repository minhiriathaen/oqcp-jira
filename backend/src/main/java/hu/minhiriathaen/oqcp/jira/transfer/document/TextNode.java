package hu.minhiriathaen.oqcp.jira.transfer.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

/**
 * The text node holds document text.
 *
 * @see <a href="https://developer.atlassian.com/cloud/jira/platform/apis/document/nodes/text/">Text
 *     Node</a>
 */
@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextNode extends InlineNode {

  /** Non-empty text string. */
  private String text;

  public TextNode() {
    super();
    type = "text";
  }
}
