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
@EqualsAndHashCode(of = {"accountMapping", "openQualityCheckerUserToken", "atlassianUserAccountId"})
@Table(
    name = UserMapping.TABLE_NAME,
    uniqueConstraints = {
      @UniqueConstraint(
          name = UserMapping.CONSTRAINT_ACCOUNT_USER_MAPPING,
          columnNames = {
            UserMapping.COLUMN_ACCOUNT_MAPPING_ID,
            UserMapping.COLUMN_ATLASSIAN_USER_ACCOUNT_ID
          })
    })
public class UserMapping {

  public static final String TABLE_NAME = "user_mapping";

  public static final String CONSTRAINT_ACCOUNT_USER_MAPPING =
      "UNIQUE__USER_MAPPING__ACCOUNT_MAPPING_ID__ATLASSIAN_USER_ACCOUNT_ID";

  public static final String FOREIGN_ACCOUNT_MAPPING = "FOREIGN__USER_MAPPING__ACCOUNT_MAPPING";

  public static final String COLUMN_ACCOUNT_MAPPING_ID = "account_mapping_id";

  public static final String COLUMN_OPEN_QUALITY_CHECKER_USER_TOKEN =
      "open_quality_checker_user_token";

  public static final String COLUMN_ATLASSIAN_USER_ACCOUNT_ID = "atlassian_user_account_id";

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
  @Column(name = COLUMN_OPEN_QUALITY_CHECKER_USER_TOKEN)
  private String openQualityCheckerUserToken;

  @NotEmpty
  @Column(name = COLUMN_ATLASSIAN_USER_ACCOUNT_ID)
  private String atlassianUserAccountId;
}
