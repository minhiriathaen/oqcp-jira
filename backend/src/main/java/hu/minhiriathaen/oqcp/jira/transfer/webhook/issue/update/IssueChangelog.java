package hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Generated
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueChangelog {

  private List<IssueChangelogItem> items = new ArrayList<>();
}
