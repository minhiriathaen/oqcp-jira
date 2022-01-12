package hu.minhiriathaen.oqcp.exception;

import lombok.Data;
import lombok.Generated;

@Data
@Generated
public class ErrorTransfer {

  private String code;

  private String message;

  public ErrorTransfer(final ErrorCode code) {
    this.code = code.name();
    message = code.getMessage();
  }
}
