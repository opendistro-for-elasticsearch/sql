.. highlight:: sh

========
Endpoint
========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

To send query request to SQL plugin, you can either use a request parameter in HTTP GET or request body by HTTP POST request. POST request is recommended because it doesn't have length limitation and allows for other parameters passed to plugin for other functionality such as prepared statement. And also the explain endpoint is used very often for query translation and troubleshooting.

