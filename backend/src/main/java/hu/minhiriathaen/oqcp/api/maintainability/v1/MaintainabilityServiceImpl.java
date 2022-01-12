package hu.minhiriathaen.oqcp.api.maintainability.v1;

import static hu.minhiriathaen.oqcp.util.HostUtil.unwrapAccountId;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.branch.OpenQualityCheckerBranchApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.project.OpenQualityCheckerProjectApiClient;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerBranch;
import hu.minhiriathaen.oqcp.openqualitychecker.transfer.OpenQualityCheckerProject;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.ProjectMappingRepository;
import hu.minhiriathaen.oqcp.util.AccountMappingUtil;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import hu.minhiriathaen.oqcp.util.UserMappingUtil;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintainabilityServiceImpl implements MaintainabilityService {

  private final AccountMappingUtil accountMappingUtil;

  private final UserMappingUtil userMappingUtil;

  private final ProjectMappingRepository projectMappingRepository;

  private final OpenQualityCheckerBranchApiClient openQualityCheckerBranchApiClient;

  private final MaintainabilityConverter maintainabilityConverter;

  private final OpenQualityCheckerProjectApiClient openQualityCheckerProjectApiClient;

  private final ContextHelper contextHelper;

  @Override
  public List<ProjectMaintainabilityTransfer> getMaintainabilities(
      final AtlassianHostUser atlassianHostUser) {

    final AccountMapping accountMapping = accountMappingUtil.findAccountMapping(atlassianHostUser);

    final UserMapping userMapping =
        userMappingUtil.findUserMapping(accountMapping, unwrapAccountId(atlassianHostUser));

    try {
      final List<OpenQualityCheckerProject> openQualityCheckerProjects =
          openQualityCheckerProjectApiClient.getPrivateProjects(
              userMapping.getOpenQualityCheckerUserToken());

      final List<String> openQualityCheckerProjectIds =
          openQualityCheckerProjects.stream()
              .map(openQualityCheckerProject -> openQualityCheckerProject.getId().toString())
              .collect(Collectors.toList());

      final List<ProjectMapping> filteredProjectMappings =
          projectMappingRepository.findByAccountMappingAndOpenQualityCheckerProjectIdIn(
              accountMapping, openQualityCheckerProjectIds);

      return filteredProjectMappings.stream()
          .map(
              projectMapping ->
                  getProjectMaintainability(
                      userMapping.getOpenQualityCheckerUserToken(),
                      projectMapping.getOpenQualityCheckerProjectId()))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } catch (final Exception e) {
      log.error(e.getClass().getName(), e);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR, e);
    }
  }

  private ProjectMaintainabilityTransfer getProjectMaintainability(
      final String openQualityCheckerUserToken, final String openQualityCheckerProjectId) {

    try {
      final List<OpenQualityCheckerBranch> branches =
          openQualityCheckerBranchApiClient.getBranches(
              openQualityCheckerUserToken, openQualityCheckerProjectId);

      return maintainabilityConverter.convert(branches);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
        log.info(
            "[{}] Getting branches forbidden for project '{}' with OQC token: {}",
            contextHelper.getUserIdForLog(),
            openQualityCheckerProjectId,
            openQualityCheckerUserToken);
        return null;
      }

      throw e;
    }
  }
}
