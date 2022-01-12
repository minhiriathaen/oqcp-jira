import {configure, mount, ReactWrapper} from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import LoadingIndicator from '../../shared/component/loading-indicator';
import {mocked} from 'ts-jest/utils'
import {act} from 'react-dom/test-utils';
import PrimaryButton from '../../shared/component/primary-button';
import {getErrorCode} from '../../shared/error/error.helper';
import SectionMessageBox from '../../shared/component/section-message-box';
import ErrorCodes from '../../shared/error/error-message';
import EditUserMappingPage from '../component/edit-user-mapping-page';
import {getUserMapping, storeUserMapping} from '../../shared/service/user-mapping-api-service';
import {UserMapping} from '../../shared/model/user-mapping';

jest.mock('../../shared/service/user-mapping-api-service')
const mockedGetUserMapping = mocked(getUserMapping)
const mockedStoreUserMapping = mocked(storeUserMapping)

jest.mock('../../shared/error/error.helper')
const mockedErrorHelper = mocked(getErrorCode)

configure({adapter: new Adapter()});

describe('EditUserMappingPage', () => {
  let component: ReactWrapper;

  it('should render a LoadingIndicator when loads', async () => {

    await act(async () => {
      component = mount(<EditUserMappingPage/>);
    })

    expect(component.find(LoadingIndicator).exists()).toBe(true);
  });

  describe('after a success getAccountMapping rest api call with an existing openQualityCheckerAccountName', () => {
    const mockUserMapping: UserMapping = {
      openQualityCheckerUserToken: 'test'
    }

    beforeEach(async () => {
      mockedGetUserMapping.mockResolvedValue(mockUserMapping);

      await act(async () => {
        component = mount(<EditUserMappingPage/>);
      })

      await act(async () => {
        await component;
        await new Promise(resolve => setImmediate(resolve));

        component.update();
      });
    })

    it('should render an input tag', async () => {
      expect(component.find('input[data-testid="openQualityCheckerUserToken"]').exists()).toBe(true);
    });

    it('the rendered inputs value should match the mocked value', async () => {
      expect(component.find('input[data-testid="openQualityCheckerUserToken"]').props().value).toBe(mockUserMapping.openQualityCheckerUserToken);
    });

    it('should render a PrimaryButton', () => {
      expect(component.find(PrimaryButton).exists()).toBe(true);
    });
  });

  describe('after a success getUserMapping rest api call where openQualityCheckerUserToken is null', () => {
    const mockUserMapping: UserMapping = {
      openQualityCheckerUserToken: 'test'
    }

    beforeEach(async () => {
      mockedGetUserMapping.mockResolvedValue(mockUserMapping);

      await act(async () => {
        component = mount(<EditUserMappingPage/>);
      })

      await act(async () => {
        await new Promise(resolve => setImmediate(resolve));

        component.update();
      });
    });

    it('should set the input value to empty string', async () => {
      expect(component.find('input[data-testid="openQualityCheckerUserToken"]').props().value).toBe(mockUserMapping.openQualityCheckerUserToken);
    });

    describe('after fill the input and click on save button', () => {
      let primaryButton: ReactWrapper<any>;
      let axiosResponse: Promise<void>
      let expectedMessage: string;

      it('with valid input, should render a SectionMessageBox component with the expected success message', async () => {
        primaryButton = component.find(PrimaryButton);
        axiosResponse = Promise.resolve();
        expectedMessage = "OpenQualityChecker user token has been saved successfully";

        mockedStoreUserMapping.mockResolvedValue(axiosResponse);

        await act(async () => {
          primaryButton.simulate('submit');

          await new Promise(resolve => setImmediate(resolve));

          component.update();
        });

        expect(component.find(SectionMessageBox).props().body).toBe(expectedMessage);
      });

      it('with invalid input, should render a SectionMessageBox component with the expected error message', async () => {
        primaryButton = component.find(PrimaryButton);
        axiosResponse = Promise.resolve();
        expectedMessage = ErrorCodes.UNKNOWN_ERROR;

        mockedStoreUserMapping.mockRejectedValue(axiosResponse);
        mockedErrorHelper.mockReturnValue("UNKNOWN_ERROR");

        await act(async () => {
          primaryButton.simulate('submit');

          await new Promise(resolve => setImmediate(resolve));

          component.update();
        });

        expect(component.find(SectionMessageBox).props().body).toBe(expectedMessage);
      });
    });
  });
});
