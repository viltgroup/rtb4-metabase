import React, { Component } from "react";
import PropTypes from "prop-types";

import Icon from "metabase/components/Icon.jsx";
import Modal from "metabase/components/Modal.jsx";

import Prism from 'prismjs';

export default class XmlLink extends Component {

    constructor(props, context) {
        super(props, context);

        this.state = {
            isOpen: false,
        };
    }

    static propTypes = {
        data: PropTypes.string.isRequired,
    };

    render() {
        let { data } = this.props;
        let onClick = (e) => this.setState({ isOpen: true }, e.stopPropagation());

        // Returns a highlighted HTML string
        var xml = Prism.highlight(data, Prism.languages.markup);
        return (
            <div>
                <Icon name="info" size={14} onClick={onClick}>
                    xml
                </Icon>
                <Modal medium backdropClassName="Modal-backdrop--dark" isOpen={this.state.isOpen} onClose={() => this.setState({ isOpen: false })}>
                    <div className="p4">
                        <div className="mb3 flex flex-row flex-full align-center justify-between">
                            <h2>XML Details</h2>
                            <span className="cursor-pointer" onClick={() => this.setState({ isOpen: false })}><Icon name="close" size={16} /></span>
                        </div>

                        <pre>
                            <code className="language-markup">
                                <div dangerouslySetInnerHTML={{__html: xml}}/>
                            </code>
                        </pre>

                        <div className="text-centered">
                            <a className="Button Button--primary" onClick={() => { this.setState({ isOpen: false }); }}>
                                Close
                            </a>
                        </div>
                    </div>
                </Modal>
            </div>
        );
    }
}
