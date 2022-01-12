package hu.minhiriathaen.oqcp.api.project.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.fail;

import com.atlassian.connect.spring.AtlassianHostUser;
import hu.minhiriathaen.oqcp.exception.ErrorCode;
import hu.minhiriathaen.oqcp.exception.ServiceError;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@Slf4j
@SpringBootTest
public class OpenQualityCheckerProjectServiceImplTest extends ServiceTestBase {

  public static final String PROJECT_ID_1 = "1";
  public static final String PROJECT_NAME_1 = "Project name 1";

  public static final String PROJECT_ID_2 = "2";
  public static final String PROJECT_NAME_2 = "Project name 2";

  @Autowired private transient OpenQualityCheckerProjectService openQualityCheckerProjectService;

  /** Test case: OQCPD_25_BCK_01_UT */
  @Test
  public void testGetProjectListWithAccountMapping() {
    mockDefaultAccountMapping();

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    try {
      openQualityCheckerProjectService.getProjects(atlassianHostUser);

      failBecauseExceptionWasNotThrown(ServiceError.class);
    } catch (final ServiceError error) {
      assertServiceError(error, HttpStatus.FORBIDDEN, ErrorCode.USER_MAPPING_NOT_FOUND);
    }
  }

  /** Test case: OQCPD_25_BCK_02_UT */
  @Test
  public void testGetProjectListWithUserMapping() {
    mockUserMapping(mockDefaultAccountMapping(), OQC_USER_TOKEN);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    try {
      final List<OpenQualityCheckerProjectTransfer> openQualityCheckerProjectTransferList =
          openQualityCheckerProjectService.getProjects(atlassianHostUser);

      assertThat(openQualityCheckerProjectTransferList).isNotEmpty();
    } catch (final ServiceError error) {
      fail("UserMapping not found but it should");
    }
  }

  /** Test case: OQCPD_25_BCK_03_UT */
  @Test
  public void testGetProjectList() {
    mockUserMapping(mockDefaultAccountMapping(), OQC_USER_TOKEN);

    final AtlassianHostUser atlassianHostUser = createAtlassianHostUser(AtlassianUtil.BASE_URL);

    try {
      final List<OpenQualityCheckerProjectTransfer> openQualityCheckerProjectTransferList =
          openQualityCheckerProjectService.getProjects(atlassianHostUser);

      assertThat(openQualityCheckerProjectTransferList).isNotEmpty();
      assertThat(openQualityCheckerProjectTransferList.get(0).getId()).isEqualTo(PROJECT_ID_1);
      assertThat(openQualityCheckerProjectTransferList.get(0).getName()).isEqualTo(PROJECT_NAME_1);
      assertThat(openQualityCheckerProjectTransferList.get(1).getId()).isEqualTo(PROJECT_ID_2);
      assertThat(openQualityCheckerProjectTransferList.get(1).getName()).isEqualTo(PROJECT_NAME_2);
    } catch (final ServiceError error) {
      fail("OpenQualityCheckerProject not found");
    }
  }
}
