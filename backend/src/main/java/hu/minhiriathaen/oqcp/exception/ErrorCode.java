package hu.minhiriathaen.oqcp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  OPEN_QUALITY_CHECKER_ACCOUNT_NAME_REQUIRED("Open Quality Checker account name is required"),

  ACCOUNT_MAPPING_NOT_FOUND("Account mapping not found for current Atlassian host"),

  USER_MAPPING_NOT_FOUND("User mapping not found for current Atlassian user"),

  OPEN_QUALITY_CHECKER_USER_TOKEN_VERIFICATION_FAILED("Invalid OpenQualityChecker user token"),

  OPEN_QUALITY_CHECKER_ACCOUNT_ALREADY_MAPPED(
      "OpenQualityChecker account already mapped to another Atlassian host"),

  OPEN_QUALITY_CHECKER_USER_TOKEN_REQUIRED("openQualityCheckerUserToken cannot be empty"),

  OPEN_QUALITY_CHECKER_ERROR("Error occurred during calling OpenQualityChecker"),

  OPEN_QUALITY_CHECKER_PROJECT_IDS_REQUIRED("openQualityCheckerProjectIds cannot be null"),

  OPEN_QUALITY_CHECKER_PROJECTS_ALREADY_MAPPED(
      "OpenQualityChecker project(s) already mapped to another Jira project"),

  OPEN_QUALITY_CHECKER_USER_TOKEN_NOT_FOUND(
      "OpenQualityChecker user token not found for current Atlassian host and user"),

  JIRA_CLOUD_ERROR("Error occurred during calling Jira Cloud");

  private final String message;
}
