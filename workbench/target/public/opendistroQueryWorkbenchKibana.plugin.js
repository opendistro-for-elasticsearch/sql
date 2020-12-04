/******/ (function(modules) { // webpackBootstrap
/******/ 	// install a JSONP callback for chunk loading
/******/ 	function webpackJsonpCallback(data) {
/******/ 		var chunkIds = data[0];
/******/ 		var moreModules = data[1];
/******/
/******/
/******/ 		// add "moreModules" to the modules object,
/******/ 		// then flag all "chunkIds" as loaded and fire callback
/******/ 		var moduleId, chunkId, i = 0, resolves = [];
/******/ 		for(;i < chunkIds.length; i++) {
/******/ 			chunkId = chunkIds[i];
/******/ 			if(Object.prototype.hasOwnProperty.call(installedChunks, chunkId) && installedChunks[chunkId]) {
/******/ 				resolves.push(installedChunks[chunkId][0]);
/******/ 			}
/******/ 			installedChunks[chunkId] = 0;
/******/ 		}
/******/ 		for(moduleId in moreModules) {
/******/ 			if(Object.prototype.hasOwnProperty.call(moreModules, moduleId)) {
/******/ 				modules[moduleId] = moreModules[moduleId];
/******/ 			}
/******/ 		}
/******/ 		if(parentJsonpFunction) parentJsonpFunction(data);
/******/
/******/ 		while(resolves.length) {
/******/ 			resolves.shift()();
/******/ 		}
/******/
/******/ 	};
/******/
/******/
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// object to store loaded and loading chunks
/******/ 	// undefined = chunk not loaded, null = chunk preloaded/prefetched
/******/ 	// Promise = chunk loading, 0 = chunk loaded
/******/ 	var installedChunks = {
/******/ 		"opendistroQueryWorkbenchKibana": 0
/******/ 	};
/******/
/******/
/******/
/******/ 	// script path function
/******/ 	function jsonpScriptSrc(chunkId) {
/******/ 		return __webpack_require__.p + "opendistroQueryWorkbenchKibana.chunk." + chunkId + ".js"
/******/ 	}
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/ 	// This file contains only the entry chunk.
/******/ 	// The chunk loading function for additional chunks
/******/ 	__webpack_require__.e = function requireEnsure(chunkId) {
/******/ 		var promises = [];
/******/
/******/
/******/ 		// JSONP chunk loading for javascript
/******/
/******/ 		var installedChunkData = installedChunks[chunkId];
/******/ 		if(installedChunkData !== 0) { // 0 means "already installed".
/******/
/******/ 			// a Promise means "currently loading".
/******/ 			if(installedChunkData) {
/******/ 				promises.push(installedChunkData[2]);
/******/ 			} else {
/******/ 				// setup Promise in chunk cache
/******/ 				var promise = new Promise(function(resolve, reject) {
/******/ 					installedChunkData = installedChunks[chunkId] = [resolve, reject];
/******/ 				});
/******/ 				promises.push(installedChunkData[2] = promise);
/******/
/******/ 				// start chunk loading
/******/ 				var script = document.createElement('script');
/******/ 				var onScriptComplete;
/******/
/******/ 				script.charset = 'utf-8';
/******/ 				script.timeout = 120;
/******/ 				if (__webpack_require__.nc) {
/******/ 					script.setAttribute("nonce", __webpack_require__.nc);
/******/ 				}
/******/ 				script.src = jsonpScriptSrc(chunkId);
/******/
/******/ 				// create error before stack unwound to get useful stacktrace later
/******/ 				var error = new Error();
/******/ 				onScriptComplete = function (event) {
/******/ 					// avoid mem leaks in IE.
/******/ 					script.onerror = script.onload = null;
/******/ 					clearTimeout(timeout);
/******/ 					var chunk = installedChunks[chunkId];
/******/ 					if(chunk !== 0) {
/******/ 						if(chunk) {
/******/ 							var errorType = event && (event.type === 'load' ? 'missing' : event.type);
/******/ 							var realSrc = event && event.target && event.target.src;
/******/ 							error.message = 'Loading chunk ' + chunkId + ' failed.\n(' + errorType + ': ' + realSrc + ')';
/******/ 							error.name = 'ChunkLoadError';
/******/ 							error.type = errorType;
/******/ 							error.request = realSrc;
/******/ 							chunk[1](error);
/******/ 						}
/******/ 						installedChunks[chunkId] = undefined;
/******/ 					}
/******/ 				};
/******/ 				var timeout = setTimeout(function(){
/******/ 					onScriptComplete({ type: 'timeout', target: script });
/******/ 				}, 120000);
/******/ 				script.onerror = script.onload = onScriptComplete;
/******/ 				document.head.appendChild(script);
/******/ 			}
/******/ 		}
/******/ 		return Promise.all(promises);
/******/ 	};
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// on error function for async loading
/******/ 	__webpack_require__.oe = function(err) { console.error(err); throw err; };
/******/
/******/ 	var jsonpArray = window["opendistroQueryWorkbenchKibana_bundle_jsonpfunction"] = window["opendistroQueryWorkbenchKibana_bundle_jsonpfunction"] || [];
/******/ 	var oldJsonpFunction = jsonpArray.push.bind(jsonpArray);
/******/ 	jsonpArray.push = webpackJsonpCallback;
/******/ 	jsonpArray = jsonpArray.slice();
/******/ 	for(var i = 0; i < jsonpArray.length; i++) webpackJsonpCallback(jsonpArray[i]);
/******/ 	var parentJsonpFunction = oldJsonpFunction;
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = "../../packages/kbn-optimizer/target/worker/entry_point_creator.js");
/******/ })
/************************************************************************/
/******/ ({

/***/ "../../node_modules/css-loader/dist/cjs.js?!../../node_modules/postcss-loader/src/index.js?!../../node_modules/sass-loader/dist/cjs.js?!./public/index.scss?v7dark":
/*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** /Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/css-loader/dist/cjs.js??ref--6-oneOf-0-1!/Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/postcss-loader/src??ref--6-oneOf-0-2!/Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/sass-loader/dist/cjs.js??ref--6-oneOf-0-3!./public/index.scss?v7dark ***!
  \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// Imports
var ___CSS_LOADER_API_IMPORT___ = __webpack_require__(/*! ../../../node_modules/css-loader/dist/runtime/api.js */ "../../node_modules/css-loader/dist/runtime/api.js");
exports = ___CSS_LOADER_API_IMPORT___(true);
// Module
exports.push([module.i, "/*\n * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\").\n * You may not use this file except in compliance with the License.\n * A copy of the License is located at\n *\n * http://www.apache.org/licenses/LICENSE-2.0\n *\n * or in the \"license\" file accompanying this file. This file is distributed\n * on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either\n * express or implied. See the License for the specific language governing\n * permissions and limitations under the License.\n */\n.ace-sql-console {\n  background-color: #f5f7fa;\n  color: black; }\n  .ace-sql-console .ace_gutter {\n    background: #edeff3;\n    color: black; }\n  .ace-sql-console .ace_print-margin {\n    width: 1px;\n    background: #e8e8e8; }\n  .ace-sql-console .ace_fold {\n    background-color: #6b72e6; }\n  .ace-sql-console .ace_marker-layer .ace_active-line.ace_active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_marker-layer .ace_selection {\n    background: #b5d5ff; }\n  .ace-sql-console .ace_marker-layer .ace_step {\n    background: #fcff00; }\n  .ace-sql-console .ace_marker-layer .ace_stack {\n    background: #a4e565; }\n  .ace-sql-console .ace_marker-layer .ace_bracket {\n    margin: -1px 0 0 -1px;\n    border: 1px solid silver; }\n  .ace-sql-console .ace_marker-layer .ace_active-line {\n    background: rgba(0, 0, 0, 0.07); }\n  .ace-sql-console .ace_marker-layer .ace_selected-word {\n    background: #fafaff;\n    border: 1px solid #c8c8fa; }\n  .ace-sql-console .ace_cursor {\n    color: black; }\n  .ace-sql-console .ace_invisible {\n    color: #bfbfbf; }\n  .ace-sql-console .ace_storage {\n    color: #9d6af2; }\n  .ace-sql-console .ace_keyword {\n    color: #9d6af2; }\n  .ace-sql-console .ace_constant {\n    color: #c5060b; }\n  .ace-sql-console .ace_constant.ace_buildin {\n    color: #5848f6; }\n  .ace-sql-console .ace_constant.ace_language {\n    color: #585cf6; }\n  .ace-sql-console .ace_constant.ace_library {\n    color: #06960e; }\n  .ace-sql-console .ace_invalid {\n    background-color: rgba(255, 0, 0, 0.1);\n    color: red; }\n  .ace-sql-console .ace_support.ace_function {\n    color: #3c4c72; }\n  .ace-sql-console .ace_support.ace_constant {\n    color: #06960e; }\n  .ace-sql-console .ace_support.ace_type {\n    color: #6d79de; }\n  .ace-sql-console .ace_support.ace_class {\n    color: #6d79de; }\n  .ace-sql-console .ace_keyword.ace_operator {\n    color: #687687; }\n  .ace-sql-console .ace_string {\n    color: #036a07; }\n  .ace-sql-console .ace_comment {\n    color: #4c886b; }\n  .ace-sql-console .ace_comment.ace_doc {\n    color: #0066ff; }\n  .ace-sql-console .ace_comment.ace_doc.ace_tag {\n    color: #809fbf; }\n  .ace-sql-console .ace_constant.ace_numeric {\n    color: mediumblue; }\n  .ace-sql-console .ace_variable {\n    color: #318495; }\n  .ace-sql-console .ace_xml-pe {\n    color: #68685b; }\n  .ace-sql-console .ace_entity.ace_name.ace_function {\n    color: #0000a2; }\n  .ace-sql-console .ace_heading {\n    color: #0c07ff; }\n  .ace-sql-console .ace_list {\n    color: #b90690; }\n  .ace-sql-console .ace_meta.ace_tag {\n    color: #00168e; }\n  .ace-sql-console .ace_string.ace_regex {\n    color: red; }\n  .ace-sql-console .ace_gutter-active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_indent-guide {\n    background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAE0lEQVQImWP4////f4bLly//BwAmVgd1/w11/gAAAABJRU5ErkJggg==\") right repeat-y; }\n\n.ace-sql-console.ace_multiselect .ace_selection.ace_start {\n  box-shadow: 0 0 3px 0px white; }\n\n.ace_editor .ace-sql-console {\n  height: 200px; }\n", "",{"version":3,"sources":["index.scss"],"names":[],"mappings":"AAAA;;;;;;;;;;;;;EAaE;AACF;EACE,yBAAyB;EACzB,YAAY,EAAE;EACd;IACE,mBAAmB;IACnB,YAAY,EAAE;EAChB;IACE,UAAU;IACV,mBAAmB,EAAE;EACvB;IACE,yBAAyB,EAAE;EAC7B;IACE,yBAAyB,EAAE;EAC7B;IACE,mBAAmB,EAAE;EACvB;IACE,mBAAmB,EAAE;EACvB;IACE,mBAAmB,EAAE;EACvB;IACE,qBAAqB;IACrB,wBAAwB,EAAE;EAC5B;IACE,+BAA+B,EAAE;EACnC;IACE,mBAAmB;IACnB,yBAAyB,EAAE;EAC7B;IACE,YAAY,EAAE;EAChB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,sCAAsC;IACtC,UAAU,EAAE;EACd;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,iBAAiB,EAAE;EACrB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,UAAU,EAAE;EACd;IACE,yBAAyB,EAAE;EAC7B;IACE,gKAAgK,EAAE;;AAEtK;EACE,6BAA6B,EAAE;;AAEjC;EACE,aAAa,EAAE","file":"index.scss","sourcesContent":["/*\n * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\").\n * You may not use this file except in compliance with the License.\n * A copy of the License is located at\n *\n * http://www.apache.org/licenses/LICENSE-2.0\n *\n * or in the \"license\" file accompanying this file. This file is distributed\n * on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either\n * express or implied. See the License for the specific language governing\n * permissions and limitations under the License.\n */\n.ace-sql-console {\n  background-color: #f5f7fa;\n  color: black; }\n  .ace-sql-console .ace_gutter {\n    background: #edeff3;\n    color: black; }\n  .ace-sql-console .ace_print-margin {\n    width: 1px;\n    background: #e8e8e8; }\n  .ace-sql-console .ace_fold {\n    background-color: #6b72e6; }\n  .ace-sql-console .ace_marker-layer .ace_active-line.ace_active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_marker-layer .ace_selection {\n    background: #b5d5ff; }\n  .ace-sql-console .ace_marker-layer .ace_step {\n    background: #fcff00; }\n  .ace-sql-console .ace_marker-layer .ace_stack {\n    background: #a4e565; }\n  .ace-sql-console .ace_marker-layer .ace_bracket {\n    margin: -1px 0 0 -1px;\n    border: 1px solid silver; }\n  .ace-sql-console .ace_marker-layer .ace_active-line {\n    background: rgba(0, 0, 0, 0.07); }\n  .ace-sql-console .ace_marker-layer .ace_selected-word {\n    background: #fafaff;\n    border: 1px solid #c8c8fa; }\n  .ace-sql-console .ace_cursor {\n    color: black; }\n  .ace-sql-console .ace_invisible {\n    color: #bfbfbf; }\n  .ace-sql-console .ace_storage {\n    color: #9d6af2; }\n  .ace-sql-console .ace_keyword {\n    color: #9d6af2; }\n  .ace-sql-console .ace_constant {\n    color: #c5060b; }\n  .ace-sql-console .ace_constant.ace_buildin {\n    color: #5848f6; }\n  .ace-sql-console .ace_constant.ace_language {\n    color: #585cf6; }\n  .ace-sql-console .ace_constant.ace_library {\n    color: #06960e; }\n  .ace-sql-console .ace_invalid {\n    background-color: rgba(255, 0, 0, 0.1);\n    color: red; }\n  .ace-sql-console .ace_support.ace_function {\n    color: #3c4c72; }\n  .ace-sql-console .ace_support.ace_constant {\n    color: #06960e; }\n  .ace-sql-console .ace_support.ace_type {\n    color: #6d79de; }\n  .ace-sql-console .ace_support.ace_class {\n    color: #6d79de; }\n  .ace-sql-console .ace_keyword.ace_operator {\n    color: #687687; }\n  .ace-sql-console .ace_string {\n    color: #036a07; }\n  .ace-sql-console .ace_comment {\n    color: #4c886b; }\n  .ace-sql-console .ace_comment.ace_doc {\n    color: #0066ff; }\n  .ace-sql-console .ace_comment.ace_doc.ace_tag {\n    color: #809fbf; }\n  .ace-sql-console .ace_constant.ace_numeric {\n    color: mediumblue; }\n  .ace-sql-console .ace_variable {\n    color: #318495; }\n  .ace-sql-console .ace_xml-pe {\n    color: #68685b; }\n  .ace-sql-console .ace_entity.ace_name.ace_function {\n    color: #0000a2; }\n  .ace-sql-console .ace_heading {\n    color: #0c07ff; }\n  .ace-sql-console .ace_list {\n    color: #b90690; }\n  .ace-sql-console .ace_meta.ace_tag {\n    color: #00168e; }\n  .ace-sql-console .ace_string.ace_regex {\n    color: red; }\n  .ace-sql-console .ace_gutter-active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_indent-guide {\n    background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAE0lEQVQImWP4////f4bLly//BwAmVgd1/w11/gAAAABJRU5ErkJggg==\") right repeat-y; }\n\n.ace-sql-console.ace_multiselect .ace_selection.ace_start {\n  box-shadow: 0 0 3px 0px white; }\n\n.ace_editor .ace-sql-console {\n  height: 200px; }\n"]}]);
// Exports
module.exports = exports;


/***/ }),

