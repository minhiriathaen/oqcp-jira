package hu.minhiriathaen.oqcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class OpenQualityCheckerPluginApplication extends SpringBootServletInitializer {

  public static void main(final String[] args) {
    SpringApplication.run(OpenQualityCheckerPluginApplication.class);
  }
}
