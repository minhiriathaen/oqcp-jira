package hu.minhiriathaen.oqcp.api.advice.v1;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Generated
public class ContributorWrapper {

  private final List<ContributorTransfer> contributors = new ArrayList<>();

  @JsonAnySetter
  public void addContributor(final String key, final Object value) {
    if (value instanceof Number) {
      contributors.add(new ContributorTransfer(key, (Number) value));
    } else {
      final String logMessage =
          "Unable to resolve number of contributions '{}' "
              + "because of invalid value type: '{}', value: '{}'";
      log.warn(logMessage, key, value.getClass(), value);
    }
  }
}
