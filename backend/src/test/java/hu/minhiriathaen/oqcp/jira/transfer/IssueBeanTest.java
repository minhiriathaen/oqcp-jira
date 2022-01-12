package hu.minhiriathaen.oqcp.jira.transfer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.minhiriathaen.oqcp.jira.transfer.issue.IssueBean;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class IssueBeanTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  public void testConversion() throws IOException {

    final String resourceName = "issue-bean.json";

    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final File file =
        new File(Objects.requireNonNull(classLoader.getResource(resourceName)).getFile());

    final IssueBean issue = OBJECT_MAPPER.readValue(file, IssueBean.class);

    assertThat(issue.getId()).isEqualTo("10000");
    assertThat(issue.getFields().getCreated())
        .isCloseTo("2020-11-19T13:50:00", TimeUnit.MINUTES.toMillis(1));
    assertThat(issue.getFields().getUpdated())
        .isCloseTo("2020-11-19T13:55:00", TimeUnit.MINUTES.toMillis(1));
    assertThat(issue.getFields().getStatus().getId()).isEqualTo("4");
    assertThat(issue.getFields().getStatus().getName()).isEqualTo("Reopened");
    assertThat(issue.getTransitions().get(0).getId()).isEqualTo("4");
    assertThat(issue.getTransitions().get(0).getName()).isEqualTo("Start Progress");
    assertThat(issue.getTransitions().get(0).getTarget().getId()).isEqualTo("3");
    assertThat(issue.getTransitions().get(0).getTarget().getName()).isEqualTo("In Progress");
    assertThat(issue.getTransitions().get(1).getId()).isEqualTo("5");
    assertThat(issue.getTransitions().get(1).getName()).isEqualTo("Resolve Issue");
    assertThat(issue.getTransitions().get(1).getTarget().getId()).isEqualTo("5");
    assertThat(issue.getTransitions().get(1).getTarget().getName()).isEqualTo("Resolved");
  }
}
