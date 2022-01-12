package hu.minhiriathaen.oqcp.exception;

import hu.minhiriathaen.oqcp.util.ContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerController {

  private final ContextHelper contextHelper;

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorTransfer> onRuntimeError(final RuntimeException exception) {

    log.error("[{}] Unhandled exception!", contextHelper.getUserIdForLog(), exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

  @ExceptionHandler(ServiceError.class)
  public ResponseEntity<ErrorTransfer> onServiceError(final ServiceError exception) {

    log.info(
        "[{}] Service error: {} ({})",
        contextHelper.getUserIdForLog(),
        exception.getErrorCode().getMessage(),
        exception.getErrorCode());
    return ResponseEntity.status(exception.getStatus())
        .body(new ErrorTransfer(exception.getErrorCode()));
  }

  @ExceptionHandler(OpenQualityCheckerProjectConflictError.class)
  public ResponseEntity<ErrorTransfer> onOpenQualityCheckerProjectConflictError(
      final OpenQualityCheckerProjectConflictError exception) {

    log.info(
        "[{}] OpenQualityChecker project conflict error: {}",
        contextHelper.getUserIdForLog(),
        exception);
    return ResponseEntity.status(exception.getStatus())
        .body(
            new OpenQualityCheckerProjectConflictErrorTransfer(
                exception.getErrorCode(), exception.getMappedOpenQualityCheckerProjectIds()));
  }
}
