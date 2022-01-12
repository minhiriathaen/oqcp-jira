import { BranchMaintainability } from './branch-maintainability';

export interface ProjectMaintainability {
  id: string;
  name: string;
  mainBranchName: string;
  maintainabilityIndex: number | null | undefined;
  branches: BranchMaintainability[];
}
