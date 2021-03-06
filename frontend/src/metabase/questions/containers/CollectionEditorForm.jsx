import React, { Component } from "react";

import Button from "metabase/components/Button";
import ColorPicker from "metabase/components/ColorPicker";
import FormField from "metabase/components/FormField";
import Input from "metabase/components/Input";
import Modal from "metabase/components/Modal";

import { reduxForm } from "redux-form";

import { normal, getRandomColor } from "metabase/lib/colors";

const formConfig = {
    form: 'collection',
    fields: ['id', 'name', 'description', 'color'],
    validate: (values) => {
        const errors = {};
        if (!values.name) {
            errors.name = "Name is required";
        } else if (values.name.length > 100) {
            errors.name = "Name must be 100 characters or less";
        }
        if (!values.color) {
            errors.color = "Color is required";
        }
        return errors;
    },
    initialValues: {
        name: "",
        description: "",
        // pick a random color to start so everything isn't blue all the time
        color: getRandomColor(normal)
    }
}

export const getFormTitle = ({ id, name }) =>
    id.value ? name.value : "New collection"

export const getActionText = ({ id }) =>
    id.value ? "Update": "Create"


export const CollectionEditorFormActions = ({ handleSubmit, invalid, onClose, fields}) =>
    <div>
        <Button className="mr1" onClick={onClose}>
            Cancel
        </Button>
        <Button primary disabled={invalid} onClick={handleSubmit}>
            { getActionText(fields) }
        </Button>
    </div>

export class CollectionEditorForm extends Component {
    props: {
        fields: Object,
        onClose: Function,
        invalid: Boolean,
        handleSubmit: Function,
    }

    render() {
        const { fields, onClose } = this.props;
        return (
            <Modal
                inline
                form
                title={getFormTitle(fields)}
                footer={<CollectionEditorFormActions {...this.props} />}
                onClose={onClose}
            >
                <div className="NewForm ml-auto mr-auto mt4 pt2" style={{ width: 540 }}>
                    <FormField
                        displayName="Name"
                        {...fields.name}
                    >
                        <Input
                            readOnly={fields.description.value == "Audit default"}
                            className="Form-input full"
                            placeholder="My new fantastic collection"
                            autoFocus
                            {...fields.name}
                        />
                    </FormField>
                    <FormField
                        displayName="Description"
                        {...fields.description}
                    >
                        <textarea
                            readOnly={fields.description.value == "Audit default"}
                            className="Form-input full"
                            placeholder="It's optional but oh, so helpful"
                            {...fields.description}
                        />
                    </FormField>
                    <FormField
                        displayName="Color"
                        {...fields.color}
                    >
                        <ColorPicker {...fields.color} />
                    </FormField>
                </div>
            </Modal>
        )
    }
}

export default reduxForm(formConfig)(CollectionEditorForm)
