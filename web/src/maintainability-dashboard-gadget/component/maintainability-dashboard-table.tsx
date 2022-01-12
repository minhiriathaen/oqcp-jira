import React from 'react';
import { DynamicTableStateless } from '@atlaskit/dynamic-table';
import StarFilledIcon from '@atlaskit/icon/glyph/star-filled';
import { ProjectMaintainability } from '../../shared/model/project-maintainability';
import { BranchMaintainability } from '../../shared/model/branch-maintainability';
import MaintainabilityRow from './maintainability-row';
import MaintainabilityRowType from '../../shared/enum/maintainability-row-type';
import { RowType } from '../../shared/type/atlaskit/atlaskit.dynamic-table.types';

interface MaintainabilityDashboardTableProps {
  projects: ProjectMaintainability[];
}

function MaintainabilityDashboardTable({
  projects,
}: MaintainabilityDashboardTableProps): JSX.Element {
  const caption = 'Projects';

  const tableHeader = {
    cells: [
      {
        content: <span style={{ lineHeight: '24px' }}>Name</span>,
        isSortable: false,
      },
      {
        content: (
          <span style={{ display: 'flex', alignItems: 'center', margin: 'auto' }}>
            <StarFilledIcon label="" />
            <span>Maintainability</span>
          </span>
        ),
        isSortable: false,
      },
    ],
  };

  const createRows = () => {
    const rows: RowType[] = [];

    projects.forEach((project: ProjectMaintainability) => {
      rows.push(
        MaintainabilityRow(
          project.name,
          project.mainBranchName,
          project.maintainabilityIndex,
          MaintainabilityRowType.PROJECT,
        ),
      );

      project.branches.forEach((branch: BranchMaintainability) => {
        rows.push(
          MaintainabilityRow(
            project.name,
            branch.name,
            branch.maintainabilityIndex,
            MaintainabilityRowType.BRANCH,
          ),
        );
      });
    });

    return rows;
  };

  return <DynamicTableStateless caption={caption} head={tableHeader} rows={createRows()} />;
}

export default MaintainabilityDashboardTable;
