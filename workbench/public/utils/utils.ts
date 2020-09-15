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

import {ItemIdToExpandedRowMap, QueryMessage, ResponseDetail} from '../components/Main/main';
import {MESSAGE_TAB_LABEL} from "./constants";

// It returns an array of queries
export const getQueries = (queriesString: string): string[] => {
  if (queriesString == '') {
    return [];
  }

  return queriesString
    .split(';')
    .map((query: string) => query.trim())
    .filter((query: string) => query != '');
};

// It retrieves the index from the query. The index is used to label the query results tab
export function getQueryIndex(query: string): string {
  if (query) {
    const queryFrom : string []= query.toLowerCase().split("from");

    if (queryFrom.length > 1){
      return queryFrom[1].split(" ")[1];
    }
  }
 return query;
}

// Tabs utils
export function getDefaultTabId ( queryResults: ResponseDetail<string>[]) : string {
  return queryResults && queryResults.length > 0 && queryResults[0].fulfilled ? "0" : MESSAGE_TAB_LABEL
}

export function getDefaultTabLabel ( queryResults: ResponseDetail<string>[], queryString: string  ) : string {

  return queryResults && queryResults.length > 0 && queryResults[0].fulfilled ? getQueryIndex(queryString) : MESSAGE_TAB_LABEL
}

// It returns the results for the selected tab
export function getSelectedResults (results: ResponseDetail<any>[], selectedTabId: string ): any {
  const selectedIndex: number = parseInt(selectedTabId);
  if (!Number.isNaN(selectedIndex) && results) {
    const selectedResult: ResponseDetail<any> = results[selectedIndex];
    return selectedResult && selectedResult.fulfilled ? selectedResult.data : undefined;
  }
  return undefined;
}

export function isEmpty (obj: object) : boolean {
  for (const key in obj) {
    if(obj.hasOwnProperty(key)) { return false; }
  }
  return true;
}

export function capitalizeFirstLetter(name: string): string {
  return name && name.length > 0 ? name.charAt(0).toUpperCase() + name.slice(1) : name;
}

export function getMessageString(messages: QueryMessage[], tabNames: string[]): string {
  return messages && messages.length > 0 && tabNames && tabNames.length > 0 ? messages.reduce( (finalMessage, message, currentIndex) => finalMessage.concat(capitalizeFirstLetter(tabNames[currentIndex]), ': ', messages[currentIndex].text, '\n\n'), '' ) : '';
}

export function scrollToNode(nodeId: string): void {
  const element = document.getElementById(nodeId);
  if (element) {
    element.scrollIntoView();
  }
}

// Download functions
export function onDownloadFile(data: any, fileFormat: string, fileName: string) {
  const encodedUri = encodeURI(data);
  const content = 'data:text/'+fileFormat+';charset=utf-8,' + encodedUri;
  const link = document.createElement("a");
  link.setAttribute('href', content);
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

export class Tree {
  _root: Node;
  constructor(data: any, rootId: string) {
    this._root = new Node(data, rootId, '', this._root);
  }
}

export class Node {
  data: any;
  name: string;
  children: Node[];
  parent: Node;
  nodeId: string;

  constructor(data: any, parentId: string, name = '', parent: Node ) {
    this.data = data;
    this.name = name;
    this.children = [];
    this.parent = parent;
    this.nodeId = name === '' ? parentId : parentId + '_' + name;
  }
}

// It creates a tree of nested objects or arrays for the row, where rootId is the rowId and item is the field value
export function createRowTree(item: any, rootId: string) {
  const tree = new Tree(item, rootId);
  const root = tree._root;

  if (typeof item === 'object') {
    for (let j = 0; j < Object.keys(item).length; j++) {
      const itemKey: string = Object.keys(item)[j];
      let data = item[itemKey];
      // If value of field is an array or an object it gets added to the tree
      if (data !== null && (Array.isArray(data) || typeof data === 'object')) {
        const firstNode = new Node(data, rootId, itemKey, root);
        root.children.push(firstNode);
      }
    }
  }
  return tree;
}

// It returns the tree for the given nodeId if it exists or create a new one otherwise
export function getRowTree(nodeId: string, item: any, expandedRowMap: ItemIdToExpandedRowMap) {
  return expandedRowMap[nodeId] && expandedRowMap[nodeId].nodes
    ? expandedRowMap[nodeId].nodes
    : createRowTree(item, nodeId);
}

export function findRootNode(node: Node, expandedRowMap: ItemIdToExpandedRowMap){
  const rootNodeId = node.nodeId.split('_')[0];
  return expandedRowMap[rootNodeId].nodes._root;
}

/********* TABS Functions *********/
//It checks if an element needs a scrolling
export function needsScrolling(elementId: string){
  const element = document.getElementById(elementId);
  if (element === null) {
    return false;
  }
  return element.scrollWidth > element.offsetWidth;
}
