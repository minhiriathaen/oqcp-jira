import React, { useEffect, useState } from 'react';
import Form, { Field, FormFooter } from '@atlaskit/form';
import Textfield from '@atlaskit/textfield';
import {
  getAccountMapping,
  storeAccountMapping,
} from '../../shared/service/account-mapping-api-service';
import { AccountMapping } from '../../shared/model/account-mapping';
import ErrorCodes from '../../shared/error/error-message';
import Wrapper from '../../shared/component/wrapper';
import SectionMessageBox, { SectionMessageData } from '../../shared/component/section-message-box';
import {
  FieldChildrenArguments,
  FormChildrenArguments,
} from '../../shared/type/atlaskit/atlaskit.form.types';
import LoadingIndicator from '../../shared/component/loading-indicator';
import PrimaryButton from '../../shared/component/primary-button';
import { getErrorCode } from '../../shared/error/error.helper';

const defaultAccountMapping: AccountMapping = {
  openQualityCheckerAccountName: '',
};

function EditAccountMappingPage(): JSX.Element {
  const [accountMapping, setAccountMapping] = useState<AccountMapping>();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [sectionMessageData, setSectionMessageData] = useState<SectionMessageData | null>();

  async function load() {
    try {
      const value: AccountMapping = await getAccountMapping();

      if (value.openQualityCheckerAccountName) {
        setAccountMapping(value);
      } else {
        setAccountMapping(defaultAccountMapping);
      }

      setLoading(false);

      setShowForm(true);
    } catch (error) {
      const errorCode = getErrorCode(error, 'CONNECTION_ERROR');

      if (errorCode === ErrorCodes.CONNECTION_ERROR) {
        setShowForm(false);
      }

      setLoading(false);

      setSectionMessageData({
        title: 'Warning',
        appereance: 'warning',
        body: 'We are unable to connect to the server at this time',
      });
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function save(formState: AccountMapping) {
    setSaving(true);
    setSectionMessageData(null);

    try {
      await storeAccountMapping(formState);

      setSaving(false);

      setAccountMapping(formState);

      setSectionMessageData({
        title: 'Success',
        appereance: 'confirmation',
        body: 'OpenQualityChecker account name has been saved successfully',
      });
    } catch (error) {
      setSaving(false);

      setSectionMessageData({
        title: 'An error occurred while saving the changes',
        appereance: 'error',
        body: ErrorCodes[getErrorCode(error, 'UNKNOWN_ERROR')],
      });
    }
  }

  if (loading) {
    return <LoadingIndicator />;
  }
  return (
    <Wrapper paddingTop="48px" alignItems="center">
      <Wrapper width="450px">
        {showForm && (
          <Form onSubmit={save}>
            {({ formProps }: FormChildrenArguments<AccountMapping>) => (
              <form {...formProps}>
                <Field
                  label="Please add your OpenQualityChecker account name"
                  isRequired
                  name="openQualityCheckerAccountName"
                  defaultValue={accountMapping?.openQualityCheckerAccountName}
                >
                  {({ fieldProps }: FieldChildrenArguments<string>) => (
                    <>
                      <Textfield testId="openQualityCheckerAccountName" {...fieldProps} />
                    </>
                  )}
                </Field>
                <FormFooter>
                  <PrimaryButton type="submit" isDisabled={saving} showSpinner={saving}>
                    Save
                  </PrimaryButton>
                </FormFooter>
              </form>
            )}
          </Form>
        )}

        {sectionMessageData && <SectionMessageBox {...sectionMessageData} />}
      </Wrapper>
    </Wrapper>
  );
}

export default EditAccountMappingPage;
