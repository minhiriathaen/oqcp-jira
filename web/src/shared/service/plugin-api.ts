import axios from 'axios';
import { AtlassianConnectApi } from '../type/atlassian-connect.types';

declare let AP: AtlassianConnectApi;

const pluginApi = axios.create({
  baseURL: '/api',
});

pluginApi.interceptors.request.use(async (config) => {
  const token = await AP.context.getToken();

  return {
    ...config,
    headers: { ...config.headers, Authorization: `JWT ${token}` },
  };
});

export default pluginApi;
