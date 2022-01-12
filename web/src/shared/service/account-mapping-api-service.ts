import { AxiosResponse } from 'axios';
import pluginApi from './plugin-api';
import { AccountMapping } from '../model/account-mapping';

export async function getAccountMapping(): Promise<AccountMapping> {
  const response: AxiosResponse<AccountMapping> = await pluginApi.get<AccountMapping>(
    '/v1/accountmappings/current',
  );
  return response.data;
}

export async function storeAccountMapping(accountMapping: AccountMapping): Promise<void> {
  const response: AxiosResponse<void> = await pluginApi.put<void>(
    '/v1/accountmappings/current',
    accountMapping,
  );
  return response.data;
}