/***/ "../../node_modules/css-loader/dist/cjs.js?!../../node_modules/postcss-loader/src/index.js?!../../node_modules/sass-loader/dist/cjs.js?!./public/index.scss?v7light":
/*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** /Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!/Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/postcss-loader/src??ref--6-oneOf-1-2!/Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/sass-loader/dist/cjs.js??ref--6-oneOf-1-3!./public/index.scss?v7light ***!
  \********************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// Imports
var ___CSS_LOADER_API_IMPORT___ = __webpack_require__(/*! ../../../node_modules/css-loader/dist/runtime/api.js */ "../../node_modules/css-loader/dist/runtime/api.js");
exports = ___CSS_LOADER_API_IMPORT___(true);
// Module
exports.push([module.i, "/*\n * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\").\n * You may not use this file except in compliance with the License.\n * A copy of the License is located at\n *\n * http://www.apache.org/licenses/LICENSE-2.0\n *\n * or in the \"license\" file accompanying this file. This file is distributed\n * on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either\n * express or implied. See the License for the specific language governing\n * permissions and limitations under the License.\n */\n.ace-sql-console {\n  background-color: #f5f7fa;\n  color: black; }\n  .ace-sql-console .ace_gutter {\n    background: #edeff3;\n    color: black; }\n  .ace-sql-console .ace_print-margin {\n    width: 1px;\n    background: #e8e8e8; }\n  .ace-sql-console .ace_fold {\n    background-color: #6b72e6; }\n  .ace-sql-console .ace_marker-layer .ace_active-line.ace_active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_marker-layer .ace_selection {\n    background: #b5d5ff; }\n  .ace-sql-console .ace_marker-layer .ace_step {\n    background: #fcff00; }\n  .ace-sql-console .ace_marker-layer .ace_stack {\n    background: #a4e565; }\n  .ace-sql-console .ace_marker-layer .ace_bracket {\n    margin: -1px 0 0 -1px;\n    border: 1px solid silver; }\n  .ace-sql-console .ace_marker-layer .ace_active-line {\n    background: rgba(0, 0, 0, 0.07); }\n  .ace-sql-console .ace_marker-layer .ace_selected-word {\n    background: #fafaff;\n    border: 1px solid #c8c8fa; }\n  .ace-sql-console .ace_cursor {\n    color: black; }\n  .ace-sql-console .ace_invisible {\n    color: #bfbfbf; }\n  .ace-sql-console .ace_storage {\n    color: #9d6af2; }\n  .ace-sql-console .ace_keyword {\n    color: #9d6af2; }\n  .ace-sql-console .ace_constant {\n    color: #c5060b; }\n  .ace-sql-console .ace_constant.ace_buildin {\n    color: #5848f6; }\n  .ace-sql-console .ace_constant.ace_language {\n    color: #585cf6; }\n  .ace-sql-console .ace_constant.ace_library {\n    color: #06960e; }\n  .ace-sql-console .ace_invalid {\n    background-color: rgba(255, 0, 0, 0.1);\n    color: red; }\n  .ace-sql-console .ace_support.ace_function {\n    color: #3c4c72; }\n  .ace-sql-console .ace_support.ace_constant {\n    color: #06960e; }\n  .ace-sql-console .ace_support.ace_type {\n    color: #6d79de; }\n  .ace-sql-console .ace_support.ace_class {\n    color: #6d79de; }\n  .ace-sql-console .ace_keyword.ace_operator {\n    color: #687687; }\n  .ace-sql-console .ace_string {\n    color: #036a07; }\n  .ace-sql-console .ace_comment {\n    color: #4c886b; }\n  .ace-sql-console .ace_comment.ace_doc {\n    color: #0066ff; }\n  .ace-sql-console .ace_comment.ace_doc.ace_tag {\n    color: #809fbf; }\n  .ace-sql-console .ace_constant.ace_numeric {\n    color: mediumblue; }\n  .ace-sql-console .ace_variable {\n    color: #318495; }\n  .ace-sql-console .ace_xml-pe {\n    color: #68685b; }\n  .ace-sql-console .ace_entity.ace_name.ace_function {\n    color: #0000a2; }\n  .ace-sql-console .ace_heading {\n    color: #0c07ff; }\n  .ace-sql-console .ace_list {\n    color: #b90690; }\n  .ace-sql-console .ace_meta.ace_tag {\n    color: #00168e; }\n  .ace-sql-console .ace_string.ace_regex {\n    color: red; }\n  .ace-sql-console .ace_gutter-active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_indent-guide {\n    background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAE0lEQVQImWP4////f4bLly//BwAmVgd1/w11/gAAAABJRU5ErkJggg==\") right repeat-y; }\n\n.ace-sql-console.ace_multiselect .ace_selection.ace_start {\n  box-shadow: 0 0 3px 0px white; }\n\n.ace_editor .ace-sql-console {\n  height: 200px; }\n", "",{"version":3,"sources":["index.scss"],"names":[],"mappings":"AAAA;;;;;;;;;;;;;EAaE;AACF;EACE,yBAAyB;EACzB,YAAY,EAAE;EACd;IACE,mBAAmB;IACnB,YAAY,EAAE;EAChB;IACE,UAAU;IACV,mBAAmB,EAAE;EACvB;IACE,yBAAyB,EAAE;EAC7B;IACE,yBAAyB,EAAE;EAC7B;IACE,mBAAmB,EAAE;EACvB;IACE,mBAAmB,EAAE;EACvB;IACE,mBAAmB,EAAE;EACvB;IACE,qBAAqB;IACrB,wBAAwB,EAAE;EAC5B;IACE,+BAA+B,EAAE;EACnC;IACE,mBAAmB;IACnB,yBAAyB,EAAE;EAC7B;IACE,YAAY,EAAE;EAChB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,sCAAsC;IACtC,UAAU,EAAE;EACd;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,iBAAiB,EAAE;EACrB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,cAAc,EAAE;EAClB;IACE,UAAU,EAAE;EACd;IACE,yBAAyB,EAAE;EAC7B;IACE,gKAAgK,EAAE;;AAEtK;EACE,6BAA6B,EAAE;;AAEjC;EACE,aAAa,EAAE","file":"index.scss","sourcesContent":["/*\n * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\").\n * You may not use this file except in compliance with the License.\n * A copy of the License is located at\n *\n * http://www.apache.org/licenses/LICENSE-2.0\n *\n * or in the \"license\" file accompanying this file. This file is distributed\n * on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either\n * express or implied. See the License for the specific language governing\n * permissions and limitations under the License.\n */\n.ace-sql-console {\n  background-color: #f5f7fa;\n  color: black; }\n  .ace-sql-console .ace_gutter {\n    background: #edeff3;\n    color: black; }\n  .ace-sql-console .ace_print-margin {\n    width: 1px;\n    background: #e8e8e8; }\n  .ace-sql-console .ace_fold {\n    background-color: #6b72e6; }\n  .ace-sql-console .ace_marker-layer .ace_active-line.ace_active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_marker-layer .ace_selection {\n    background: #b5d5ff; }\n  .ace-sql-console .ace_marker-layer .ace_step {\n    background: #fcff00; }\n  .ace-sql-console .ace_marker-layer .ace_stack {\n    background: #a4e565; }\n  .ace-sql-console .ace_marker-layer .ace_bracket {\n    margin: -1px 0 0 -1px;\n    border: 1px solid silver; }\n  .ace-sql-console .ace_marker-layer .ace_active-line {\n    background: rgba(0, 0, 0, 0.07); }\n  .ace-sql-console .ace_marker-layer .ace_selected-word {\n    background: #fafaff;\n    border: 1px solid #c8c8fa; }\n  .ace-sql-console .ace_cursor {\n    color: black; }\n  .ace-sql-console .ace_invisible {\n    color: #bfbfbf; }\n  .ace-sql-console .ace_storage {\n    color: #9d6af2; }\n  .ace-sql-console .ace_keyword {\n    color: #9d6af2; }\n  .ace-sql-console .ace_constant {\n    color: #c5060b; }\n  .ace-sql-console .ace_constant.ace_buildin {\n    color: #5848f6; }\n  .ace-sql-console .ace_constant.ace_language {\n    color: #585cf6; }\n  .ace-sql-console .ace_constant.ace_library {\n    color: #06960e; }\n  .ace-sql-console .ace_invalid {\n    background-color: rgba(255, 0, 0, 0.1);\n    color: red; }\n  .ace-sql-console .ace_support.ace_function {\n    color: #3c4c72; }\n  .ace-sql-console .ace_support.ace_constant {\n    color: #06960e; }\n  .ace-sql-console .ace_support.ace_type {\n    color: #6d79de; }\n  .ace-sql-console .ace_support.ace_class {\n    color: #6d79de; }\n  .ace-sql-console .ace_keyword.ace_operator {\n    color: #687687; }\n  .ace-sql-console .ace_string {\n    color: #036a07; }\n  .ace-sql-console .ace_comment {\n    color: #4c886b; }\n  .ace-sql-console .ace_comment.ace_doc {\n    color: #0066ff; }\n  .ace-sql-console .ace_comment.ace_doc.ace_tag {\n    color: #809fbf; }\n  .ace-sql-console .ace_constant.ace_numeric {\n    color: mediumblue; }\n  .ace-sql-console .ace_variable {\n    color: #318495; }\n  .ace-sql-console .ace_xml-pe {\n    color: #68685b; }\n  .ace-sql-console .ace_entity.ace_name.ace_function {\n    color: #0000a2; }\n  .ace-sql-console .ace_heading {\n    color: #0c07ff; }\n  .ace-sql-console .ace_list {\n    color: #b90690; }\n  .ace-sql-console .ace_meta.ace_tag {\n    color: #00168e; }\n  .ace-sql-console .ace_string.ace_regex {\n    color: red; }\n  .ace-sql-console .ace_gutter-active-line {\n    background-color: #d3dae6; }\n  .ace-sql-console .ace_indent-guide {\n    background: url(\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAE0lEQVQImWP4////f4bLly//BwAmVgd1/w11/gAAAABJRU5ErkJggg==\") right repeat-y; }\n\n.ace-sql-console.ace_multiselect .ace_selection.ace_start {\n  box-shadow: 0 0 3px 0px white; }\n\n.ace_editor .ace-sql-console {\n  height: 200px; }\n"]}]);
// Exports
module.exports = exports;


