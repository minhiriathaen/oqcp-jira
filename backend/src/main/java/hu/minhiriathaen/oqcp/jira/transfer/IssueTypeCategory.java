package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IssueTypeCategory {
  SUBTASK("subtask"),

  STANDARD("standard");

  @Getter @JsonValue private final String jsonValue;
}
