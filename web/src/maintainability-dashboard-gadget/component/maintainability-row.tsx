import React from 'react';
import MaintainabilityRowType from '../../shared/enum/maintainability-row-type';
import { RowType } from '../../shared/type/atlaskit/atlaskit.dynamic-table.types';

interface NameCellProps {
  projectName: string;
  branchName: string;
  type: MaintainabilityRowType;
}

interface MaintainabilityCellProps {
  maintainabilityIndex: number | null | undefined;
  type: MaintainabilityRowType;
}

function NameCell({ projectName, branchName, type }: NameCellProps): JSX.Element {
  return (
    <span style={{ marginLeft: type === MaintainabilityRowType.BRANCH ? 20 : 0 }}>
      {type === MaintainabilityRowType.PROJECT ? projectName : branchName}
    </span>
  );
}

function MaintainabilityCell({
  type,
  maintainabilityIndex,
}: MaintainabilityCellProps): JSX.Element {
  const getMaintainabilityStyle = () => {
    let color;
    let backgroundColor;

    if (!maintainabilityIndex) {
      color = 'gray';
      backgroundColor = 'transparent';
    } else if (maintainabilityIndex <= 3.33) {
      color = '#ef3e23';
      backgroundColor = '#fde5e1';
    } else if (maintainabilityIndex > 3.33 && maintainabilityIndex <= 6.66) {
      color = '#fbaa19';
      backgroundColor = '#fef4e2';
    } else if (maintainabilityIndex > 6.66) {
      color = '#35b37e';
      backgroundColor = '#c6eedd';
    }

    return {
      color,
      backgroundColor,
    };
  };

  const { color, backgroundColor } = getMaintainabilityStyle();

  const maintainabilityStyle = {
    borderRadius: 3,
    paddingTop: 3,
    paddingBottom: 3,
    paddingLeft: 9,
    paddingRight: 9,
    marginLeft: type === MaintainabilityRowType.BRANCH ? 20 : 0,
    color,
    backgroundColor,
  };

  return (
    <span style={maintainabilityStyle}>
      {maintainabilityIndex ? maintainabilityIndex.toFixed(2) : 'N/A'}
    </span>
  );
}

function MaintainabilityRow(
  projectName: string,
  branchName: string,
  maintainabilityIndex: number | null | undefined,
  type: MaintainabilityRowType,
): RowType {
  const nameCellProps: NameCellProps = {
    projectName,
    branchName,
    type,
  };

  const maintainabilityCellProps: MaintainabilityCellProps = {
    maintainabilityIndex,
    type,
  };

  return {
    cells: [
      { content: <NameCell {...nameCellProps} /> },
      { content: <MaintainabilityCell {...maintainabilityCellProps} /> },
    ],
  };
}

export default MaintainabilityRow;
