/* @flow */

import React, { Component } from "react";
import PropTypes from "prop-types";

import CheckboxTree from 'react-checkbox-tree';
import 'react-checkbox-tree/lib/react-checkbox-tree.css';


type SelectOption = {
    name: string,
    key: string
};

type State = {
    nodes: Array<>,
    checked: Array<string>,
    expanded: Array<string>,
}

type Props = {
    options: Array<SelectOption>,
    values: Array<string>,
    onValuesChange: (values: any[]) => void
}


export default class FilterMultiHierarchy extends Component {
    state: State;
    props: Props;

    constructor(props: Props) {
        super(props);
        this.state = {
            nodes: this.buildNodes(this.props.options),
            checked: (this.props.values[0] !== undefined) ? this.props.values : [],
            expanded: [],
        }
        this.onCheck = this.onCheck.bind(this);
    }

    static propTypes = {
        options: PropTypes.object.isRequired,
        values: PropTypes.array.isRequired,
        onValuesChange: PropTypes.func.isRequired
    };

    buildNodes(options) {
        let nodes = [];
        for (const option of this.getSplittedValues(options)) {
            let inserted = false;
            const splittedPath = option.split(' > ');
            for (let i = 0; i < nodes.length && !inserted; i++) {
                inserted = this.findInLevel(splittedPath, nodes[i], 0);
            }
            if (!inserted) {
                nodes.push(this.addNewPath(splittedPath, 0));
            }
        }
        return nodes;
    }

    getSplittedValues(options) {
        let res = [];
        for (let i = 0; i < options.length; ++i) {
            let splitted = options[i].key.split(', ');
            for (let j = 0; j < splitted.length; ++j) {
                res.push(splitted[j]);
            }
        }
        return res;
    }


    findInLevel(splittedPath, root, pos) {
        let inserted = false;
        let value = splittedPath.slice(0, pos + 1).join(' > ');

        if (root.value === value) {
            while (pos < splittedPath.length && !inserted) {
                if (root.label === splittedPath[pos]) {
                    if (root.children != null) {
                        root.children.push(this.addNewPath(splittedPath, ++pos));
                    } else {
                        root.children = [this.addNewPath(splittedPath, ++pos)];
                    }
                    inserted = true;
                } else {
                    root = root.children;
                    ++pos;
                }
            }
        }
        return inserted;
    }


    addNewPath(splittedPath, pos) {
        let node, children = [];

        for (let i = splittedPath.length - 1; i >= pos; --i) {
            let value = splittedPath.slice(0, i + 1).join(' > ');  //can be optimized   
            node = {
                value: value,
                label: splittedPath[i],
                children: (children.length === 0) ? children : [children],
            };
            children = node;
        }
        return children;
    }


    onCheck(checked) {
        this.setState({ checked });
        this.props.onValuesChange(checked);
    }

    render() {
        const nodes = this.state.nodes;

        return (
            <div className="pt1" style={{ maxHeight: '400px', overflowY: 'scroll' }}>
                <CheckboxTree
                    nodes={nodes}
                    checked={this.state.checked}
                    expanded={this.state.expanded}
                    onCheck={this.onCheck}
                    onExpand={expanded => this.setState({ expanded })}
                />
            </div>
        );
    }
}