/***/ }),

/***/ "../../node_modules/css-loader/dist/runtime/api.js":
/*!*************************************************************************************************!*\
  !*** /Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/css-loader/dist/runtime/api.js ***!
  \*************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/*
  MIT License http://www.opensource.org/licenses/mit-license.php
  Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
// eslint-disable-next-line func-names
module.exports = function (useSourceMap) {
  var list = []; // return the list of modules as css string

  list.toString = function toString() {
    return this.map(function (item) {
      var content = cssWithMappingToString(item, useSourceMap);

      if (item[2]) {
        return "@media ".concat(item[2], " {").concat(content, "}");
      }

      return content;
    }).join('');
  }; // import a list of modules into the list
  // eslint-disable-next-line func-names


  list.i = function (modules, mediaQuery, dedupe) {
    if (typeof modules === 'string') {
      // eslint-disable-next-line no-param-reassign
      modules = [[null, modules, '']];
    }

    var alreadyImportedModules = {};

    if (dedupe) {
      for (var i = 0; i < this.length; i++) {
        // eslint-disable-next-line prefer-destructuring
        var id = this[i][0];

        if (id != null) {
          alreadyImportedModules[id] = true;
        }
      }
    }

    for (var _i = 0; _i < modules.length; _i++) {
      var item = [].concat(modules[_i]);

      if (dedupe && alreadyImportedModules[item[0]]) {
        // eslint-disable-next-line no-continue
        continue;
      }

      if (mediaQuery) {
        if (!item[2]) {
          item[2] = mediaQuery;
        } else {
          item[2] = "".concat(mediaQuery, " and ").concat(item[2]);
        }
      }

      list.push(item);
    }
  };

  return list;
};

function cssWithMappingToString(item, useSourceMap) {
  var content = item[1] || ''; // eslint-disable-next-line prefer-destructuring

  var cssMapping = item[3];

  if (!cssMapping) {
    return content;
  }

  if (useSourceMap && typeof btoa === 'function') {
    var sourceMapping = toComment(cssMapping);
    var sourceURLs = cssMapping.sources.map(function (source) {
      return "/*# sourceURL=".concat(cssMapping.sourceRoot || '').concat(source, " */");
    });
    return [content].concat(sourceURLs).concat([sourceMapping]).join('\n');
  }

  return [content].join('\n');
} // Adapted from convert-source-map (MIT)


