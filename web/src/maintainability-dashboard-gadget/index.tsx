import React from 'react';
import ReactDOM from 'react-dom';
import '@atlaskit/css-reset';
import '../index.css';
import MaintainabilityDashboardGadget from './component/maintainability-dashboard-gadget';

ReactDOM.render(
  <React.StrictMode>
    <MaintainabilityDashboardGadget />
  </React.StrictMode>,
  document.getElementById('root'),
);
