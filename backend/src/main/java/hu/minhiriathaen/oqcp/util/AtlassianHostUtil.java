package hu.minhiriathaen.oqcp.util;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AtlassianHostUtil {

  private final AtlassianHostRepository atlassianHostRepository;

  public AtlassianHost getAtlassianHost(final String atlassianHostUrl) {

    final Optional<AtlassianHost> optionalAtlassianHost =
        atlassianHostRepository.findFirstByBaseUrlOrderByLastModifiedDateDesc(atlassianHostUrl);

    return optionalAtlassianHost.orElseThrow(
        () -> {
          log.error("Atlassian host not found for url: {}", atlassianHostUrl);
          throw new ServiceError(
              HttpStatus.INTERNAL_SERVER_ERROR,
              ErrorCode.JIRA_CLOUD_ERROR,
              new NoSuchElementException(atlassianHostUrl));
        });
  }
}
