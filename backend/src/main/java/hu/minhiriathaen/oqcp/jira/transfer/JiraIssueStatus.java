package hu.minhiriathaen.oqcp.jira.transfer;

import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import java.util.Locale;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public enum JiraIssueStatus {
  OPEN,
  REOPENED,
  RESOLVED,
  CLOSED,
  IN_PROGRESS,
  UNKNOWN;

  @Getter private JiraIssueStatus[] possibleTargets;

  static {
    OPEN.possibleTargets =
        new JiraIssueStatus[] {
          JiraIssueStatus.RESOLVED, JiraIssueStatus.CLOSED, JiraIssueStatus.IN_PROGRESS
        };
    REOPENED.possibleTargets =
        new JiraIssueStatus[] {JiraIssueStatus.RESOLVED, JiraIssueStatus.IN_PROGRESS};
    RESOLVED.possibleTargets =
        new JiraIssueStatus[] {JiraIssueStatus.REOPENED, JiraIssueStatus.CLOSED};
    CLOSED.possibleTargets = new JiraIssueStatus[] {JiraIssueStatus.REOPENED};
    IN_PROGRESS.possibleTargets =
        new JiraIssueStatus[] {
          JiraIssueStatus.OPEN, JiraIssueStatus.CLOSED, JiraIssueStatus.RESOLVED
        };
  }

  public static JiraIssueStatus fromString(final String canonicalValue) {

    if (StringUtils.isBlank(canonicalValue)) {
      return JiraIssueStatus.UNKNOWN;
    }

    final String replace = canonicalValue.toUpperCase(Locale.ENGLISH).trim().replace(' ', '_');

    JiraIssueStatus jiraIssueStatus = JiraIssueStatus.UNKNOWN;
    try {
      jiraIssueStatus = Enum.valueOf(JiraIssueStatus.class, replace);

    } catch (final IllegalArgumentException e) {
      log.warn("Unable to resolve value because of {} ", e.getMessage());
    }

    return jiraIssueStatus;
  }

  public static JiraIssueStatus fromIssue(final IssueBean issueBean) {

    if (null == issueBean
        || null == issueBean.getFields()
        || null == issueBean.getFields().getStatus()) {

      return UNKNOWN;
    }

    return fromString(issueBean.getFields().getStatus().getName());
  }
}
