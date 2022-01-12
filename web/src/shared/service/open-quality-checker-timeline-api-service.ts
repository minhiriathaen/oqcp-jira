/* eslint-disable import/prefer-default-export */
import { AxiosResponse } from 'axios';
import pluginApi from './plugin-api';

export async function getTimeline(projectName: string, branchName: string): Promise<string> {
  const response: AxiosResponse<string> = await pluginApi.get<string>(
    `/v1/projects/${projectName}/branches/${branchName}/timeline`,
  );
  return response.data;
}
