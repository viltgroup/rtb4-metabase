/* eslint-disable react/display-name */

import React, { Component } from "react";

import Tutorial, { qs, qsWithContent } from "./Tutorial.jsx";

import RetinaImage from "react-retina-image";

const RTB_COLLECTIONS_STEPS = [
{
    getModal: (props) =>
        <div className="text-centered">
            <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/simple_embed.png" width={186} />
            <h3>Welcome to the Questions page!</h3>
            <p>A question is a report created from your datasource. Here is the list of all your questions.</p>
            <a className="Button Button--primary" onClick={props.onNext}>Tell me more</a>
        </div>
},
{
    getPortalTarget: () => qs(".Grid--gutters"),
    getModalTarget: () => qs(".Grid--gutters"),
    getModal: (props) =>
        <div className="text-centered">
            <h3>Your question can be placed into collections.</h3>
            <p>Each collection has a set of permissions that have an affect on the questions in it.</p>
            <a className="Button Button--primary" onClick={props.onNext}>Next</a>
        </div>
},
{
    getPortalTarget: () => qsWithContent(".Grid-cell", "RTB default"),
    getModalTarget: () => qsWithContent(".Grid-cell", "RTB default"),
    getModal: (props) =>
        <div className="text-centered">
            <h3>We provide you an default collection - RTB Default.</h3>
            <p>You cannot edit any report or info associated with this collection. But, as an admin, feel free to create the collections you pretend.</p>
            <a className="Button Button--primary" onClick={props.onNext}>Next</a>
        </div>
},
/*{
    getPortalTarget: () => qs("ul[class^=List]"),
    getModalTarget: () => qs("ul[class^=List]"),
    getModal: (props) =>
        <div className="text-centered">
            <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/welcome-modal-2.png" width={186}/>
            <h3>This is the list of the remaining list of questions.</h3>
            <p>The questions with are not associated with any collection are diplayed here.</p>
            <a className="Button Button--primary" onClick={props.onNext}>Next</a>
        </div>
},*/
{
    getPortalTarget: () => qs(".Icon-viewArchive"),
    getModalTarget: () => qs(".Icon-viewArchive"),
    getModal: (props) =>
        <div className="text-centered">
            <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/pulse_no_results.png" />
            <p>Here you can see your archived questions and collections.</p>
            <a className="Button Button--primary" onClick={props.onClose}>Finish</a>
        </div>
}
]

export default class RTBCollectionsTutorial extends Component {
    render() {
        return <Tutorial steps={RTB_COLLECTIONS_STEPS} {...this.props} />;
    }
}
