package hu.minhiriathaen.oqcp;

import hu.minhiriathaen.oqcp.openqualitychecker.project.MockOpenQualityCheckerProjectApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectApiClient;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfig {

  @Bean
  @Primary
  public OpenQualityCheckerProjectApiClient openQualityCheckerProjectApiClient(
      final ContextHelper contextHelper) {
    return new MockOpenQualityCheckerProjectApiClient(contextHelper);
  }
}
