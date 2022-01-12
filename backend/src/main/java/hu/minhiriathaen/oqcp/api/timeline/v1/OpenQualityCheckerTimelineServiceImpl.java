package hu.minhiriathaen.oqcp.api.timeline.v1;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.openqualitychecker.timeline.OpenQualityCheckerTimelineApiClient;
import hu.minhiriathaen.oqcp.persistence.entity.UserMapping;
import hu.minhiriathaen.oqcp.persistence.repository.UserMappingRepository;
import hu.minhiriathaen.oqcp.util.HostUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenQualityCheckerTimelineServiceImpl implements OpenQualityCheckerTimelineService {

  private final UserMappingRepository userMappingRepository;

  private final OpenQualityCheckerTimelineApiClient openQualityCheckerTimelineApiClient;

  @Override
  public byte[] getTimeline(
      final AtlassianHostUser atlassianHostUser,
      final String projectName,
      final String branchName) {

    final AtlassianHost atlassianHost = HostUtil.unwrapHost(atlassianHostUser);
    final String atlassianUserAccountId = HostUtil.unwrapAccountId(atlassianHostUser);

    final UserMapping userMapping =
        userMappingRepository
            .findByAccountMappingAtlassianHostUrlAndAtlassianUserAccountId(
                atlassianHost.getBaseUrl(), atlassianUserAccountId)
            .orElseThrow(
                () ->
                    new ServiceError(
                        HttpStatus.FORBIDDEN, ErrorCode.OPEN_QUALITY_CHECKER_USER_TOKEN_NOT_FOUND));

    try {
      return openQualityCheckerTimelineApiClient.getTimeline(
          userMapping.getOpenQualityCheckerUserToken(), projectName, branchName);

    } catch (final Exception e) {
      log.error(e.getClass().getName(), e);
      throw new ServiceError(
          HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.OPEN_QUALITY_CHECKER_ERROR, e);
    }
  }
}
