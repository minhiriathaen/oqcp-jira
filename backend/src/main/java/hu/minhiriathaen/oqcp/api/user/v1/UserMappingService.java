package hu.minhiriathaen.oqcp.api.user.v1;

import com.atlassian.connect.spring.AtlassianHostUser;

public interface UserMappingService {

  UserMappingTransfer getUserMapping(AtlassianHostUser atlassianHostUser);

  void storeUserMapping(
      AtlassianHostUser atlassianHostUser, UserMappingTransfer userMappingTransfer);
}
