package hu.minhiriathaen.oqcp.api.advice.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class AdviceAssessmentChangedTransferTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  public void testConversion() throws IOException {

    final String resourceName = "advice-like.json";

    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final File file =
        new File(Objects.requireNonNull(classLoader.getResource(resourceName)).getFile());

    final AdviceAssessmentChangedTransfer adviceTransfer =
        OBJECT_MAPPER.readValue(file, AdviceAssessmentChangedTransfer.class);

    assertThat(adviceTransfer.getAdvice().getId())
        .isEqualTo("WARNING:NullPointerException:99627556");
    assertThat(adviceTransfer.getProjectId()).isEqualTo(12);
    assertThat(adviceTransfer.getBranchName()).isEqualTo("master");
    assertThat(adviceTransfer.getAdvice().getContributors()).isNotNull();
    assertThat(adviceTransfer.getAdvice().getAssessment()).isEqualTo(Assessment.LIKE);
    assertThat(adviceTransfer.getAdvice().getReasons().get(0).getReason())
        .isEqualTo("This coding issue was created 15 day(s) earlier");
    assertThat(adviceTransfer.getAdvice().getReasons().get(0).getSignificance()).isEqualTo(0.9);
    assertThat(adviceTransfer.getAdvice().getReasons().get(1).getReason())
        .isEqualTo("This is a Blocker-level warning");
    assertThat(adviceTransfer.getAdvice().getReasons().get(1).getSignificance()).isEqualTo(1);
    assertThat(adviceTransfer.getAdvice().getReasons().get(2).getReason())
        .isEqualTo("Factor based on similar advice assessments");
    assertThat(adviceTransfer.getAdvice().getReasons().get(2).getSignificance()).isEqualTo(1.0);
    assertThat(adviceTransfer.getAdvice().getContributors().get(0).getName())
        .isEqualTo("developer.name.1");
    assertThat(adviceTransfer.getAdvice().getContributors().get(0).getNumberOfContributions())
        .isEqualTo(1);
    assertThat(adviceTransfer.getAdvice().getContributors().get(1).getName())
        .isEqualTo("developer.name.2");
    assertThat(adviceTransfer.getAdvice().getContributors().get(1).getNumberOfContributions())
        .isEqualTo(5);
  }
}
