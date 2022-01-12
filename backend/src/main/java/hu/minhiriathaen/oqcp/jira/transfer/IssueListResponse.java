package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueListResponse extends PaginatedResponse {

  private List<IssueBean> issues;
}