function toComment(sourceMap) {
  // eslint-disable-next-line no-undef
  var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap))));
  var data = "sourceMappingURL=data:application/json;charset=utf-8;base64,".concat(base64);
  return "/*# ".concat(data, " */");
}

/***/ }),

/***/ "../../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js":
/*!************************************************************************************************************************!*\
  !*** /Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js ***!
  \************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var isOldIE = function isOldIE() {
  var memo;
  return function memorize() {
    if (typeof memo === 'undefined') {
      // Test for IE <= 9 as proposed by Browserhacks
      // @see http://browserhacks.com/#hack-e71d8692f65334173fee715c222cb805
      // Tests for existence of standard globals is to allow style-loader
      // to operate correctly into non-standard environments
      // @see https://github.com/webpack-contrib/style-loader/issues/177
      memo = Boolean(window && document && document.all && !window.atob);
    }

    return memo;
  };
}();

var getTarget = function getTarget() {
  var memo = {};
  return function memorize(target) {
    if (typeof memo[target] === 'undefined') {
      var styleTarget = document.querySelector(target); // Special case to return head of iframe instead of iframe itself

      if (window.HTMLIFrameElement && styleTarget instanceof window.HTMLIFrameElement) {
        try {
          // This will throw an exception if access to iframe is blocked
          // due to cross-origin restrictions
          styleTarget = styleTarget.contentDocument.head;
        } catch (e) {
          // istanbul ignore next
          styleTarget = null;
        }
      }

      memo[target] = styleTarget;
    }

    return memo[target];
  };
}();

var stylesInDom = [];

function getIndexByIdentifier(identifier) {
  var result = -1;

  for (var i = 0; i < stylesInDom.length; i++) {
    if (stylesInDom[i].identifier === identifier) {
      result = i;
      break;
    }
  }

  return result;
}

function modulesToDom(list, options) {
  var idCountMap = {};
  var identifiers = [];

  for (var i = 0; i < list.length; i++) {
    var item = list[i];
    var id = options.base ? item[0] + options.base : item[0];
    var count = idCountMap[id] || 0;
    var identifier = "".concat(id, " ").concat(count);
    idCountMap[id] = count + 1;
    var index = getIndexByIdentifier(identifier);
    var obj = {
      css: item[1],
      media: item[2],
      sourceMap: item[3]
    };

    if (index !== -1) {
      stylesInDom[index].references++;
      stylesInDom[index].updater(obj);
    } else {
      stylesInDom.push({
        identifier: identifier,
        updater: addStyle(obj, options),
        references: 1
      });
    }

    identifiers.push(identifier);
  }

  return identifiers;
}

function insertStyleElement(options) {
  var style = document.createElement('style');
  var attributes = options.attributes || {};

  if (typeof attributes.nonce === 'undefined') {
    var nonce =  true ? __webpack_require__.nc : undefined;

    if (nonce) {
      attributes.nonce = nonce;
    }
  }

  Object.keys(attributes).forEach(function (key) {
    style.setAttribute(key, attributes[key]);
  });

  if (typeof options.insert === 'function') {
    options.insert(style);
  } else {
    var target = getTarget(options.insert || 'head');

    if (!target) {
      throw new Error("Couldn't find a style target. This probably means that the value for the 'insert' parameter is invalid.");
    }

    target.appendChild(style);
  }

  return style;
}

function removeStyleElement(style) {
  // istanbul ignore if
  if (style.parentNode === null) {
    return false;
  }

  style.parentNode.removeChild(style);
}
/* istanbul ignore next  */


