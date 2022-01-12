package hu.minhiriathaen.oqcp.jira.transfer.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attributes {

  /**
   * Defines the URL for the hyperlink and is the equivalent of the href value for a HTML <a></a>
   * element. Required in 'link' typed mark.
   */
  private String href;

  /**
   * Defines the title for the hyperlink and is the equivalent of the title value for a HTML <a></a>
   * element.
   */
  private String title;
}
