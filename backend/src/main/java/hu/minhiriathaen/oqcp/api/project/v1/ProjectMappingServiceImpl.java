package hu.minhiriathaen.oqcp.api.project.v1;

import static hu.minhiriathaen.oqcp.util.HostUtil.unwrapAccountId;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.BadRequestError;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.OpenQualityCheckerProjectConflictError;
import hu.minhiriathaen.oqcp.persistence.entity.AccountMapping;
import hu.minhiriathaen.oqcp.persistence.entity.ProjectMapping;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.ProjectMappingRepository;
import hu.minhiriathaen.oqcp.util.AccountMappingUtil;
import hu.minhiriathaen.oqcp.util.ContextHelper;
import hu.minhiriathaen.oqcp.util.UserMappingUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMappingServiceImpl implements ProjectMappingService {

  private final ProjectMappingRepository projectMappingRepository;

  private final AccountMappingUtil accountMappingUtil;

  private final ContextHelper contextHelper;

  private final AsyncProjectMappingService asyncProjectMappingService;

  private final UserMappingUtil userMappingUtil;

  @Override
  public ProjectMappingTransfer getProjectMapping(
      final AtlassianHostUser atlassianHostUser, final String jiraProjectId) {

    final AccountMapping accountMapping = accountMappingUtil.findAccountMapping(atlassianHostUser);

    final List<ProjectMapping> projectMappings =
        projectMappingRepository.findByAccountMappingAndJiraProjectId(
            accountMapping, jiraProjectId);

    log.info("[{}] Project mappings found: {}", contextHelper.getUserIdForLog(), projectMappings);

    final ProjectMappingTransfer transfer = new ProjectMappingTransfer();

    final List<String> qualityProjectIds =
        projectMappings.stream()
            .map(ProjectMapping::getOpenQualityCheckerProjectId)
            .collect(Collectors.toList());

    transfer.setOpenQualityCheckerProjectIds(qualityProjectIds);

    return transfer;
  }

  @Override
  @Transactional
  public void storeProjectMapping(
      final AtlassianHostUser atlassianHostUser,
      final String jiraProjectId,
      final ProjectMappingTransfer projectMappingTransfer) {

    if (null == projectMappingTransfer
        || null == projectMappingTransfer.getOpenQualityCheckerProjectIds()) {
      throw new BadRequestError(ErrorCode.OPEN_QUALITY_CHECKER_PROJECT_IDS_REQUIRED);
    }

    if (!projectMappingTransfer.getOpenQualityCheckerProjectIds().isEmpty()) {
      checkConflictingProjects(jiraProjectId, projectMappingTransfer);
    }

    final AccountMapping accountMapping = accountMappingUtil.findAccountMapping(atlassianHostUser);

    final List<ProjectMapping> projectMappings =
        projectMappingRepository.findByAccountMappingAndJiraProjectId(
            accountMapping, jiraProjectId);

    final Set<String> projectIdsInRequest =
        new HashSet<>(projectMappingTransfer.getOpenQualityCheckerProjectIds());

    final Set<String> storedProjectIds =
        projectMappings.stream()
            .map(ProjectMapping::getOpenQualityCheckerProjectId)
            .collect(Collectors.toSet());

    final Set<String> projectIdsToDelete =
        SetUtils.difference(storedProjectIds, projectIdsInRequest);

    projectMappingRepository.deleteByAccountMappingAndOpenQualityCheckerProjectIdIn(
        accountMapping, projectIdsToDelete);
    log.info(
        "[{}] ProjectMappings were deleted for Jira project: '{}' and Open Quality Checker project ids: {}",
        contextHelper.getUserIdForLog(),
        jiraProjectId,
        projectIdsToDelete);
    final Set<String> projectIdsToSave = SetUtils.difference(projectIdsInRequest, storedProjectIds);

    final List<ProjectMapping> savedProjectMappings = new ArrayList<>();
    projectIdsToSave.forEach(
        id ->
            savedProjectMappings.add(
                saveProjectMapping(
                    accountMapping, id, jiraProjectId, unwrapAccountId(atlassianHostUser))));
    log.info(
        "[{}] ProjectMappings were saved for Jira project: '{}' and Open Quality Checker project ids: {}",
        contextHelper.getUserIdForLog(),
        jiraProjectId,
        projectIdsToSave);

    final UserMapping userMapping =
        userMappingUtil.findUserMapping(accountMapping, unwrapAccountId(atlassianHostUser));

    asyncProjectMappingService.processCreatedProjectMappings(
        atlassianHostUser, userMapping.getOpenQualityCheckerUserToken(), savedProjectMappings);

    asyncProjectMappingService.processDeletedProjectMappings(
        userMapping.getOpenQualityCheckerUserToken(), projectIdsToDelete);
  }

  private void checkConflictingProjects(
      final String jiraProjectId, final ProjectMappingTransfer projectMappingTransfer) {
    final List<ProjectMapping> projectMappingsInConflict =
        projectMappingRepository.findByOpenQualityCheckerProjectIdInAndJiraProjectIdNot(
            projectMappingTransfer.getOpenQualityCheckerProjectIds(), jiraProjectId);

    if (!projectMappingsInConflict.isEmpty()) {
      final List<String> mappedOpenQualityCheckerProjectIds =
          projectMappingsInConflict.stream()
              .map(ProjectMapping::getOpenQualityCheckerProjectId)
              .collect(Collectors.toList());

      log.info(
          "[{}] Conflicting ProjectMappings: JiraProjectId: '{}', OpenQualityCheckerProjectIds: {}",
          contextHelper.getUserIdForLog(),
          jiraProjectId,
          mappedOpenQualityCheckerProjectIds);
      throw new OpenQualityCheckerProjectConflictError(mappedOpenQualityCheckerProjectIds);
    }
  }

  public ProjectMapping saveProjectMapping(
      final AccountMapping accountMapping,
      final String openQualityCheckerProjectId,
      final String jiraProjectId,
      final String creatorAtlassianUserAccountId) {

    final ProjectMapping projectMapping = new ProjectMapping();

    projectMapping.setOpenQualityCheckerProjectId(openQualityCheckerProjectId);
    projectMapping.setJiraProjectId(jiraProjectId);
    projectMapping.setAccountMapping(accountMapping);
    projectMapping.setCreatorAtlassianUserAccountId(creatorAtlassianUserAccountId);

    return projectMappingRepository.save(projectMapping);
  }
}