var replaceText = function replaceText() {
  var textStore = [];
  return function replace(index, replacement) {
    textStore[index] = replacement;
    return textStore.filter(Boolean).join('\n');
  };
}();

function applyToSingletonTag(style, index, remove, obj) {
  var css = remove ? '' : obj.media ? "@media ".concat(obj.media, " {").concat(obj.css, "}") : obj.css; // For old IE

  /* istanbul ignore if  */

  if (style.styleSheet) {
    style.styleSheet.cssText = replaceText(index, css);
  } else {
    var cssNode = document.createTextNode(css);
    var childNodes = style.childNodes;

    if (childNodes[index]) {
      style.removeChild(childNodes[index]);
    }

    if (childNodes.length) {
      style.insertBefore(cssNode, childNodes[index]);
    } else {
      style.appendChild(cssNode);
    }
  }
}

function applyToTag(style, options, obj) {
  var css = obj.css;
  var media = obj.media;
  var sourceMap = obj.sourceMap;

  if (media) {
    style.setAttribute('media', media);
  } else {
    style.removeAttribute('media');
  }

  if (sourceMap && btoa) {
    css += "\n/*# sourceMappingURL=data:application/json;base64,".concat(btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))), " */");
  } // For old IE

  /* istanbul ignore if  */


  if (style.styleSheet) {
    style.styleSheet.cssText = css;
  } else {
    while (style.firstChild) {
      style.removeChild(style.firstChild);
    }

    style.appendChild(document.createTextNode(css));
  }
}

