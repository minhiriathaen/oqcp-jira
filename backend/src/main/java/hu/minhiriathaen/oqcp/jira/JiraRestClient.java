package hu.minhiriathaen.oqcp.jira;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class JiraRestClient {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final AtlassianHostRestClients restClients;

  private final Map<AtlassianHost, RestTemplate> restTemplates = new HashMap<>();

  public <T> ResponseEntity<T> authenticatedGet(
      final AtlassianHost atlassianHost,
      final String url,
      final Class<T> responseType,
      final Object... uriVariables) {

    log.info("GET {}, params: {}", atlassianHost.getBaseUrl() + url, uriVariables);

    final ResponseEntity<T> response =
        getRestTemplate(atlassianHost)
            .exchange(
                url, HttpMethod.GET, createHttpEntityWithHeaders(), responseType, uriVariables);

    log.info(
        "GET {}, response code: {}, body: {}",
        atlassianHost.getBaseUrl() + url,
        response.getStatusCode(),
        response.getBody());

    return response;
  }

  public <T> ResponseEntity<T> authenticatedPost(
      final AtlassianHost atlassianHost,
      final String url,
      final Object request,
      final Class<T> responseType,
      final Object... uriVariables) {

    try {
      log.info(
          "POST {}, params: {}, body: {}",
          atlassianHost.getBaseUrl() + url,
          uriVariables,
          OBJECT_MAPPER.writeValueAsString(request));
    } catch (final JsonProcessingException e) {
      log.error(e.getClass().getName(), e);
    }

    final ResponseEntity<T> response =
        getRestTemplate(atlassianHost)
            .postForEntity(url, createHttpEntityWithHeaders(request), responseType, uriVariables);

    log.info(
        "POST {}, response code: {}, body: {}",
        atlassianHost.getBaseUrl() + url,
        response.getStatusCode(),
        response.getBody());

    return response;
  }

  public <T> ResponseEntity<T> authenticatedPut(
      final AtlassianHost atlassianHost,
      final String url,
      final Object request,
      final Class<T> responseType,
      final Object... uriVariables) {

    try {
      log.info(
          "PUT {}, params: {}, body: {}",
          atlassianHost.getBaseUrl() + url,
          uriVariables,
          OBJECT_MAPPER.writeValueAsString(request));
    } catch (final JsonProcessingException e) {
      log.error(e.getClass().getName(), e);
    }

    final ResponseEntity<T> response =
        getRestTemplate(atlassianHost)
            .exchange(
                url,
                HttpMethod.PUT,
                createHttpEntityWithHeaders(request),
                responseType,
                uriVariables);

    log.info(
        "PUT {}, response code: {}, body: {}",
        atlassianHost.getBaseUrl() + url,
        response.getStatusCode(),
        response.getBody());

    return response;
  }

  public RestTemplate getRestTemplate(final AtlassianHost atlassianHost) {

    if (!restTemplates.containsKey(atlassianHost)) {
      restTemplates.put(atlassianHost, restClients.authenticatedAsAddon(atlassianHost));
    }

    return restTemplates.get(atlassianHost);
  }

  private HttpEntity<String> createHttpEntityWithHeaders() {
    return new HttpEntity<>(getJiraHeaders());
  }

  private <T> HttpEntity<T> createHttpEntityWithHeaders(final T body) {
    return new HttpEntity<>(body, getJiraHeaders());
  }

  private HttpHeaders getJiraHeaders() {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("Accept-Language", "en");
    headers.add("X-Force-Accept-Language", "true");
    return headers;
  }
}
