package hu.minhiriathaen.oqcp.persistence.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;

@Data
@Entity
@Generated
@Table(
    name = AdviceGroup.TABLE_NAME,
    uniqueConstraints = {
      @UniqueConstraint(
          name = AdviceGroup.CONSTRAINT_ADVICE_GROUP,
          columnNames = {
            AdviceGroup.COLUMN_OPEN_QUALITY_CHECKER_PROJECT_ID,
            AdviceGroup.COLUMN_BRANCH_NAME,
            AdviceGroup.COLUMN_CONTRIBUTOR,
            AdviceGroup.COLUMN_JIRA_ISSUE_ID
          })
    })
@EqualsAndHashCode(of = {"openQualityCheckerProjectId", "branchName", "contributor", "jiraIssueId"})
public class AdviceGroup {

  public static final String TABLE_NAME = "advice_group";

  public static final String COLUMN_OPEN_QUALITY_CHECKER_PROJECT_ID =
      "open_quality_checker_project_id";

  public static final String COLUMN_BRANCH_NAME = "branch_name";

  public static final String COLUMN_CONTRIBUTOR = "contributor";

  public static final String COLUMN_JIRA_PROJECT_ID = "jira_project_id";

  public static final String COLUMN_JIRA_ISSUE_ID = "jira_issue_id";

  public static final String COLUMN_CREATED_AT = "created_at";

  public static final String CONSTRAINT_ADVICE_GROUP =
      "UNIQUE__ADVICE_GROUP__OPEN_QUALITY_CHECKER_PROJECT_ID__BRANCH_NAME__CONTRIBUTOR__JIRA_ISSUE_ID";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // NOPMD

  @NotEmpty
  @Column(name = COLUMN_OPEN_QUALITY_CHECKER_PROJECT_ID)
  private String openQualityCheckerProjectId;

  @NotEmpty
  @Column(name = COLUMN_BRANCH_NAME)
  private String branchName;

  @Column(name = COLUMN_CONTRIBUTOR)
  private String contributor;

  @NotEmpty
  @Column(name = COLUMN_JIRA_PROJECT_ID)
  private String jiraProjectId;

  @NotEmpty
  @Column(name = COLUMN_JIRA_ISSUE_ID)
  private String jiraIssueId;

  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = COLUMN_CREATED_AT)
  private Date createdAt;
}
