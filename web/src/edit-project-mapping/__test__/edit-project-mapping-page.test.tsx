/* eslint-disable sonarjs/no-duplicate-string */
import {configure, mount, ReactWrapper} from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import LoadingIndicator from '../../shared/component/loading-indicator';
import {mocked} from 'ts-jest/utils'
import {act} from 'react-dom/test-utils';
import PrimaryButton from '../../shared/component/primary-button';
import EditProjectMappingPage from '../component/edit-project-mapping-page';
import {getProjectMapping} from '../../shared/service/project-mapping-api-service';
import {getProjects} from '../../shared/service/open-quality-checker-project-api-service';
import {OpenQualityCheckerProject} from '../../shared/model/open-quality-checker-project';
import {ProjectMapping} from '../../shared/model/project-mapping';
import ProjectSelector from '../component/project-selector';
import {FormFooter} from '@atlaskit/form';

jest.mock('../../shared/service/project-mapping-api-service')
const mockedGetProjectMapping = mocked(getProjectMapping)

jest.mock('../../shared/service/open-quality-checker-project-api-service')
const mockedGetProjects = mocked(getProjects)

jest.mock('../../shared/error/error.helper')

configure({adapter: new Adapter()});

describe('EditProjectMappingPage', () => {
  let component: ReactWrapper;


  beforeEach(async () => {
    const mockProjects: OpenQualityCheckerProject[] = [];
    const mockProjectMappings: ProjectMapping = {
      openQualityCheckerProjectIds: []
    };

    mockedGetProjects.mockResolvedValue(mockProjects);
    mockedGetProjectMapping.mockResolvedValue(mockProjectMappings);

    await act(async () => {
      component = mount(<EditProjectMappingPage/>);
    })

    await act(async () => {
      await new Promise(resolve => setImmediate(resolve));

      component.update();
    });
  })

  it('should render a LoadingIndicator when loads', async () => {

    await act(async () => {
      component = mount(<EditProjectMappingPage/>);
    })

    expect(component.find(LoadingIndicator).exists()).toBeTruthy();
  });

  describe('after a success getProjects & getProjectMapping rest api call', () => {

    it('should render an input tag', async () => {
      expect(component.find(ProjectSelector).exists()).toBeTruthy();
    });

    it('should render a FormFooter', () => {
      expect(component.find(FormFooter).exists()).toBeTruthy();
    });

    it('should render a disabled PrimaryButton', () => {
      const primaryButton = component.find(PrimaryButton);
      expect(primaryButton.exists()).toBeTruthy();
      expect(primaryButton.props().isDisabled).toBeTruthy();
    });

    it('should render an enabled PrimaryButton', async () => {
      const mockProjects: OpenQualityCheckerProject[] = [];
      mockProjects.push({id: 'project_id_1', name: 'Project 1'});
      mockProjects.push({id: 'project_id_2', name: 'Project 2'});
      mockProjects.push({id: 'project_id_3', name: 'Project 3'});

      mockedGetProjects.mockResolvedValue(mockProjects);

      await act(async () => {
        component = mount(<EditProjectMappingPage/>);
      })

      await act(async () => {
        await new Promise(resolve => setImmediate(resolve));

        component.update();
      });

      const primaryButton = component.find(PrimaryButton);
      expect(primaryButton.exists()).toBeTruthy();
      expect(primaryButton.props().isDisabled).toBeFalsy();
    });
  });
});
