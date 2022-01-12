import { AxiosResponse } from 'axios';
import pluginApi from './plugin-api';
import { UserMapping } from '../model/user-mapping';

export async function getUserMapping(): Promise<UserMapping> {
  const response: AxiosResponse<UserMapping> = await pluginApi.get<UserMapping>(
    '/v1/usermappings/current',
  );
  return response.data;
}

export async function storeUserMapping(userMapping: UserMapping): Promise<void> {
  const response: AxiosResponse<void> = await pluginApi.put<void>(
    '/v1/usermappings/current',
    userMapping,
  );
  return response.data;
}
