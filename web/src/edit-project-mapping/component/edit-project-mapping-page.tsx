import React, { useEffect, useState } from 'react';
import { FormFooter } from '@atlaskit/form';
import ErrorCodes from '../../shared/error/error-message';
import SectionMessageBox, { SectionMessageData } from '../../shared/component/section-message-box';
import Wrapper from '../../shared/component/wrapper';
import LoadingIndicator from '../../shared/component/loading-indicator';
import PrimaryButton from '../../shared/component/primary-button';
import { getErrorCode } from '../../shared/error/error.helper';
import { ProjectMapping } from '../../shared/model/project-mapping';
import { OpenQualityCheckerProject } from '../../shared/model/open-quality-checker-project';
import { getProjects } from '../../shared/service/open-quality-checker-project-api-service';
import {
  getProjectMapping,
  storeProjectMapping,
} from '../../shared/service/project-mapping-api-service';
import ProjectSelector from './project-selector';

const defaultAvailableProjects: OpenQualityCheckerProject[] = [];

const defaultProjectMapping: ProjectMapping = {
  openQualityCheckerProjectIds: [],
};

function getAlreadyMappedProjectNames(
  errorCode: keyof typeof ErrorCodes,
  alreadyMappedProjectIds: string[],
  availableProjects: OpenQualityCheckerProject[],
) {
  if (errorCode === 'OPEN_QUALITY_CHECKER_PROJECTS_ALREADY_MAPPED') {
    const alreadyMappedProjectsName: string[] = [];

    alreadyMappedProjectIds.forEach((id) => {
      availableProjects.forEach((project) => {
        if (id === project.id) {
          alreadyMappedProjectsName.push(project.name);
        }
      });
    });

    return alreadyMappedProjectsName;
  }
  return [];
}

function EditProjectMappingPage(): JSX.Element {
  const [availableProjects, setAvailableProjects] = useState<OpenQualityCheckerProject[]>(
    defaultAvailableProjects,
  );
  const [projectMapping, setProjectMapping] = useState<ProjectMapping>(defaultProjectMapping);
  const [loading, setLoading] = useState(true);

  const [saving, setSaving] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [sectionMessageData, setSectionMessageData] = useState<SectionMessageData | null>();

  async function load() {
    try {
      const getProjectsResult: OpenQualityCheckerProject[] = await getProjects();
      setAvailableProjects(getProjectsResult);

      setLoading(false);

      if (getProjectsResult.length === 0) {
        setSectionMessageData({
          title: 'Info',
          appereance: undefined,
          body: 'No OpenQualityChecker projects are available',
        });

        setDisableButton(true);
      } else {
        const getProjectMappingResult = await getProjectMapping();
        setProjectMapping(getProjectMappingResult);
      }

      setShowForm(true);
      setLoading(false);
    } catch (error) {
      const errorCode = getErrorCode(error, 'CONNECTION_ERROR');

      const errors: string[] = [
        'CONNECTION_ERROR',
        'ACCOUNT_MAPPING_NOT_FOUND',
        'USER_MAPPING_NOT_FOUND',
      ];
      setShowForm(!errors.includes(errorCode));

      setLoading(false);
      setDisableButton(true);

      setSectionMessageData({
        title: 'Warning',
        appereance: 'warning',
        body: ErrorCodes[errorCode],
      });
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function save(updatedProjectMapping: ProjectMapping) {
    setSaving(true);
    setSectionMessageData(null);

    try {
      await storeProjectMapping(updatedProjectMapping);

      setSaving(false);

      setSectionMessageData({
        title: 'Success',
        appereance: 'confirmation',
        body: 'OpenQualityChecker projects has been saved successfully',
      });
    } catch (error) {
      setSaving(false);

      const errorCode = getErrorCode(error, 'UNKNOWN_ERROR');
      const alreadyMappedProjectIds: string[] =
        error.response?.data?.mappedOpenQualityCheckerProjectIds;

      const alreadyMappedProjectsName: string[] = getAlreadyMappedProjectNames(
        errorCode,
        alreadyMappedProjectIds,
        availableProjects,
      );

      setSectionMessageData({
        title: 'An error occurred while saving the changes',
        appereance: 'error',
        body: ErrorCodes[errorCode] + alreadyMappedProjectsName.map((name) => ` ${name}`),
      });
    }
  }

  function onSelect(id: string) {
    const newProjectMapping: ProjectMapping = { ...projectMapping };

    newProjectMapping.openQualityCheckerProjectIds.push(id);

    setProjectMapping(newProjectMapping);
  }

  function onDeselect(id: string) {
    const newProjectMapping = { ...projectMapping };

    newProjectMapping.openQualityCheckerProjectIds.splice(
      projectMapping?.openQualityCheckerProjectIds.findIndex((projectId) => projectId === id),
      1,
    );

    setProjectMapping(newProjectMapping);
  }

  if (loading) {
    return <LoadingIndicator />;
  }
  return (
    <Wrapper paddingTop="48px" alignItems="center">
      <h2>OpenQualityChecker</h2>

      <Wrapper width="450px">
        {showForm && (
          <>
            <h3>Available OpenQualityChecker projects</h3>

            <ProjectSelector
              projects={availableProjects}
              selectedProjectIds={[...projectMapping.openQualityCheckerProjectIds]}
              onDeselect={onDeselect}
              onSelect={onSelect}
            />

            <FormFooter>
              <PrimaryButton
                type="button"
                isDisabled={saving || disableButton}
                showSpinner={saving}
                onClick={() => save(projectMapping)}
              >
                Save
              </PrimaryButton>
            </FormFooter>
          </>
        )}

        {sectionMessageData && <SectionMessageBox {...sectionMessageData} />}
      </Wrapper>
    </Wrapper>
  );
}

export default EditProjectMappingPage;