var singleton = null;
var singletonCounter = 0;

function addStyle(obj, options) {
  var style;
  var update;
  var remove;

  if (options.singleton) {
    var styleIndex = singletonCounter++;
    style = singleton || (singleton = insertStyleElement(options));
    update = applyToSingletonTag.bind(null, style, styleIndex, false);
    remove = applyToSingletonTag.bind(null, style, styleIndex, true);
  } else {
    style = insertStyleElement(options);
    update = applyToTag.bind(null, style, options);

    remove = function remove() {
      removeStyleElement(style);
    };
  }

  update(obj);
  return function updateStyle(newObj) {
    if (newObj) {
      if (newObj.css === obj.css && newObj.media === obj.media && newObj.sourceMap === obj.sourceMap) {
        return;
      }

      update(obj = newObj);
    } else {
      remove();
    }
  };
}

module.exports = function (list, options) {
  options = options || {}; // Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
  // tags it will allow on a page

  if (!options.singleton && typeof options.singleton !== 'boolean') {
    options.singleton = isOldIE();
  }

  list = list || [];
  var lastIdentifiers = modulesToDom(list, options);
  return function update(newList) {
    newList = newList || [];

    if (Object.prototype.toString.call(newList) !== '[object Array]') {
      return;
    }

    for (var i = 0; i < lastIdentifiers.length; i++) {
      var identifier = lastIdentifiers[i];
      var index = getIndexByIdentifier(identifier);
      stylesInDom[index].references--;
    }

    var newLastIdentifiers = modulesToDom(newList, options);

    for (var _i = 0; _i < lastIdentifiers.length; _i++) {
      var _identifier = lastIdentifiers[_i];

      var _index = getIndexByIdentifier(_identifier);

      if (stylesInDom[_index].references === 0) {
        stylesInDom[_index].updater();

        stylesInDom.splice(_index, 1);
      }
    }

    lastIdentifiers = newLastIdentifiers;
  };
};

