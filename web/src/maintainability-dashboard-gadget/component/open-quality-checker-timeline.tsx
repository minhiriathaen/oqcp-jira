import React, { useEffect, useState } from 'react';
import SVG from 'react-inlinesvg';
import SectionMessageBox, { SectionMessageData } from '../../shared/component/section-message-box';
import { getErrorCode } from '../../shared/error/error.helper';
import { getTimeline } from '../../shared/service/open-quality-checker-timeline-api-service';
import ErrorCodes from '../../shared/error/error-message';
import LoadingIndicator from '../../shared/component/loading-indicator';
import Wrapper from '../../shared/component/wrapper';

interface OpenQualityCheckerTimelineProps {
  projectName: string;
  branchName: string;
}

function OpenQualityCheckerTimeline({
  projectName,
  branchName,
}: OpenQualityCheckerTimelineProps): JSX.Element {
  const [timelineSvg, setTimelineSvg] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [sectionMessageData, setSectionMessageData] = useState<SectionMessageData | null>();

  async function load() {
    try {
      const getTimelineResult: string = await getTimeline(projectName, branchName);

      setTimelineSvg(getTimelineResult);
    } catch (error) {
      setSectionMessageData({
        title: 'Warning',
        appereance: 'warning',
        body: ErrorCodes[getErrorCode(error, 'CONNECTION_ERROR')],
      });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  });

  if (loading) {
    return <LoadingIndicator />;
  }
  return (
    <Wrapper alignItems="center">
      <Wrapper width="480px">
        {sectionMessageData ? (
          <SectionMessageBox {...sectionMessageData} />
        ) : (
          <SVG src={timelineSvg} />
        )}
      </Wrapper>
    </Wrapper>
  );
}

export default OpenQualityCheckerTimeline;
