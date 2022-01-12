package hu.minhiriathaen.oqcp.openqualitychecker.branch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectRestTemplate;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerResultWrapper;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class RestOpenQualityCheckerBranchApiClient implements OpenQualityCheckerBranchApiClient {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ContextHelper contextHelper;

  private final OpenQualityCheckerProjectRestTemplate openQualityCheckerProjectRestTemplate;

  @Override
  public List<OpenQualityCheckerBranch> getBranches(
      final String userToken, final String projectId) {

    log.info(
        "[{}] Getting branches for user token '{}' and project id '{}'",
        contextHelper.getUserIdForLog(),
        userToken,
        projectId);

    if (StringUtils.isBlank(userToken)) {
      throw new IllegalArgumentException("User token parameter can not be null or empty");
    }

    if (StringUtils.isBlank(projectId)) {
      throw new IllegalArgumentException("Project id parameter can not be null or empty");
    }

    final ResponseEntity<OpenQualityCheckerResultWrapper> result =
        openQualityCheckerProjectRestTemplate.getBranches(userToken, projectId);

    if (!result.hasBody()) {
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR);
    }

    final OpenQualityCheckerResultWrapper resultBody = result.getBody();

    try {
      assert resultBody != null;
      return OBJECT_MAPPER
          .readerFor(new TypeReference<List<OpenQualityCheckerBranch>>() {})
          .readValue(resultBody.getData());
    } catch (final IOException e) {
      log.error(e.getClass().getName(), e);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR, e);
    }
  }
}
