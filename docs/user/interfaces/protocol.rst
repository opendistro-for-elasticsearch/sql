.. highlight:: sh

========
Protocol
========

.. rubric:: Table of contents

.. contents::
   :local:


Introduction
============

SQL plugin provides multiple protocols for different purposes.

Elasticsearch DSL
=================

Examples
--------

By default the plugin returns original response from Elasticsearch in JSON. Because this is the native response  from Elasticsearch, extra efforts are needed to parse and interpret it. Meanwhile mutation like field alias will  not be present inside.

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=jdbc -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts LIMIT 2"
	}'

Result set::

	{  "schema": [    {      "name": "firstname",      "type": "text"    },    {      "name": "lastname",      "type": "text"    },    {      "name": "age",      "type": "long"    },    {      "name": "city",      "type": "text"    }  ],  "total": 4,  "datarows": [    [      "Nanette",      "Bates",      28,      "Nogal"    ],    [      "Hattie",      "Bond",      36,      "Dante"    ]  ],  "size": 2,  "status": 200}

JDBC Format
===========

Examples
--------

JDBC format is provided for JDBC driver and client side that needs both schema and result set formatted

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=jdbc -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts LIMIT 2"
	}'

Result set::

	{  "schema": [    {      "name": "firstname",      "type": "text"    },    {      "name": "lastname",      "type": "text"    },    {      "name": "age",      "type": "long"    },    {      "name": "city",      "type": "text"    }  ],  "total": 4,  "datarows": [    [      "Nanette",      "Bates",      28,      "Nogal"    ],    [      "Hattie",      "Bond",      36,      "Dante"    ]  ],  "size": 2,  "status": 200}

CSV Format
==========

Examples
--------

You can also use CSV format to download result set in csv format

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=csv -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts"
	}'

Result set::

	firstname,lastname,age,cityNanette,Bates,28,NogalHattie,Bond,36,DanteAmber,Duke,32,BroganDale,Adams,33,Orick

Raw Format
==========

Examples
--------

Additionally you can also use RAW format to pipe the result with other command line tool for post processing

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql?format=raw -d '{
	  "query": "SELECT firstname, lastname, age, city FROM accounts"
	}'

Result set::

	Nanette|Bates|28|NogalHattie|Bond|36|DanteAmber|Duke|32|BroganDale|Adams|33|Orick

