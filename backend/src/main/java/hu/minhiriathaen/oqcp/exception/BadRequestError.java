package hu.minhiriathaen.oqcp.exception;

import org.springframework.http.HttpStatus;

public class BadRequestError extends ServiceError {
  private static final long serialVersionUID = -3672188508610022209L;

  public BadRequestError(final ErrorCode code) {
    super(HttpStatus.BAD_REQUEST, code);
  }
}
