.. highlight:: sh

========
Protocol
========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

For the protocol, SQL plugin provides multiple response formats for different purposes while the request format is same for all. Except JDBC format, all other responses are basically in their original format. Because of the requirements for schema information and functionality such as pagination, JDBC driver requires a different and well defined response format.


