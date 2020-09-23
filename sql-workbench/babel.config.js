/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

// babelrc doesn't respect NODE_PATH anymore but using require does.
// Alternative to install them locally in node_modules
module.exports = {
  presets: [require("@babel/preset-env"), require("@babel/preset-react"), require("@babel/preset-typescript")],
  plugins: [require("@babel/plugin-proposal-class-properties"), require("@babel/plugin-proposal-object-rest-spread"), ["@babel/transform-runtime"]]
};
