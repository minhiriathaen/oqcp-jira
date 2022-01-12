package hu.minhiriathaen.oqcp.api.advice.v1;

import hu.minhiriathaen.oqcp.util.NumberComparator;
import java.util.Comparator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContributorExtractor {

  public String extractContributor(
      final AdviceAssessmentChangedTransfer adviceAssessmentChangedTransfer) {
    final NumberComparator<Number> numberComparator = new NumberComparator<>();
    String contributor = null;

    if (CollectionUtils.isEmpty(adviceAssessmentChangedTransfer.getAdvice().getContributors())) {
      log.warn(
          "[{}] Contributor not found: {}",
          adviceAssessmentChangedTransfer.getAdvice().getId(),
          adviceAssessmentChangedTransfer);
    } else {
      final Optional<ContributorTransfer> optionalHighestContributor =
          adviceAssessmentChangedTransfer.getAdvice().getContributors().stream()
              .max(
                  Comparator.comparing(
                      ContributorTransfer::getNumberOfContributions, numberComparator));

      if (optionalHighestContributor.isEmpty()) {
        log.warn(
            "[{}] Contributor not found: {}",
            adviceAssessmentChangedTransfer.getAdvice().getId(),
            adviceAssessmentChangedTransfer);
      } else {
        contributor = optionalHighestContributor.get().getName();
      }

      log.info(
          "[{}] Extracted contributor '{}' from {}",
          adviceAssessmentChangedTransfer.getAdvice().getId(),
          contributor,
          adviceAssessmentChangedTransfer.getAdvice().getContributors());
    }
    return contributor;
  }
}
