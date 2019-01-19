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

var MappingParser = function(data) {

	var parsedMapping = parseMapping(data);
	this.mapping = parsedMapping;
}


function parseMapping(mapping){
	var indexToTypeToFields = {};
	for(index in mapping){
		var types = mapping[index]["mappings"];
		var typeToFields = {};
		for(type in types){
			var fields = types[type]["properties"];
			fieldsFlatten = {};
			getFieldsRecursive(fields,fieldsFlatten,"");
			typeToFields[type] = fieldsFlatten;
		}

		indexToTypeToFields[index] = typeToFields;
	}
	return indexToTypeToFields;	
}

function getFieldsRecursive(fields,fieldsFlatten,prefix){
	for(field in fields){
		var fieldMapping = fields[field];
		if("type" in fieldMapping){
			fieldsFlatten[prefix+field] = fieldMapping;
		}
		if(!("type" in fieldMapping) || fieldMapping.type == "nested") {
			getFieldsRecursive(fieldMapping["properties"],fieldsFlatten,prefix+field+".");
		}
	}
}

MappingParser.prototype.getIndices = function() {
  return Object.keys(this.mapping);
};

MappingParser.prototype.getTypes = function(index) {
  return Object.keys(this.mapping[index]);
};

MappingParser.prototype.getFieldsForType = function(index,type) {
  return Object.keys(this.mapping[index][type]);
};
MappingParser.prototype.getFieldsForTypeWithMapping = function(index,type) {
  return this.mapping[index][type];
};

