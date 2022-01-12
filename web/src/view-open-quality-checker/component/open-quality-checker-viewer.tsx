import React from 'react';
import Wrapper from '../../shared/component/wrapper';

interface OpenQualityCheckerViewerProps {
  userToken: string | undefined;
  projectIds: string[] | undefined;
}

function OpenQualityCheckerViewer({
  userToken,
  projectIds,
}: OpenQualityCheckerViewerProps): JSX.Element {
  function createUrl() {
    const OQC_BASE_URL = process.env.REACT_APP_OQC_BASE_URL;

    let oqcUrl = `${OQC_BASE_URL}/dashboard/?iframe=1&api_token=${userToken}`;

    if (projectIds) {
      projectIds.forEach((projectId) => {
        oqcUrl += `&projectId=${projectId}`;
      });
    }

    console.log('Generated OQC Url: >>>', oqcUrl);

    return oqcUrl;
  }

  return (
    <Wrapper alignItems="center">
      <h2>OpenQualityChecker Projects</h2>
      <iframe
        src={createUrl()}
        title="openQualityCheckerViewer"
        style={{ width: window.innerWidth, height: window.innerHeight }}
      />
    </Wrapper>
  );
}

export default OpenQualityCheckerViewer;
