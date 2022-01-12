package hu.minhiriathaen.oqcp.api.advice.v1;

import com.atlassian.connect.spring.IgnoreJwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdviceController {

  public static final String ASSESSMENT_CHANGED_URL = "/v1/advices/assessmentchanged";

  public static final String RESOLVED_URL = "/v1/advices/resolved";

  private final AsyncAdviceService asyncAdviceService;

  @IgnoreJwt
  @PostMapping(RESOLVED_URL)
  public void resolved(@RequestBody final AdviceResolvedTransfer adviceResolvedTransfer) {
    log.info("resolved: {}", adviceResolvedTransfer);

    asyncAdviceService.resolved(adviceResolvedTransfer);
  }

  @IgnoreJwt
  @PostMapping(ASSESSMENT_CHANGED_URL)
  public void assessmentChanged(
      @RequestBody final AdviceAssessmentChangedTransfer adviceAssessmentChangedTransfer) {
    log.info("assessmentChanged: {}", adviceAssessmentChangedTransfer);

    asyncAdviceService.assessmentChanged(adviceAssessmentChangedTransfer);
  }
}
