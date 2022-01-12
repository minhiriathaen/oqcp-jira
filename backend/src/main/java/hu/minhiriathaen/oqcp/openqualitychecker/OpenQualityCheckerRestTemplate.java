package hu.minhiriathaen.oqcp.openqualitychecker;

import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerResultWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class OpenQualityCheckerRestTemplate extends RestTemplate {

  private final String baseUrl;

  protected static HttpHeaders createAuthorizationHeader(final String userToken) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("token", userToken);
    return headers;
  }

  protected <T> ResponseEntity<T> get(
      final String url, final Class<T> responseType, final Object... params) {
    return exchange(baseUrl + url, HttpMethod.GET, responseType, params);
  }

  protected ResponseEntity<OpenQualityCheckerResultWrapper> authenticatedGet(
      final String url, final String userToken, final Object... params) {

    return exchange(
        baseUrl + url,
        HttpMethod.GET,
        OpenQualityCheckerResultWrapper.class,
        new HttpEntity<>(createAuthorizationHeader(userToken)),
        params);
  }

  protected ResponseEntity<OpenQualityCheckerResultWrapper> authenticatedPost(
      final String url, final String userToken, final Object... params) {

    return exchange(
        baseUrl + url,
        HttpMethod.POST,
        OpenQualityCheckerResultWrapper.class,
        new HttpEntity<>(createAuthorizationHeader(userToken)),
        params);
  }

  private <T> ResponseEntity<T> exchange(
      final String url,
      final HttpMethod method,
      final Class<T> responseType,
      final Object... params) {
    return exchange(url, method, responseType, HttpEntity.EMPTY, params);
  }

  private <T> ResponseEntity<T> exchange(
      final String url,
      final HttpMethod method,
      final Class<T> responseType,
      final HttpEntity<?> entity,
      final Object... params) {

    log.info("{} {}, params: {}, request: {}", method, url, params, entity);

    final ResponseEntity<T> response = exchange(url, method, entity, responseType, params);

    log.info(
        "{} {}, response code: {}, body: {}",
        method,
        url,
        response.getStatusCode(),
        response.getBody());

    return response;
  }
}
