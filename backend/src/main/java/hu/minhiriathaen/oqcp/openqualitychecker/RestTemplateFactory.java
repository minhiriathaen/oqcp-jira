package hu.minhiriathaen.oqcp.openqualitychecker;

import hu.minhiriathaen.oqcp.openqualitychecker.issue.OpenQualityCheckerIssueRestTemplate;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectRestTemplate;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestTemplateFactory {

  @Value("${open-quality-checker.ssl.enabled:false}")
  private transient boolean sslEnabled;

  @Value("${open-quality-checker.ssl.key-store:default-key-store}")
  private transient Resource keyStoreResource;

  @Value("${open-quality-checker.ssl.key-store-password:default-key-store-password}")
  private transient String keyStorePassword;

  @Value("${open-quality-checker.ssl.key-store-type:JKS}")
  private transient String keyStoreType;

  @Value("${open-quality-checker.base-url}")
  private transient String baseUrl;

  @Value("${open-quality-checker.read-timeout:10000}")
  private transient int readTimeout;

  @Value("${open-quality-checker.connect-timeout:10000}")
  private transient int connectTimeout;

  @Value("${open-quality-checker.max-total-connection:5}")
  private transient int maxConnTotal;

  @Value("${open-quality-checker.max-total-connection-per-route:5}")
  private transient int maxConnPerRoute;

  @Bean
  public OpenQualityCheckerIssueRestTemplate getOpenQualityCheckerIssueRestTemplate() {
    final OpenQualityCheckerIssueRestTemplate restTemplate =
        new OpenQualityCheckerIssueRestTemplate(baseUrl);

    setAuthorizedRequestFactory(restTemplate);

    return restTemplate;
  }

  @Bean
  public OpenQualityCheckerProjectRestTemplate getOpenQualityCheckerProjectRestTemplate() {
    final OpenQualityCheckerProjectRestTemplate restTemplate =
        new OpenQualityCheckerProjectRestTemplate(baseUrl);

    setAuthorizedRequestFactory(restTemplate);

    return restTemplate;
  }

  private void setAuthorizedRequestFactory(final OpenQualityCheckerRestTemplate restTemplate) {
    if (!sslEnabled) {
      log.info("SSL authentication is disabled");
      return;
    }

    try {
      final HttpComponentsClientHttpRequestFactory requestFactory =
          createRequestFactory(createHttpClient());

      restTemplate.setRequestFactory(requestFactory);
    } catch (final Exception e) {
      log.error(e.getClass().getName(), e);
    }

    log.info("AuthorizedRequestFactory has been set for {}", restTemplate);
  }

  private HttpComponentsClientHttpRequestFactory createRequestFactory(
      final CloseableHttpClient httpClient) {

    final HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);

    requestFactory.setReadTimeout(readTimeout);
    requestFactory.setConnectTimeout(connectTimeout);

    return requestFactory;
  }

  private CloseableHttpClient createHttpClient()
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
          KeyManagementException, UnrecoverableKeyException {

    final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
    keyStore.load(keyStoreResource.getInputStream(), keyStorePassword.toCharArray());

    final SSLConnectionSocketFactory socketFactory =
        new SSLConnectionSocketFactory(
            new SSLContextBuilder()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, keyStorePassword.toCharArray())
                .build(),
            NoopHostnameVerifier.INSTANCE);

    return HttpClients.custom()
        .setSSLSocketFactory(socketFactory)
        .setMaxConnTotal(maxConnTotal)
        .setMaxConnPerRoute(maxConnPerRoute)
        .build();
  }
}
