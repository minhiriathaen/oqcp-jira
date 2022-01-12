package hu.minhiriathaen.oqcp.api.advice.v1;

public interface AsyncAdviceService {

  void assessmentChanged(final AdviceAssessmentChangedTransfer adviceAssessmentChangedTransfer);

  void resolved(final AdviceResolvedTransfer adviceResolvedTransfer);
}
