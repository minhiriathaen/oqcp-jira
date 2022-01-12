import {configure, mount, ReactWrapper} from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import EditAccountMappingPage from '../component/edit-account-mapping-page';
import LoadingIndicator from '../../shared/component/loading-indicator';
import {mocked} from 'ts-jest/utils'
import {
  getAccountMapping,
  storeAccountMapping
} from '../../shared/service/account-mapping-api-service'
import {AccountMapping} from '../../shared/model/account-mapping';
import {act} from 'react-dom/test-utils';
import PrimaryButton from '../../shared/component/primary-button';
import {getErrorCode} from '../../shared/error/error.helper';
import SectionMessageBox from '../../shared/component/section-message-box';
import ErrorCodes from '../../shared/error/error-message';

jest.mock('../../shared/service/account-mapping-api-service')
const mockedGetAccountMapping = mocked(getAccountMapping)
const mockedStoreAccountMapping = mocked(storeAccountMapping)

jest.mock('../../shared/error/error.helper')
const mockedErrorHelper = mocked(getErrorCode)

configure({adapter: new Adapter()});

describe('EditAccountMappingPage', () => {
  let component: ReactWrapper;

  it('should render a LoadingIndicator when loads', async () => {
    await act(async () => {
      component = mount(<EditAccountMappingPage/>);
    })

    expect(component.find(LoadingIndicator).exists()).toBe(true);
  });

  describe('after a success getAccountMapping rest api call with an existing openQualityCheckerAccountName', () => {
    const mockAccountMapping: AccountMapping = {
      openQualityCheckerAccountName: 'test'
    }

    beforeEach(async () => {
      mockedGetAccountMapping.mockResolvedValue(mockAccountMapping);

      await act(async () => {
        component = mount(<EditAccountMappingPage/>);
      })

      await act(async () => {
        await new Promise(resolve => setImmediate(resolve));

        component.update();
      });
    })

    it('should render an input tag', async () => {
      expect(component.find('input[data-testid="openQualityCheckerAccountName"]').exists()).toBe(true);
    });

    it('the rendered inputs value should match the mocked value', async () => {
      expect(component.find('input[data-testid="openQualityCheckerAccountName"]').props().value).toBe(mockAccountMapping.openQualityCheckerAccountName);
    });

    it('should render a PrimaryButton', () => {
      expect(component.find(PrimaryButton).exists()).toBe(true);
    });
  });

  describe('after a success getAccountMapping rest api call where openQualityCheckerAccountName is null', () => {
    const mockAccountMapping: AccountMapping = {
      openQualityCheckerAccountName: ''
    }

    beforeEach(async () => {
      mockedGetAccountMapping.mockResolvedValue(mockAccountMapping);

      await act(async () => {
        component = mount(<EditAccountMappingPage/>);
      })

      await act(async () => {
        await new Promise(resolve => setImmediate(resolve));

        component.update();
      });
    });

    it('should set the input value to empty string', async () => {
      expect(component.find('input[data-testid="openQualityCheckerAccountName"]').props().value).toBe(mockAccountMapping.openQualityCheckerAccountName);
    });

    describe('after fill the input and click on save button', () => {
      let primaryButton: ReactWrapper<any>;
      let axiosResponse: Promise<void>
      let expectedMessage: string;

      it('with valid input, should render a SectionMessageBox component with the expected success message', async () => {
        primaryButton = component.find(PrimaryButton);
        axiosResponse = Promise.resolve();
        expectedMessage = "OpenQualityChecker account name has been saved successfully";

        mockedStoreAccountMapping.mockResolvedValue(axiosResponse);

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

        mockedStoreAccountMapping.mockRejectedValue(axiosResponse);
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

  describe('after a failed getAccountMapping rest api call throws CONNECTION_ERROR', () => {
    beforeEach(async () => {
      mockedGetAccountMapping.mockRejectedValueOnce(new Error())
      mockedErrorHelper.mockReturnValue("CONNECTION_ERROR");

      await act(async () => {
        component = mount(<EditAccountMappingPage/>);
      })

      await act(async () => {
        await new Promise(resolve => setImmediate(resolve));

        component.update();
      });
    });

    it('should the SectionMessageBox component body prop match the expectet error message', async () => {
      const expectedMessage = "We are unable to connect to the server at this time";

      expect(component.find(SectionMessageBox).props().body).toBe(expectedMessage);
    });

    it('should render a SectionMessageBox component', async () => {
      expect(component.find(SectionMessageBox).exists()).toBe(true);
    });
  });
});