/***/ }),

/***/ "../../node_modules/val-loader/dist/cjs.js?key=opendistroQueryWorkbenchKibana!../../packages/kbn-ui-shared-deps/public_path_module_creator.js":
/*!************************************************************************************************************************************************************************************************************************************!*\
  !*** /Users/fizhang/workspace/od-kibana/kibana-oss/node_modules/val-loader/dist/cjs.js?key=opendistroQueryWorkbenchKibana!/Users/fizhang/workspace/od-kibana/kibana-oss/packages/kbn-ui-shared-deps/public_path_module_creator.js ***!
  \************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__.p = window.__kbnPublicPath__['opendistroQueryWorkbenchKibana']

/***/ }),

/***/ "../../packages/kbn-optimizer/target/worker/entry_point_creator.js":
/*!*****************************************************************************************************************!*\
  !*** /Users/fizhang/workspace/od-kibana/kibana-oss/packages/kbn-optimizer/target/worker/entry_point_creator.js ***!
  \*****************************************************************************************************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_val_loader_dist_cjs_js_key_opendistroQueryWorkbenchKibana_kbn_ui_shared_deps_public_path_module_creator_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../../../node_modules/val-loader/dist/cjs.js?key=opendistroQueryWorkbenchKibana!../../../kbn-ui-shared-deps/public_path_module_creator.js */ "../../node_modules/val-loader/dist/cjs.js?key=opendistroQueryWorkbenchKibana!../../packages/kbn-ui-shared-deps/public_path_module_creator.js");
/* harmony import */ var _node_modules_val_loader_dist_cjs_js_key_opendistroQueryWorkbenchKibana_kbn_ui_shared_deps_public_path_module_creator_js__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_val_loader_dist_cjs_js_key_opendistroQueryWorkbenchKibana_kbn_ui_shared_deps_public_path_module_creator_js__WEBPACK_IMPORTED_MODULE_0__);
__kbnBundles__.define('plugin/opendistroQueryWorkbenchKibana/public', __webpack_require__, /*require.resolve*/(/*! ../../../../plugins/workbench/public */ "./public/index.ts"))

/***/ }),

/***/ "./common/index.ts":
/*!*************************!*\
  !*** ./common/index.ts ***!
  \*************************/
/*! exports provided: PLUGIN_ID, PLUGIN_NAME */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "PLUGIN_ID", function() { return PLUGIN_ID; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "PLUGIN_NAME", function() { return PLUGIN_NAME; });
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
const PLUGIN_ID = 'opendistroQueryWorkbench';
const PLUGIN_NAME = 'Query Workbench';

/***/ }),

/***/ "./public/index.scss":
/*!***************************!*\
  !*** ./public/index.scss ***!
  \***************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {


switch (window.__kbnThemeTag__) {
  case 'v7dark':
    return __webpack_require__(/*! ./index.scss?v7dark */ "./public/index.scss?v7dark");

  case 'v7light':
    return __webpack_require__(/*! ./index.scss?v7light */ "./public/index.scss?v7light");

  case 'v8dark':
    console.error(new Error("SASS files in [opendistroQueryWorkbenchKibana] were not built for theme [v8dark]. Styles were compiled using the [v7dark] theme instead to keep Kibana somewhat usable. Please adjust the advanced settings to make use of [v7dark,v7light] or make sure the KBN_OPTIMIZER_THEMES environment variable includes [v8dark] in a comma separated list of themes you want to compile. You can also set it to \"*\" to build all themes."));
    return __webpack_require__(/*! ./index.scss?v7dark */ "./public/index.scss?v7dark")

  case 'v8light':
    console.error(new Error("SASS files in [opendistroQueryWorkbenchKibana] were not built for theme [v8light]. Styles were compiled using the [v7light] theme instead to keep Kibana somewhat usable. Please adjust the advanced settings to make use of [v7dark,v7light] or make sure the KBN_OPTIMIZER_THEMES environment variable includes [v8light] in a comma separated list of themes you want to compile. You can also set it to \"*\" to build all themes."));
    return __webpack_require__(/*! ./index.scss?v7light */ "./public/index.scss?v7light")
}

/***/ }),

/***/ "./public/index.scss?v7dark":
/*!**********************************!*\
  !*** ./public/index.scss?v7dark ***!
  \**********************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var api = __webpack_require__(/*! ../../../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js */ "../../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js");
            var content = __webpack_require__(/*! !../../../node_modules/css-loader/dist/cjs.js??ref--6-oneOf-0-1!../../../node_modules/postcss-loader/src??ref--6-oneOf-0-2!../../../node_modules/sass-loader/dist/cjs.js??ref--6-oneOf-0-3!./index.scss?v7dark */ "../../node_modules/css-loader/dist/cjs.js?!../../node_modules/postcss-loader/src/index.js?!../../node_modules/sass-loader/dist/cjs.js?!./public/index.scss?v7dark");

            content = content.__esModule ? content.default : content;

            if (typeof content === 'string') {
              content = [[module.i, content, '']];
            }

