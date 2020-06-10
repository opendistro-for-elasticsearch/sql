
===============================
OpenDistro PPL Reference Manual
===============================

| Open Distro for Elasticsearch PPL enables you to extract insights out of Elasticsearch using the familiar pipe processing language query syntax. A PPL query is a read-only request to process data and return result.
| The query consists of a sequence of command, delimited by a pipe (|). The query start with search command and then flowing a set of command delimited by pipe (|).
| for example, the following query retrieve firstname and lastname from accounts if age large than 18.

.. code-block::

   source=accounts
   | where age > 18
   | fields firstname, lastname

* **Interfaces**

  - `Endpoint <interfaces/endpoint.rst>`_

  - `Protocol <interfaces/protocol.rst>`_

* **Commands**

  - `syntax <cmd/syntax>`_

  - `field command <cmd/dedup.rst>`_

  - `field command <cmd/fields.rst>`_

  - `search command <cmd/search.rst>`_

  - `where command <cmd/where.rst>`_

* **Function**

  - `Basic Operators <dql/basics.rst>`_
