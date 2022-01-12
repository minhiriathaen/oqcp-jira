import { AxiosResponse } from 'axios';
import pluginApi from './plugin-api';
import { ProjectMapping } from '../model/project-mapping';
import { getCurrentProject } from './jira-helper-service';

export async function getProjectMapping(): Promise<ProjectMapping> {
  const currentJiraProject = await getCurrentProject();

  const response: AxiosResponse<ProjectMapping> = await pluginApi.get<ProjectMapping>(
    `/v1/projectmappings/${currentJiraProject.id}`,
  );

  return response.data;
}

export async function storeProjectMapping(projectMapping: ProjectMapping): Promise<void> {
  const currentJiraProject = await getCurrentProject();

  const response: AxiosResponse<void> = await pluginApi.put<void>(
    `/v1/projectmappings/${currentJiraProject.id}`,
    projectMapping,
  );

  return response.data;
}
