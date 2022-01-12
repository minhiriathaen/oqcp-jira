package hu.minhiriathaen.oqcp.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;

@Data
@Entity
@Generated
@Table(
    name = IssueType.TABLE_NAME,
    uniqueConstraints = {
      @UniqueConstraint(
          name = IssueType.CONSTRAINT_ISSUE_TYPE,
          columnNames = {IssueType.COLUMN_ATLASSIAN_HOST_URL})
    })
@EqualsAndHashCode(of = {"atlassianHostUrl", "adviceGroupIssueTypeId", "adviceIssueTypeId"})
public class IssueType {

  public static final String TABLE_NAME = "issue_type";

  public static final String COLUMN_ATLASSIAN_HOST_URL = "atlassian_host_url";

  public static final String COLUMN_ADVICE_GROUP_ISSUE_TYPE_ID = "advice_group_issue_type_id";

  public static final String COLUMN_ADVICE_ISSUE_TYPE_ID = "advice_issue_type_id";

  public static final String CONSTRAINT_ISSUE_TYPE = "UNIQUE__ISSUE_TYPE__ATLASSIAN_HOST_URL";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // NOPMD

  @NotEmpty
  @Column(name = COLUMN_ATLASSIAN_HOST_URL)
  private String atlassianHostUrl;

  @NotEmpty
  @Column(name = COLUMN_ADVICE_GROUP_ISSUE_TYPE_ID)
  private String adviceGroupIssueTypeId;

  @NotEmpty
  @Column(name = COLUMN_ADVICE_ISSUE_TYPE_ID)
  private String adviceIssueTypeId;
}
