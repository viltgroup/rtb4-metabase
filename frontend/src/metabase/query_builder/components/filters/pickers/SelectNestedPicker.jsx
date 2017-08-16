/* @flow */

import React, { Component } from "react";
import PropTypes from "prop-types";

import Icon from "metabase/components/Icon.jsx";

import { capitalize } from "metabase/lib/formatting";

import cx from "classnames";

type SelectNestedOption = {
    name: string,
    key: string
};

type Props = {
    options: Array<SelectNestedOption>,
    values: Array<string>,
    onValuesChange: (values: any[]) => void,
    placeholder?: string,
    multi?: bool
}

type State = {
    searchText: string,
    searchRegex: ?RegExp,
    filterKey: string,
    splitOpts: Array<Array<string>>,
    expandOpts: Array<SelectNestedOption>,
}

export default class SelectNestedPicker extends Component {
    state: State;
    props: Props;

    constructor(props: Props) {
        super(props);

        let acc = [];
        for (const option of props.options) {
            acc.push(option.name.split(' > '));
        }

        this.state = {
            searchText: "",
            searchRegex: null,
            filterKey: "",
            splitOpts: acc,
            expandOpts: this.expandOptions(this.props.options) 
        };
    }

    static propTypes = {
        options: PropTypes.object.isRequired,
        values: PropTypes.array.isRequired,
        onValuesChange: PropTypes.func.isRequired,
        placeholder: PropTypes.string,
        multi: PropTypes.bool
    };

    existsOption(options : Array<SelectNestedOption>, key : string, name : string) {
        let found = false, j;
        for (j = 0; j < options.length && !found; j++) {
            if (options[j].key == key && options[j].name == name) {
                found = true;
            }
        }
        return found;
    }

    expandOptions(options : Array<SelectNestedOption>) {
        let newOpts = [], i;
        for (const option of options) {
            let splitted = option.name.split(' > ');
            let k = "";
            for(i = 0; i < splitted.length; i++) {
                if (i !== 0)
                    k += ' > ';
                k += splitted[i];
                if (!this.existsOption(newOpts, k, k))
                    newOpts.push({"key": k, "name": splitted[i]});
            }
        }
        return newOpts;
    }

    setKey(key: string) {
         this.setState({ filterKey: key });
    }

    noChild(key: string) {
        let count = 0;
        for (const opt of this.props.options) {
            if (opt.key === key) {
                count++;
            }
        }
        return (count == 1) 
    }

    selectValue(key: string, selected: bool) {
        let values;
        if (this.props.multi) {
            values = this.props.values.slice().filter(v => v != null);
        } else {
            values = []
        }
        if (selected) {
            values.push(key);
        } else {
            values = values.filter(v => v !== key);
        }
        //if is child, keeps the key 
        if(!this.noChild(key))
            this.setKey(key);

        this.props.onValuesChange(values);
    }

    nameForOption(option: SelectNestedOption) {
        if (option.name === "") {
            return "Empty";
        } else if (typeof option.name === "string") {
            return option.name;
        } else {
            return capitalize(String(option.name));
        }
    }

    getValuesByKey(key : string) {
        let vals = [];
        //no key, get first level - array index 0
        if (key === "") {
            for (const path of this.state.splitOpts) {
                if (!vals.includes(path[0]))
                    vals.push(path[0]);
            }
        } else {
            // for every path, try to find one that matches the key  
            for (const path of this.state.splitOpts) {
                let keySplitted = key.split(' > '), i, flag = true;
                for (i = 0; i < keySplitted.length && flag; i++) {
                    if (path[i] !== keySplitted[i]) 
                        flag = false;
                }
                // path not exists and not last index and option not added already
                if (flag && i !== path.length && !vals.includes(path[i]))
                    vals.push(path[i]);
            }
        } 

        let res = [];
        for (const op of this.state.expandOpts) {
            if (vals.includes(op.name)){
                (!this.existsOption(res, op.key, op.name)) ? (res.push(op)) : null;
            }
        }
        return res;
    } 


    render() {
        let { values, placeholder } = this.props;

        let currentValues = this.getValuesByKey(this.state.filterKey);

        return (
            <div>
                { this.state.filterKey === "" ? null : 
                    <div className="FilterPopover-header text-grey-3 p1 mt1 flex align-center">
                        <a className="cursor-pointer flex align-center" onClick={() => this.setKey("")}>
                            <Icon name="chevronleft" size={18}/>
                            <h3 className="inline-block">{this.state.filterKey}</h3>
                        </a>
                    </div>
                }
                <div className="flex-wrap px1 pt1" style={{maxHeight: '400px', overflowY: 'scroll'}}>
                    { placeholder ?
                      <h5>{placeholder}</h5>
                      : null }
                           {   
                               currentValues.map((option: SelectNestedOption) =>
                                <div style={{ padding: "0.15em" }}>
                                    <button
                                        style={{ height: "30px" }}
                                        className={cx("full rounded bordered border-purple text-centered text-bold", {
                                                "text-purple bg-white": values[0] !== option.key,
                                                "text-white bg-purple-light": values[0] === option.key
                                            })}
                                        onClick={() => this.selectValue(option.key, true)}
                                    >
                                        {this.nameForOption(option)}
                                    </button>
                                </div>
                            )}                     
                </div>
            </div>
        );
    }
}
