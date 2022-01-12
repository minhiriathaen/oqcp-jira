package hu.minhiriathaen.oqcp.exception;

import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OpenQualityCheckerProjectConflictError extends ServiceError {

  private static final long serialVersionUID = -3685780913981400544L;

  private final List<String> mappedOpenQualityCheckerProjectIds;

  public OpenQualityCheckerProjectConflictError(
      final List<String> mappedOpenQualityCheckerProjectIds) {
    super(HttpStatus.CONFLICT, ErrorCode.OPEN_QUALITY_CHECKER_PROJECTS_ALREADY_MAPPED);

    this.mappedOpenQualityCheckerProjectIds = mappedOpenQualityCheckerProjectIds;
  }
}
