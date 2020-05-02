.. highlight:: sh

=================
Plugin Monitoring
=================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

By a stats endpoint, you are able to collect metrics for the plugin within the interval. Note that only node level statistics collecting is implemented for now. In other words, you only get the metrics for the node you're accessing. Cluster level statistics have yet to be implemented.

Node Stats
==========

Description
-----------

The meaning of fields in the response is as follows:

+---------------------------+---------------------------------------------------------------+
|                 Field name|                                                    Description|
+===========================+===============================================================+
|              request_total|                                         Total count of request|
+---------------------------+---------------------------------------------------------------+
|              request_count|                     Total count of request within the interval|
+---------------------------+---------------------------------------------------------------+
|failed_request_count_syserr|Count of failed request due to system error within the interval|
+---------------------------+---------------------------------------------------------------+
|failed_request_count_cuserr| Count of failed request due to bad request within the interval|
+---------------------------+---------------------------------------------------------------+
|    failed_request_count_cb| Indicate if plugin is being circuit broken within the interval|
+---------------------------+---------------------------------------------------------------+


Example
-------

SQL query::

	>> curl -H 'Content-Type: application/json' -X GET localhost:9200/_opendistro/_sql/stats

Result set::

	{
	  "failed_request_count_cb" : 0,
	  "failed_request_count_cuserr" : 0,
	  "circuit_breaker" : 0,
	  "request_total" : 49,
	  "request_count" : 0,
	  "failed_request_count_syserr" : 0
	}

