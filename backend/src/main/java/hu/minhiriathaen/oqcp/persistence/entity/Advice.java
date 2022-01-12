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
@Entity
@Generated
@Table(
    name = Advice.TABLE_NAME,
    uniqueConstraints = {
      @UniqueConstraint(
          name = Advice.CONSTRAINT_ADVICE,
          columnNames = {
            Advice.COLUMN_GROUP_ID,
            Advice.COLUMN_ADVICE_ID,
            Advice.COLUMN_JIRA_ISSUE_ID
          })
    })
@EqualsAndHashCode(of = {"group", "adviceId", "jiraIssueId"})
public class Advice {

  public static final String TABLE_NAME = "advice";

  public static final String COLUMN_GROUP_ID = "group_id";

  public static final String COLUMN_ADVICE_ID = "advice_id";

  public static final String COLUMN_JIRA_ISSUE_ID = "jira_issue_id";

  public static final String CONSTRAINT_ADVICE = "UNIQUE__ADVICE__GROUP_ID__ADVICE_ID__PROJECT_ID";

  public static final String FOREIGN_ADVICE_GROUP_ID = "FOREIGN__ADVICE__ADVICE_GROUP_ID";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // NOPMD

  @NotNull
  @ManyToOne
  @JoinColumn(
      name = COLUMN_GROUP_ID,
      foreignKey = @ForeignKey(name = FOREIGN_ADVICE_GROUP_ID),
      nullable = false)
  private AdviceGroup group;

  @NotEmpty
  @Column(name = COLUMN_ADVICE_ID)
  private String adviceId;

  @NotEmpty
  @Column(name = COLUMN_JIRA_ISSUE_ID)
  private String jiraIssueId;
}
