export interface Context {
  getToken: () => Promise<string>;
  getContext: () => Promise<JiraContext>;
}

export interface JiraProject {
  id: string;
  key: string;
}

export interface ProjectContext {
  project: JiraProject;
}

export interface JiraContext {
  jira: ProjectContext;
}

export interface AtlassianConnectApi {
  context: Context;
}
