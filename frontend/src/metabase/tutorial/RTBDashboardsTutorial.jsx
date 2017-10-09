/* eslint-disable react/display-name */

import React, { Component } from "react";
    
import Tutorial, { qs, qsWithContent } from "./Tutorial.jsx";

import RetinaImage from "react-retina-image";

const RTB_DASHBOARDS_STEPS = [
    {
        getPortalTarget: () => qs(".Grid-Cell"),
        getModal: (props) =>
            <div className="text-centered">
                <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/segments-list.png" width={186} />
                <h3>Welcome to the Dashboards page!</h3>
                <p>A Dashboard lets you join multiples reports in one place. Here is a list of all your dashboards.</p>
                <a className="Button Button--primary" onClick={props.onNext}>Tell me more</a>
            </div>
    },
    {
        getPortalTarget: () => qs(".NavItem > img"),
        getModalTarget: () => qs(".NavItem > img"),
        getModal: (props) =>
            <div className="text-centered">
                <h3>In this page you can find all the recent activity happened in the platform.</h3>
                <p>Information like dashboard and report creation are tracked.</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qsWithContent(".NavItem", "Dashboards"),
        getModalTarget: () => qsWithContent(".NavItem", "Dashboards"),
        getModal: (props) =>
            <div className="text-centered">
                <h3>This is your principal page where you can find all your dashboards.</h3>
                <p>This page contains the default and the custom dashboards</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qsWithContent(".NavItem", "Questions"),
        getModalTarget: () => qsWithContent(".NavItem", "Questions"),
        getModal: (props) =>
            <div className="text-centered">
                <h3>Here you can find all your question/reports and the collections them can be associated with.</h3>
                <p>There is an default collection - Audit default</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qsWithContent(".NavItem", "Share"),
        getModalTarget: () => qsWithContent(".NavItem", "Share"),
        getModal: (props) =>
            <div className="text-centered">
                <h3>You can share the any report you have.</h3>
                <p>Send your data via email or Slack</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qs(".Icon-add"),
        getModalTarget: () => qs(".Icon-add"),
        getModal: (props) =>
            <div className="text-centered">
                <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/qb_tutorial/table.png" width={157} />
                <h3>Here you can create a new dashboards and can place any of your custom reports.</h3>
                <p>You cannot change the default dashboards but you can create as many dashboards as you like!</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qs(".Icon-viewArchive"),
        getModalTarget: () => qs(".Icon-viewArchive"),
        getModal: (props) =>
            <div className="text-centered">
                <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/pulse_no_results.png" width={157} />
                <h3>Whenever you archive an dashboard you can look for it here and bring it back to this page.</h3>
                <p>An archived dashboard has always the questions which are associated to it!</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qs("div[class*=_166lg"),
        getModalTarget: () => qs("div[class*=_166lg"),
        getModal: (props) =>
            <div className="text-centered">
                <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/qb_tutorial/funnel.png" width={120} />
                <h3>Since there are quite a few default dashboards you can filter them by their name in this search bar.</h3>
                <p>You cannot change the default dashboards but you can create as many dashboards as you like!</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qs(".Grid-cell > a"),
        getModalTarget: () => qs(".Grid-cell > a"),
        getModal: (props) =>
            <div className="text-centered">
                <RetinaImage className="mb2" forceOriginalDimensions={false} src="app/assets/img/illustration_dashboard.png" width={157} />
                <h3>Each one of this boxes represent a dashboard where are your organized data.</h3>
                <p>For every event there is a dashboard with some reports.</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qs(".text-ellipsis"),
        getModalTarget: () => qs(".text-ellipsis"),
        getModal: (props) =>
            <div className="text-centered">
                <h3>The default dashboards names start with the [AUDIT] prefix and you cannot change them.</h3>
                <p>The name of the dashboard has the information about the event name with its hierarchical position.</p>
                <a className="Button Button--primary" onClick={props.onNext}>Next</a>
            </div>
    },
    {
        getPortalTarget: () => qs(".Icon-staroutline"),
        getModalTarget: () => qs(".Icon-staroutline"),
        getModal: (props) =>
            <div className="text-centered">
                <h3>You can add a dashboard to your favorites by starring it.</h3>
                <p>Only you can see your favorite dashboards.</p>
                <a className="Button Button--primary" onClick={props.onClose}>Finish</a>
            </div>
    }
]

export default class RTBDashboardsTutorial extends Component {
    render() {
        return <Tutorial steps={RTB_DASHBOARDS_STEPS} {...this.props} />;
    }
}
