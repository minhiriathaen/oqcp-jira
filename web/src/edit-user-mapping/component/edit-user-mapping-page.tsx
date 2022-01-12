import React, { useEffect, useState } from 'react';
import Form, { Field, FormFooter } from '@atlaskit/form';
import Textfield from '@atlaskit/textfield';
import ErrorCodes from '../../shared/error/error-message';
import SectionMessageBox, { SectionMessageData } from '../../shared/component/section-message-box';
import Wrapper from '../../shared/component/wrapper';
import { UserMapping } from '../../shared/model/user-mapping';
import LoadingIndicator from '../../shared/component/loading-indicator';
import {
  FieldChildrenArguments,
  FormChildrenArguments,
} from '../../shared/type/atlaskit/atlaskit.form.types';
import { getUserMapping, storeUserMapping } from '../../shared/service/user-mapping-api-service';
import PrimaryButton from '../../shared/component/primary-button';
import { getErrorCode } from '../../shared/error/error.helper';

const defaultUserMapping: UserMapping = {
  openQualityCheckerUserToken: '',
};

function EditUserMappingPage(): JSX.Element {
  const [userMapping, setUserMapping] = useState<UserMapping>();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [sectionMessageData, setSectionMessageData] = useState<SectionMessageData | null>();

  async function load() {
    try {
      const value: UserMapping = await getUserMapping();

      if (value.openQualityCheckerUserToken) {
        setUserMapping(value);
      } else {
        setUserMapping(defaultUserMapping);
      }

      setLoading(false);

      setShowForm(true);
    } catch (error) {
      const errorCode = getErrorCode(error, 'CONNECTION_ERROR');

      if (
        errorCode === ErrorCodes.CONNECTION_ERROR ||
        errorCode === ErrorCodes.ACCOUNT_MAPPING_NOT_FOUND
      ) {
        setShowForm(false);
      }

      setLoading(false);

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

  async function save(formState: UserMapping) {
    setSaving(true);
    setSectionMessageData(null);

    try {
      await storeUserMapping(formState);

      setSaving(false);

      setUserMapping(formState);

      setSectionMessageData({
        title: 'Success',
        appereance: 'confirmation',
        body: 'OpenQualityChecker user token has been saved successfully',
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
            {({ formProps }: FormChildrenArguments<UserMapping>) => (
              <form {...formProps}>
                <Field
                  label="Please add your OpenQualityChecker user token"
                  isRequired
                  name="openQualityCheckerUserToken"
                  defaultValue={userMapping?.openQualityCheckerUserToken}
                >
                  {({ fieldProps }: FieldChildrenArguments<string>) => (
                    <>
                      <Textfield testId="openQualityCheckerUserToken" {...fieldProps} />
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

export default EditUserMappingPage;
