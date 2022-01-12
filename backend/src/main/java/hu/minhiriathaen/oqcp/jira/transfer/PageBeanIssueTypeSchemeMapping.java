package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;

@Data
@Generated
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageBeanIssueTypeSchemeMapping extends PaginatedResponse {

  private List<IssueTypeSchemeMapping> values;
}