var options = {};

options.insert = "head";
options.singleton = false;

var update = api(content, options);

var exported = content.locals ? content.locals : {};



module.exports = exported;

/***/ }),

/***/ "./public/index.scss?v7light":
/*!***********************************!*\
  !*** ./public/index.scss?v7light ***!
  \***********************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

var api = __webpack_require__(/*! ../../../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js */ "../../node_modules/style-loader/dist/runtime/injectStylesIntoStyleTag.js");
            var content = __webpack_require__(/*! !../../../node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!../../../node_modules/postcss-loader/src??ref--6-oneOf-1-2!../../../node_modules/sass-loader/dist/cjs.js??ref--6-oneOf-1-3!./index.scss?v7light */ "../../node_modules/css-loader/dist/cjs.js?!../../node_modules/postcss-loader/src/index.js?!../../node_modules/sass-loader/dist/cjs.js?!./public/index.scss?v7light");

            content = content.__esModule ? content.default : content;

            if (typeof content === 'string') {
              content = [[module.i, content, '']];
            }

var options = {};

options.insert = "head";
options.singleton = false;

var update = api(content, options);

var exported = content.locals ? content.locals : {};



module.exports = exported;

/***/ }),

/***/ "./public/index.ts":
/*!*************************!*\
  !*** ./public/index.ts ***!
  \*************************/
/*! exports provided: plugin, WorkbenchPluginSetup, WorkbenchPluginStart */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "plugin", function() { return plugin; });
/* harmony import */ var _index_scss__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./index.scss */ "./public/index.scss");
/* harmony import */ var _index_scss__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_index_scss__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _plugin__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./plugin */ "./public/plugin.ts");
/* harmony import */ var _types__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./types */ "./public/types.ts");
/* harmony import */ var _types__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(_types__WEBPACK_IMPORTED_MODULE_2__);
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "WorkbenchPluginSetup", function() { return _types__WEBPACK_IMPORTED_MODULE_2__["WorkbenchPluginSetup"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "WorkbenchPluginStart", function() { return _types__WEBPACK_IMPORTED_MODULE_2__["WorkbenchPluginStart"]; });

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

 // This exports static code and TypeScript types,
// as well as, Kibana Platform `plugin()` initializer.

function plugin() {
  return new _plugin__WEBPACK_IMPORTED_MODULE_1__["WorkbenchPlugin"]();
}


/***/ }),

/***/ "./public/plugin.ts":
/*!**************************!*\
  !*** ./public/plugin.ts ***!
  \**************************/
/*! exports provided: WorkbenchPlugin */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "WorkbenchPlugin", function() { return WorkbenchPlugin; });
/* harmony import */ var _common__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../common */ "./common/index.ts");
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

class WorkbenchPlugin {
  setup(core) {
    // Register an application into the side navigation menu
    core.application.register({
      id: 'opendistro-query-workbench',
      title: _common__WEBPACK_IMPORTED_MODULE_0__["PLUGIN_NAME"],
      category: {
        id: 'odfe',
        label: 'Open Distro for Elasticsearch',
        euiIconType: 'logoKibana',
        order: 2000
      },
      order: 1000,

      async mount(params) {
        // Load application bundle
        const {
          renderApp
        } = await Promise.all(/*! import() */[__webpack_require__.e(1), __webpack_require__.e(0)]).then(__webpack_require__.bind(null, /*! ./application */ "./public/application.tsx")); // Get start services as specified in kibana.json

        const [coreStart, depsStart] = await core.getStartServices(); // Render the application

        return renderApp(coreStart, depsStart, params);
      }

    }); // Return methods that should be available to other plugins

    return {};
  }

  start(core) {
    return {};
  }

  stop() {}

}

/***/ }),

/***/ "./public/types.ts":
/*!*************************!*\
  !*** ./public/types.ts ***!
  \*************************/
/*! no static exports found */
/***/ (function(module, exports) {



/***/ }),

/***/ "@elastic/eui":
/*!***********************************************!*\
  !*** external "__kbnSharedDeps__.ElasticEui" ***!
  \***********************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __kbnSharedDeps__.ElasticEui;

/***/ }),

/***/ "@elastic/eui/lib/services":
/*!**********************************************************!*\
  !*** external "__kbnSharedDeps__.ElasticEuiLibServices" ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __kbnSharedDeps__.ElasticEuiLibServices;

/***/ }),

/***/ "@kbn/i18n/react":
/*!*************************************************!*\
  !*** external "__kbnSharedDeps__.KbnI18nReact" ***!
  \*************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __kbnSharedDeps__.KbnI18nReact;

/***/ }),

/***/ "lodash":
/*!*******************************************!*\
  !*** external "__kbnSharedDeps__.Lodash" ***!
  \*******************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __kbnSharedDeps__.Lodash;

/***/ }),

/***/ "react":
/*!******************************************!*\
  !*** external "__kbnSharedDeps__.React" ***!
  \******************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __kbnSharedDeps__.React;

/***/ }),

/***/ "react-dom":
/*!*********************************************!*\
  !*** external "__kbnSharedDeps__.ReactDom" ***!
  \*********************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __kbnSharedDeps__.ReactDom;

/***/ }),

/***/ "react-router-dom":
/*!***************************************************!*\
  !*** external "__kbnSharedDeps__.ReactRouterDom" ***!
  \***************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = __kbnSharedDeps__.ReactRouterDom;

/***/ })

/******/ });
//# sourceMappingURL=opendistroQueryWorkbenchKibana.plugin.js.map