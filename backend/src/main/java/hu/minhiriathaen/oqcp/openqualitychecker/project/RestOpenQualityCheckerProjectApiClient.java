package hu.minhiriathaen.oqcp.openqualitychecker.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerResultPage;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerResultWrapper;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestOpenQualityCheckerProjectApiClient implements OpenQualityCheckerProjectApiClient {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ContextHelper contextHelper;

  private final OpenQualityCheckerProjectRestTemplate openQualityCheckerProjectRestTemplate;

  @Override
  public List<OpenQualityCheckerProject> getPrivateProjects(final String userToken) {
    log.info("[{}] REST getPrivateProjects '{}'", contextHelper.getUserIdForLog(), userToken);

    short page = 0;
    boolean lastPage = false;
    final List<OpenQualityCheckerProject> openQualityCheckerProjects = new ArrayList<>();

    while (!lastPage) {
      final ResponseEntity<OpenQualityCheckerResultWrapper>
          openQualityCheckerResultWrapperResponseEntity =
              openQualityCheckerProjectRestTemplate.getPrivateProjects(userToken, ++page);

      if (!openQualityCheckerResultWrapperResponseEntity.hasBody()) {
        throw new ServiceError(
            HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR);
      }

      try {
        assert openQualityCheckerResultWrapperResponseEntity.getBody() != null;
        final OpenQualityCheckerResultPage openQualityCheckerResultPage =
            OBJECT_MAPPER.treeToValue(
                openQualityCheckerResultWrapperResponseEntity.getBody().getData(),
                OpenQualityCheckerResultPage.class);
        lastPage = openQualityCheckerResultPage.getLast();

        final List<OpenQualityCheckerProject> openQualityCheckerProjectList =
            OBJECT_MAPPER
                .readerFor(new TypeReference<List<OpenQualityCheckerProject>>() {})
                .readValue(openQualityCheckerResultPage.getContent());

        openQualityCheckerProjects.addAll(openQualityCheckerProjectList);

      } catch (final IOException e) {
        log.error(e.getClass().getName(), e);
        throw new ServiceError(
            HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR, e);
      }
    }
    return openQualityCheckerProjects;
  }

  @Override
  public void subscribeToProject(final String userToken, final String openQualityCheckerProjectId) {
    openQualityCheckerProjectRestTemplate.subscribeProject(userToken, openQualityCheckerProjectId);
  }

  @Override
  public void unsubscribeFromProject(
      final String userToken, final String openQualityCheckerProjectId) {
    openQualityCheckerProjectRestTemplate.unsubscribeProject(
        userToken, openQualityCheckerProjectId);
  }
}
