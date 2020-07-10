# Open Distro for Elasticsearch SQL Workbench

The Open Distro for Elasticsearch SQL Workbench enables you to query your Elasticsearch data using SQL syntax from a dedicated Kibana UI. You can download your query results data in JSON, JDBC, CSV and raw text formats.


## Documentation

Please see our technical [documentation](https://opendistro.github.io/for-elasticsearch-docs/) to learn more about its features.


## Setup

1. Download Elasticsearch for the version that matches the [Kibana version specified in package.json](./package.json#L8).
1. Download and install the most recent version of [Open Distro for Elasticsearch SQL plugin](https://github.com/opendistro-for-elasticsearch/sql).
1. Download the Kibana source code for the [version specified in package.json](./package.json#L8) you want to set up.

   See the [Kibana contributing guide](https://github.com/elastic/kibana/blob/master/CONTRIBUTING.md#setting-up-your-development-environment) for more instructions on setting up your development environment.
   
1. Change your node version to the version specified in `.node-version` inside the Kibana root directory.
1. cd into `plugins` directory in the Kibana source code directory.
1. Check out this package from version control into the `plugins` directory.
1. Run `yarn kbn bootstrap` inside `kibana/plugins/sql-workbench`.

Ultimately, your directory structure should look like this:

```md
.
├── kibana
│   └── plugins
│       └── sql-workbench
```


## Build

To build the plugin's distributable zip simply run `yarn build`.

Example output: `./build/opendistro-sql-workbench-*.zip`


## Run

- `yarn start`

  Starts Kibana and includes this plugin. Kibana will be available on `localhost:5601`.

- `NODE_PATH=../../node_modules yarn test:jest`

  Runs the plugin tests.


## Contributing to Open Distro for Elasticsearch SQL Workbench

- Refer to [CONTRIBUTING.md](./CONTRIBUTING.md).
- Since this is a workbench, it can be useful to review the [Kibana contributing guide](https://github.com/elastic/kibana/blob/master/CONTRIBUTING.md) alongside the documentation around [workbenchs](https://www.elastic.co/guide/en/kibana/master/kibana-plugins.html) and [plugin development](https://www.elastic.co/guide/en/kibana/master/plugin-development.html).

## Bugs, Enhancements or Questions

Please file an issue to report any bugs you may find, enhancements you may need or questions you may have [here](https://github.com/opendistro-for-elasticsearch/sql-workbench/issues).

## License

This code is licensed under the Apache 2.0 License. 

## Copyright

Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
