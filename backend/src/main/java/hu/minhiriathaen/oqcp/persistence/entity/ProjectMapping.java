package hu.minhiriathaen.oqcp.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;

@Data
@Generated
@Entity
@EqualsAndHashCode(of = {"accountMapping", "openQualityCheckerProjectId", "jiraProjectId"})
@Table(
    name = ProjectMapping.TABLE_NAME,
    uniqueConstraints = {
      @UniqueConstraint(
          name = ProjectMapping.CONSTRAINT_OPEN_QUALITY_CHECKER_PROJECT_ID,
          columnNames = {ProjectMapping.COLUMN_OPEN_QUALITY_CHECKER_PROJECT_ID})
    })
public class ProjectMapping {

  public static final String TABLE_NAME = "project_mapping";

  public static final String CONSTRAINT_OPEN_QUALITY_CHECKER_PROJECT_ID =
      "UNIQUE__PROJECT_MAPPING__OPEN_QUALITY_CHECKER_PROJECT_ID";

  public static final String COLUMN_OPEN_QUALITY_CHECKER_PROJECT_ID =
      "open_quality_checker_project_id";

  public static final String COLUMN_ACCOUNT_MAPPING_ID = "account_mapping_id";

  public static final String FOREIGN_ACCOUNT_MAPPING = "FOREIGN__PROJECT_MAPPING__ACCOUNT_MAPPING";

  public static final String COLUMN_JIRA_PROJECT_ID = "jira_project_id";

  public static final String COLUMN_CREATOR_ATLASSIAN_USER_ACCOUNT_ID =
      "creator_atlassian_user_account_id";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // NOPMD

  @NotNull
  @ManyToOne
  @JoinColumn(
      name = COLUMN_ACCOUNT_MAPPING_ID,
      foreignKey = @ForeignKey(name = FOREIGN_ACCOUNT_MAPPING),
      nullable = false)
  private AccountMapping accountMapping;

  @NotEmpty
  @Column(name = COLUMN_OPEN_QUALITY_CHECKER_PROJECT_ID)
  private String openQualityCheckerProjectId;

  @NotEmpty
  @Column(name = COLUMN_JIRA_PROJECT_ID)
  private String jiraProjectId;

  @NotEmpty
  @Column(name = COLUMN_CREATOR_ATLASSIAN_USER_ACCOUNT_ID)
  private String creatorAtlassianUserAccountId;
}
