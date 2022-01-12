import React, { useEffect, useState } from 'react';
import SectionMessageBox, { SectionMessageData } from '../../shared/component/section-message-box';
import { getErrorCode } from '../../shared/error/error.helper';
import { ProjectMapping } from '../../shared/model/project-mapping';
import { UserMapping } from '../../shared/model/user-mapping';
import { getProjectMapping } from '../../shared/service/project-mapping-api-service';
import { getUserMapping } from '../../shared/service/user-mapping-api-service';
import ErrorCodes from '../../shared/error/error-message';
import LoadingIndicator from '../../shared/component/loading-indicator';
import Wrapper from '../../shared/component/wrapper';
import OpenQualityCheckerViewer from './open-quality-checker-viewer';

function ViewOpenQualityCheckerPage(): JSX.Element {
  const [userMapping, setUserMapping] = useState<UserMapping>();
  const [projectMapping, setProjectMapping] = useState<ProjectMapping>();
  const [loading, setLoading] = useState(true);
  const [sectionMessageData, setSectionMessageData] = useState<SectionMessageData | null>();

  async function load() {
    try {
      const getUserMappingResult: UserMapping = await getUserMapping();

      if (!getUserMappingResult.openQualityCheckerUserToken) {
        setSectionMessageData({
          title: 'Warning',
          appereance: 'warning',
          body: ErrorCodes.USER_MAPPING_NOT_FOUND,
        });
      } else {
        setUserMapping(getUserMappingResult);

        const getProjectMappingResult = await getProjectMapping();

        if (!getProjectMappingResult.openQualityCheckerProjectIds.length) {
          setSectionMessageData({
            title: 'Warning',
            appereance: 'warning',
            body: ErrorCodes.PROJECT_MAPPING_NOT_FOUND,
          });
        } else {
          setProjectMapping(getProjectMappingResult);
        }
      }
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
    <Wrapper paddingTop="48px" paddingBottom="48px" alignItems="center">
      {sectionMessageData ? (
        <Wrapper width="450px">
          <SectionMessageBox {...sectionMessageData} />
        </Wrapper>
      ) : (
        <OpenQualityCheckerViewer
          userToken={userMapping?.openQualityCheckerUserToken}
          projectIds={projectMapping?.openQualityCheckerProjectIds}
        />
      )}
    </Wrapper>
  );
}

export default ViewOpenQualityCheckerPage;
