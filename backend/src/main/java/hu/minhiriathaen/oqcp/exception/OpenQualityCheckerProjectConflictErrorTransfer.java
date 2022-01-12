package hu.minhiriathaen.oqcp.exception;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Getter;
import lombok.ToString;

@Getter
@Generated
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OpenQualityCheckerProjectConflictErrorTransfer extends ErrorTransfer {

  private final List<String> mappedOpenQualityCheckerProjectIds;

  public OpenQualityCheckerProjectConflictErrorTransfer(
      final ErrorCode code, final List<String> mappedOpenQualityCheckerProjectIds) {
    super(code);
    this.mappedOpenQualityCheckerProjectIds = mappedOpenQualityCheckerProjectIds;
  }
}
