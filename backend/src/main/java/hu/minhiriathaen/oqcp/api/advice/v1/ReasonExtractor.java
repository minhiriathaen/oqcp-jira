package hu.minhiriathaen.oqcp.api.advice.v1;

import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReasonExtractor {

  public String extractReason(
      final AdviceAssessmentChangedTransfer adviceAssessmentChangedTransfer) {
    return adviceAssessmentChangedTransfer.getAdvice().getReasons().stream()
        .sorted(Comparator.comparing(ReasonTransfer::getSignificance))
        .map(ReasonTransfer::getReason)
        .collect(Collectors.joining("\n"));
  }
}
