/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

#ifndef __VERSION_H__
#define __VERSION_H__

/*
 *	BuildAll may pass ELASTICDRIVERVERSION, ELASTIC_RESOURCE_VERSION
 *	and ES_DRVFILE_VERSION via winbuild/elasticodbc.vcxproj.
 */
#ifdef ES_ODBC_VERSION

#ifndef ELASTICSEARCHDRIVERVERSION
#define ELASTICSEARCHDRIVERVERSION ES_ODBC_VERSION
#endif
#ifndef ELASTICSEARCH_RESOURCE_VERSION
#define ELASTICSEARCH_RESOURCE_VERSION ELASTICSEARCHDRIVERVERSION
#endif
#ifndef ES_DRVFILE_VERSION
#define ES_DRVFILE_VERSION ES_ODBC_DRVFILE_VERSION
#endif

#endif  // ES_ODBC_VERSION

#endif
