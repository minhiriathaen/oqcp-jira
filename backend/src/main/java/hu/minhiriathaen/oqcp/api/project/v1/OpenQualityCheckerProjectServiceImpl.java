package hu.minhiriathaen.oqcp.api.project.v1;

import static hu.minhiriathaen.oqcp.util.HostUtil.unwrapAccountId;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.util.AccountMappingUtil;
import hu.minhiriathaen.oqcp.util.UserMappingUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenQualityCheckerProjectServiceImpl implements OpenQualityCheckerProjectService {

  private final AccountMappingUtil accountMappingUtil;
  private final UserMappingUtil userMappingUtil;
  private final OpenQualityCheckerProjectApiClient openQualityCheckerProjectApiClient;

  @Override
  public List<OpenQualityCheckerProjectTransfer> getProjects(
      final AtlassianHostUser atlassianHostUser) {
    final AccountMapping accountMapping = accountMappingUtil.findAccountMapping(atlassianHostUser);

    final UserMapping userMapping =
        userMappingUtil.findUserMapping(accountMapping, unwrapAccountId(atlassianHostUser));

    try {
      final List<OpenQualityCheckerProject> openQualityCheckerProjects =
          openQualityCheckerProjectApiClient.getPrivateProjects(
              userMapping.getOpenQualityCheckerUserToken());

      return openQualityCheckerProjects.stream()
          .map(
              project -> {
                final OpenQualityCheckerProjectTransfer openQualityCheckerProjectTransfer =
                    new OpenQualityCheckerProjectTransfer();
                openQualityCheckerProjectTransfer.setId(String.valueOf(project.getId()));
                openQualityCheckerProjectTransfer.setName(project.getName());
                return openQualityCheckerProjectTransfer;
              })
          .collect(Collectors.toList());
    } catch (final Exception e) {
      log.error(e.getClass().getName(), e);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR, e);
    }
  }
}
