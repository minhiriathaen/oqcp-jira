/* eslint-disable import/prefer-default-export */
import { AxiosResponse } from 'axios';
import pluginApi from './plugin-api';
import { ProjectMaintainability } from '../model/project-maintainability';

export async function getMaintainabilities(): Promise<ProjectMaintainability[]> {
  const response: AxiosResponse<ProjectMaintainability[]> = await pluginApi.get<
    ProjectMaintainability[]
  >('/v1/projects/maintainabilities');
  return response.data;
}
