import React, { useState, useEffect } from 'react';
import { getMaintainabilities } from '../../shared/service/maintainability-api-service';
import LoadingIndicator from '../../shared/component/loading-indicator';
import { ProjectMaintainability } from '../../shared/model/project-maintainability';
import MaintainabilityDashboardTable from './maintainability-dashboard-table';
import Wrapper from '../../shared/component/wrapper';
import { getErrorCode } from '../../shared/error/error.helper';
import ErrorCodes from '../../shared/error/error-message';
import SectionMessageBox, { SectionMessageData } from '../../shared/component/section-message-box';

function MaintainabilityDashboardGadget(): JSX.Element {
  const [projectMaintainabilities, setProjectMaintainabilities] = useState<
    ProjectMaintainability[]
  >([]);
  const [loading, setLoading] = useState(true);
  const [sectionMessageData, setSectionMessageData] = useState<SectionMessageData | null>();

  async function load() {
    try {
      const getMaintainabilitiesResult: ProjectMaintainability[] = await getMaintainabilities();
      setProjectMaintainabilities(getMaintainabilitiesResult);
    } catch (error) {
      const errorCode = getErrorCode(error, 'CONNECTION_ERROR');

      setSectionMessageData({
        title: 'Warning',
        appereance: 'warning',
        body: ErrorCodes[errorCode],
      });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  if (loading) {
    return <LoadingIndicator />;
  }

  return (
    <>
      {sectionMessageData ? (
        <Wrapper width="450px">
          <SectionMessageBox {...sectionMessageData} />
        </Wrapper>
      ) : (
        <MaintainabilityDashboardTable projects={projectMaintainabilities} />
      )}
    </>
  );
}

export default MaintainabilityDashboardGadget;
