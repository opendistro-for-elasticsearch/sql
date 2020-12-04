
===============================
OpenDistro PPL Reference Manual
===============================

Overview
---------
Piped Processing Language (PPL), powered by Open Distro for Elasticsearch, enables Open Distro for Elasticsearch users with exploration and discovery of, and finding search patterns in data stored in Elasticsearch, using a set of commands delimited by pipes (|). These are essentially read-only requests to process data and return results.

Currently, Open Distro for Elasticsearch users can query data using either Query DSL or SQL. Query DSL is powerful and fast. However, it has a steep learning curve, and was not designed as a human interface to easily create ad hoc queries and explore user data. SQL allows users to extract and analyze data in Elasticsearch in a declarative manner. Open Distro for Elasticsearch now makes its search and query engine robust by introducing Piped Processing Language (PPL). It enables users to extract insights from Elasticsearch with a sequence of commands delimited by pipes (|). It supports  a comprehensive set of commands including search, where, fields, rename, dedup, sort, eval, head, top and rare, and functions, operators and expressions. Even new users who have recently adopted Open Distro for Elasticsearch, can be productive day one, if they are familiar with the pipe (|) syntax. It enables developers, DevOps engineers, support engineers, site reliability engineers (SREs), and IT managers to effectively discover and explore log, monitoring and observability data stored in Open Distro for Elasticsearch.

We expand the capabilities of our Workbench, a comprehensive and integrated visual query tool currently supporting only SQL, to run on-demand PPL commands, and view and save results as text and JSON. We also add  a new interactive standalone command line tool, the PPL CLI, to run on-demand PPL commands, and view and save results as text and JSON.

The query start with search command and then flowing a set of command delimited by pipe (|).
| for example, the following query retrieve firstname and lastname from accounts if age large than 18.

.. code-block::

   source=accounts
   | where age > 18
   | fields firstname, lastname

* **Interfaces**

  - `Endpoint <interfaces/endpoint.rst>`_

  - `Protocol <interfaces/protocol.rst>`_

* **Administration**

  - `Plugin Settings <admin/settings.rst>`_

  - `Monitoring <admin/monitoring.rst>`_

* **Commands**

  - `Syntax <cmd/syntax.rst>`_

  - `dedup command <cmd/dedup.rst>`_

  - `eval command <cmd/eval.rst>`_

  - `fields command <cmd/fields.rst>`_

  - `rename command <cmd/rename.rst>`_

  - `search command <cmd/search.rst>`_

  - `sort command <cmd/sort.rst>`_

  - `stats command <cmd/stats.rst>`_

  - `where command <cmd/where.rst>`_

  - `head command <cmd/head.rst>`_
  
  - `rare command <cmd/rare.rst>`_

  - `top command <cmd/top.rst>`_

* **Functions**

  - `PPL Functions <../../user/dql/functions.rst>`_

* **Optimization**

  - `Optimization <../../user/optimization/optimization.rst>`_

* **Language Structure**

  - `Identifiers <general/identifiers.rst>`_

  - `Data Types <general/datatypes.rst>`_