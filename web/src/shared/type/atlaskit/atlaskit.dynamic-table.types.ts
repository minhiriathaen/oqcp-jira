import React, { RefObject } from 'react';

export interface RowCellType {
  key?: string | number;
  colSpan?: number;
  content?: React.ReactNode | string;
  testId?: string;
}

export declare type RowType = {
  cells: Array<RowCellType>;
  key?: string;
  onClick?: React.MouseEventHandler;
  onKeyPress?: React.KeyboardEventHandler;
  testId?: string;
  innerRef?: RefObject<HTMLElement>;
};
