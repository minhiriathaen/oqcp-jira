package hu.minhiriathaen.oqcp.api.account.v1;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.connect.spring.AtlassianHost;
import hu.minhiriathaen.oqcp.jira.issuetype.JiraIssueTypeApiClient;
import hu.minhiriathaen.oqcp.jira.transfer.CreateIssueTypeRequest;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeCategory;
import hu.minhiriathaen.oqcp.jira.transfer.IssueTypeDetails;
import hu.minhiriathaen.oqcp.persistence.entity.IssueType;
import hu.minhiriathaen.oqcp.test.util.AtlassianUtil;
import hu.minhiriathaen.oqcp.test.util.ServiceTestBase;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class AsyncIssueTypeServiceImplTest extends ServiceTestBase {

  private static final String EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID =
      "Existing-AdviceGroupIssueTypeId-1";
  private static final String EXISTING_ADVICE_ISSUE_TYPE_ID = "Existing-AdviceIssueTypeId-2";

  private static final String OTHER_EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID =
      "Other-Existing-AdviceGroupIssueTypeId-1";
  private static final String OTHER_EXISTING_ADVICE_ISSUE_TYPE_ID =
      "Other-Existing-AdviceIssueTypeId-2";

  private static final String NEW_ADVICE_GROUP_ISSUE_TYPE_ID = "New-AdviceGroupIssueTypeId-1";
  private static final String NEW_ADVICE_ISSUE_TYPE_ID = "New-AdviceIssueTypeId-2";

  private static final String ADVICE_GROUP_ISSUE_TYPE_NAME = "OpenQualityChecker Advice group";
  private static final String ADVICE_ISSUE_TYPE_NAME = "OpenQualityChecker Advice";

  private final transient AtlassianHost atlassianHost = createAtlassianHost(AtlassianUtil.BASE_URL);
  private final transient String atlassianHostBaseUrl = atlassianHost.getBaseUrl();

  @Autowired protected transient AsyncIssueTypeService asyncIssueTypeService;

  @MockBean protected transient JiraIssueTypeApiClient jiraIssueTypeApiClient;

  @Test
  public void testCreateGroupIssueType() {
    mockGetSubtaskIssueType();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final CreateIssueTypeRequest createIssueTypeRequest = new CreateIssueTypeRequest();
    createIssueTypeRequest.setName(ADVICE_GROUP_ISSUE_TYPE_NAME);
    createIssueTypeRequest.setType(IssueTypeCategory.STANDARD);

    verify(jiraIssueTypeApiClient).createIssueType(atlassianHost, createIssueTypeRequest);
  }

  @Test
  public void testCreateSubtaskIssueType() {
    mockGetGroupIssueType();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final CreateIssueTypeRequest createIssueTypeRequest = new CreateIssueTypeRequest();
    createIssueTypeRequest.setName(ADVICE_ISSUE_TYPE_NAME);
    createIssueTypeRequest.setType(IssueTypeCategory.SUBTASK);

    verify(jiraIssueTypeApiClient).createIssueType(atlassianHost, createIssueTypeRequest);
  }

  @Test
  public void testUpdateIssueTypeWhenGroupIssueTypeIsMissingAndSubtaskIssueTypeMatches() {
    mockIssueType(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID, EXISTING_ADVICE_ISSUE_TYPE_ID);
    mockGetSubtaskIssueType();
    mockCreateGroupIssueType();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(NEW_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(EXISTING_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  @Test
  public void testUpdateIssueTypeWhenGroupIssueTypeIsDifferentAndSubtaskIssueTypeMatches() {
    mockIssueType(OTHER_EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID, EXISTING_ADVICE_ISSUE_TYPE_ID);
    mockGetAllJiraIssueTypes();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(EXISTING_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  @Test
  public void testUpdateIssueTypeWhenGroupIssueTypeMatchesAndSubtaskIssueTypeIsMissing() {
    mockIssueType(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID, EXISTING_ADVICE_ISSUE_TYPE_ID);
    mockGetGroupIssueType();
    mockCreateSubtaskIssueType();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(NEW_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  @Test
  public void testUpdateIssueTypeWhenGroupIssueTypeMatchesAndSubtaskIssueTypeIsDifferent() {
    mockIssueType(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID, OTHER_EXISTING_ADVICE_ISSUE_TYPE_ID);
    mockGetAllJiraIssueTypes();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(EXISTING_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  @Test
  public void testCreateIssueTypeWhenGroupIssueTypeIsMissingAndSubtaskIssueTypeExists() {
    mockEmptyIssueType();
    mockGetSubtaskIssueType();
    mockCreateGroupIssueType();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(NEW_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(EXISTING_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  @Test
  public void testCreateIssueTypeWhenGroupIssueTypeExistsAndSubtaskIssueTypeIsMissing() {
    mockEmptyIssueType();
    mockGetGroupIssueType();
    mockCreateSubtaskIssueType();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(NEW_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  @Test
  public void testCreateIssueTypeWhenJiraIssueTypesAreMissing() {
    mockEmptyIssueType();
    mockGetEmptyJiraIssueTypeList();
    mockCreateGroupIssueType();
    mockCreateSubtaskIssueType();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(NEW_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(NEW_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  @Test
  public void testCreateIssueTypeWhenJiraIssueTypesExist() {
    mockEmptyIssueType();
    mockGetAllJiraIssueTypes();

    asyncIssueTypeService.createIssueTypes(atlassianHost);

    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID);
    issueType.setAdviceIssueTypeId(EXISTING_ADVICE_ISSUE_TYPE_ID);

    verify(issueTypeRepository).save(issueType);
  }

  private void mockIssueType(final String adviceGroupIssueTypeId, final String adviceIssueTypeId) {
    final IssueType issueType = new IssueType();
    issueType.setAtlassianHostUrl(atlassianHostBaseUrl);
    issueType.setAdviceGroupIssueTypeId(adviceGroupIssueTypeId);
    issueType.setAdviceIssueTypeId(adviceIssueTypeId);

    when(issueTypeRepository.findByAtlassianHostUrl(eq(atlassianHostBaseUrl)))
        .thenReturn(Optional.of(issueType));
  }

  private void mockEmptyIssueType() {
    when(issueTypeRepository.findByAtlassianHostUrl(eq(atlassianHostBaseUrl)))
        .thenReturn(Optional.empty());
  }

  private void mockGetGroupIssueType() {
    final IssueTypeDetails groupIssueTypeDetails = new IssueTypeDetails();

    groupIssueTypeDetails.setName(ADVICE_GROUP_ISSUE_TYPE_NAME);
    groupIssueTypeDetails.setId(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID);

    final ArrayList<IssueTypeDetails> issueTypeDetails = new ArrayList<>();

    issueTypeDetails.add(groupIssueTypeDetails);

    when(jiraIssueTypeApiClient.getAllIssueTypes(atlassianHost)).thenReturn(issueTypeDetails);
  }

  private void mockGetSubtaskIssueType() {
    final IssueTypeDetails subtaskIssueTypeDetails = new IssueTypeDetails();

    subtaskIssueTypeDetails.setName(ADVICE_ISSUE_TYPE_NAME);
    subtaskIssueTypeDetails.setId(EXISTING_ADVICE_ISSUE_TYPE_ID);

    final ArrayList<IssueTypeDetails> issueTypeDetails = new ArrayList<>();

    issueTypeDetails.add(subtaskIssueTypeDetails);

    when(jiraIssueTypeApiClient.getAllIssueTypes(atlassianHost)).thenReturn(issueTypeDetails);
  }

  private void mockGetEmptyJiraIssueTypeList() {
    when(jiraIssueTypeApiClient.getAllIssueTypes(atlassianHost)).thenReturn(new ArrayList<>());
  }

  private void mockGetAllJiraIssueTypes() {
    final IssueTypeDetails groupIssueTypeDetails = new IssueTypeDetails();
    final IssueTypeDetails subtaskIssueTypeDetails = new IssueTypeDetails();

    groupIssueTypeDetails.setName(ADVICE_GROUP_ISSUE_TYPE_NAME);
    groupIssueTypeDetails.setId(EXISTING_ADVICE_GROUP_ISSUE_TYPE_ID);

    subtaskIssueTypeDetails.setName(ADVICE_ISSUE_TYPE_NAME);
    subtaskIssueTypeDetails.setId(EXISTING_ADVICE_ISSUE_TYPE_ID);

    final ArrayList<IssueTypeDetails> issueTypeDetails = new ArrayList<>();

    issueTypeDetails.add(groupIssueTypeDetails);
    issueTypeDetails.add(subtaskIssueTypeDetails);

    when(jiraIssueTypeApiClient.getAllIssueTypes(atlassianHost)).thenReturn(issueTypeDetails);
  }

  private void mockCreateGroupIssueType() {
    final CreateIssueTypeRequest createIssueTypeRequest = new CreateIssueTypeRequest();
    createIssueTypeRequest.setName(ADVICE_GROUP_ISSUE_TYPE_NAME);
    createIssueTypeRequest.setType(IssueTypeCategory.STANDARD);

    final IdentifiedJiraObject identifiedJiraObject = new IdentifiedJiraObject();
    identifiedJiraObject.setId(NEW_ADVICE_GROUP_ISSUE_TYPE_ID);
    identifiedJiraObject.setName(ADVICE_GROUP_ISSUE_TYPE_NAME);

    when(jiraIssueTypeApiClient.createIssueType(atlassianHost, createIssueTypeRequest))
        .thenReturn(identifiedJiraObject);
  }

  private void mockCreateSubtaskIssueType() {
    final CreateIssueTypeRequest createIssueTypeRequest = new CreateIssueTypeRequest();
    createIssueTypeRequest.setName(ADVICE_ISSUE_TYPE_NAME);
    createIssueTypeRequest.setType(IssueTypeCategory.SUBTASK);

    final IdentifiedJiraObject identifiedJiraObject = new IdentifiedJiraObject();
    identifiedJiraObject.setId(NEW_ADVICE_ISSUE_TYPE_ID);
    identifiedJiraObject.setName(ADVICE_ISSUE_TYPE_NAME);

    when(jiraIssueTypeApiClient.createIssueType(atlassianHost, createIssueTypeRequest))
        .thenReturn(identifiedJiraObject);
  }
}
