/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

// Create the queryTextarea editor
window.onload = function() {
  window.editor = CodeMirror.fromTextArea(document.getElementById('queryTextarea'), {
    mode: 'text/x-mysql',
    indentWithTabs: true,
    smartIndent: true,
    lineNumbers: true,
    matchBrackets : true,
    autofocus: true,
    extraKeys: {
      "Ctrl-Space": "autocomplete",
      "Ctrl-Enter": angular.element($("#queryTextarea")).scope().search
    } 
  });
  
  
  window.explanResult = CodeMirror.fromTextArea(document.getElementById('explanResult'), {
    mode: 'application/json',
    indentWithTabs: true,
    smartIndent: true,
    lineNumbers: true,
    matchBrackets : true
  });
};
