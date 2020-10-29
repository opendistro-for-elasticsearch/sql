/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

import React from "react";
import _ from "lodash";
import { EuiButtonGroup } from "@elastic/eui";
// @ts-ignore
import { htmlIdGenerator } from "@elastic/eui/lib/services";

interface SwitchProps {
    onChange: (id: string, value?: any) => void;
    language: string;
}

interface SwitchState {
    // language: string
}

const toggleButtons = [
    {
        id: 'SQL',
        label: 'SQL',
    },
    {
        id: 'PPL',
        label: 'PPL',
    },
];

class Switch extends React.Component<SwitchProps, SwitchState> {
    constructor(props: SwitchProps) {
        super(props);
        this.state = {
            language: 'SQL'
        };
    }

    render() {

        return (
            <EuiButtonGroup className="query-language-switch"
                legend="query-language-swtich"
                options={toggleButtons}
                onChange={(id) => this.props.onChange(id)}
                idSelected={this.props.language}
                buttonSize="m"
            />
        )
    }
}

export default Switch;