package hu.minhiriathaen.oqcp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ServiceError extends RuntimeException {

  private static final long serialVersionUID = -2291271747217896845L;

  private final HttpStatus status;

  private final ErrorCode errorCode;

  public ServiceError(
      final HttpStatus status, final ErrorCode errorCode, final Throwable throwable) {
    super(throwable);
    this.status = status;
    this.errorCode = errorCode;
  }
}
