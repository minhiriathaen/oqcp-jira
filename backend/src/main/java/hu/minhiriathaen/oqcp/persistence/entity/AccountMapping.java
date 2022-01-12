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
@Generated
@Entity
@EqualsAndHashCode(of = {"openQualityCheckerAccountName", "atlassianHostUrl"})
@Table(
    name = AccountMapping.TABLE_NAME,
    uniqueConstraints = {
      @UniqueConstraint(
          name = AccountMapping.CONSTRAINT_OPEN_QUALITY_CHECKER_ACCOUNT_NAME,
          columnNames = {AccountMapping.COLUMN_OPEN_QUALITY_CHECKER_ACCOUNT_NAME}),
      @UniqueConstraint(
          name = AccountMapping.CONSTRAINT_ATLASSIAN_HOST_URL,
          columnNames = {AccountMapping.COLUMN_ATLASSIAN_HOST_URL})
    })
public class AccountMapping {

  public static final String TABLE_NAME = "account_mapping";

  public static final String CONSTRAINT_OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "UNIQUE__ACCOUNT_MAPPING__OPEN_QUALITY_CHECKER_ACCOUNT_NAME";

  public static final String CONSTRAINT_ATLASSIAN_HOST_URL =
      "UNIQUE__ACCOUNT_MAPPING__ATLASSIAN_HOST_URL";

  public static final String COLUMN_OPEN_QUALITY_CHECKER_ACCOUNT_NAME =
      "open_quality_checker_account_name";

  public static final String COLUMN_ATLASSIAN_HOST_URL = "atlassian_host_url";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id; // NOPMD

  @NotEmpty
  @Column(name = COLUMN_OPEN_QUALITY_CHECKER_ACCOUNT_NAME)
  private String openQualityCheckerAccountName;

  @NotEmpty
  @Column(name = COLUMN_ATLASSIAN_HOST_URL)
  private String atlassianHostUrl;
}
