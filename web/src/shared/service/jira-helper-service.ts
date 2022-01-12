/* eslint-disable import/prefer-default-export */
import { AtlassianConnectApi, JiraContext, JiraProject } from '../type/atlassian-connect.types';

declare let AP: AtlassianConnectApi;

export async function getCurrentProject(): Promise<JiraProject> {
  const context: JiraContext = await AP.context.getContext();
  return context?.jira?.project;
}
