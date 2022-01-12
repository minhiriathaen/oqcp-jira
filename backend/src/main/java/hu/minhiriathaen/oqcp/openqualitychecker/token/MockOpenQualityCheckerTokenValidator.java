package hu.minhiriathaen.oqcp.openqualitychecker.token;

import hu.minhiriathaen.oqcp.util.ContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class MockOpenQualityCheckerTokenValidator implements OpenQualityCheckerTokenValidator {

  private final ContextHelper contextHelper;

  @Override
  public boolean isUserTokenValidForAccountName(final String userToken, final String accountName) {

    log.info(
        "[{}] MOCK Validating token '{}' for account name '{}'",
        contextHelper.getUserIdForLog(),
        userToken,
        accountName);

    // Egyelőre nem kell OQC API hívás, a MockOpenQualityCheckerTokenValidator-t fogjuk
    // használni, ami minden esetben true-t fog visszaadni

    return true;
  }
}
