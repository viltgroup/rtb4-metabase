/* eslint-disable react/display-name */

import React, { Component } from "react";

import Tutorial, { qs, qsWithContent } from "./Tutorial.jsx";

import RetinaImage from "react-retina-image";

const RTB_DASHBOARD_STEPS = [
{
    getPortalTarget: () => qs(".Dashboard"),
    getModal: (props) =>
        <div className="text-centered">
            <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/segments-list.png" width={186} />
            <h3>Welcome to the Dashboard page!</h3>
            <p>Every event has a default dashboard with some relevant reports.</p>
            <a className="Button Button--primary" onClick={props.onNext}>Tell me more</a>
        </div>
},
{
    getPortalTarget: () => qs(".DashboardGrid"),
    getModalTarget: () => qs(".DashboardGrid"),
    getModal: (props) =>
        <div className="text-centered">
            <h3>For now you have three types of reports.</h3>
            <p>You cannot edit any of this reports but feel free to create new reports from the ones we provide!</p>
            <a className="Button Button--primary" onClick={props.onNext}>Next</a>
        </div>
},
{
    getPortalTarget: () => qs(".DashCard"),
    getModalTarget: () => qs(".DashCard"),
    getModal: (props) =>
        <div className="text-centered">
            <h3>Here you have every event with its atributes.</h3>
            <p>The attributes will vary according to the event.</p>
            <a className="Button Button--primary" onClick={props.onNext}>Next</a>
        </div>
},
{
    getPortalTarget: () => qs(".LegendItem"),
    getModalTarget: () => qs(".LegendItem"),
    getModal: (props) =>
        <div className="text-centered">
            <RetinaImage className="mb2" forceOriginalDimensions={true} src="app/assets/img/illustration_ask_question.png"/>
            <h3>You can click in the report name to enter in the query builder menu.</h3>
            <p>There you can apply many filters and create new reports.</p>
            <a className="Button Button--primary" onClick={props.onNext}>Next</a>
        </div>
},
{
    getPortalTarget: () => qs(".flex-row"),
    getModalTarget: () => qs(".flex-row"),
    getModal: (props) =>
        <div className="text-centered">
            <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/qb_tutorial/funnel.png" width={186} />
            <h3>You can also apply some filters over the dashboard.</h3>
            <p>Each filter will be applied over the question in this dashboard</p>
            <a className="Button Button--primary" onClick={props.onNext}>Finish</a>
        </div>
}
]

export default class RTBDashboardTutorial extends Component {
render() {
    return <Tutorial steps={RTB_DASHBOARD_STEPS} {...this.props} />;
}
}
