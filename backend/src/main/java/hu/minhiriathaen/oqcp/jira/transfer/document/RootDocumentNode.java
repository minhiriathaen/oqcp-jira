package hu.minhiriathaen.oqcp.jira.transfer.document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

/**
 * The Atlassian Document Format (ADF) represents rich text stored in Atlassian products. For
 * example, in Jira Cloud platform, the text in issue comments and in textarea custom fields is
 * stored as ADF.
 *
 * @see <a
 *     href="https://developer.atlassian.com/cloud/jira/platform/apis/document/structure/">Atlassian
 *     Document Format</a>
 */
@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RootDocumentNode extends BlockNode {

  /** Defines the version of ADF used in this representation. Required. */
  private Integer version;

  public RootDocumentNode() {
    super();
    type = "doc";
    version = 1;
  }
}
