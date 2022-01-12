package hu.minhiriathaen.oqcp.jira.transfer;

import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JiraIssueStatusTest {

  @Test
  public void testFromStringWithNull() {
    final JiraIssueStatus status = JiraIssueStatus.fromString(null);

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.UNKNOWN);
  }

  @Test
  public void testFromStringWithEmpty() {
    final JiraIssueStatus status = JiraIssueStatus.fromString(Strings.EMPTY);

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.UNKNOWN);
  }

  @Test
  public void testFromStringWithOpenCapital() {
    final JiraIssueStatus status = JiraIssueStatus.fromString("Open");

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.OPEN);
  }

  @Test
  public void testFromStringWithOpenLowerCase() {
    final JiraIssueStatus status = JiraIssueStatus.fromString("open");

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.OPEN);
  }

  @Test
  public void testFromStringWithOpenFullCap() {
    final JiraIssueStatus status = JiraIssueStatus.fromString("OPEN");

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.OPEN);
  }

  @Test
  public void testFromStringWithOpenLeadingTailingSpaces() {
    final JiraIssueStatus status = JiraIssueStatus.fromString("  open  ");

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.OPEN);
  }

  @Test
  public void testFromStringWithInProgress() {
    final JiraIssueStatus status = JiraIssueStatus.fromString("In progress");

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.IN_PROGRESS);
  }

  @Test
  public void testFromStringWithInProgressFullCap() {
    final JiraIssueStatus status = JiraIssueStatus.fromString("In_Progress");

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.IN_PROGRESS);
  }

  @Test
  public void testFromStringWithUnknown() {
    final JiraIssueStatus status = JiraIssueStatus.fromString("not existing state");

    Assertions.assertThat(status).isEqualTo(JiraIssueStatus.UNKNOWN);
  }
}
