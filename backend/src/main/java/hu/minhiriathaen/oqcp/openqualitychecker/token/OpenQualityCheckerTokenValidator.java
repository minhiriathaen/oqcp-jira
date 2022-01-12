package hu.minhiriathaen.oqcp.openqualitychecker.token;

public interface OpenQualityCheckerTokenValidator {

  boolean isUserTokenValidForAccountName(String userToken, String accountName);
}
