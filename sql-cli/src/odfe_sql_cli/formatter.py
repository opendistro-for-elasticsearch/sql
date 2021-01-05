"""
Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License").
You may not use this file except in compliance with the License.
A copy of the License is located at

    http://www.apache.org/licenses/LICENSE-2.0

or in the "license" file accompanying this file. This file is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
express or implied. See the License for the specific language governing
permissions and limitations under the License.
"""
import click
import itertools

from cli_helpers.tabular_output import TabularOutputFormatter
from cli_helpers.tabular_output.preprocessors import format_numbers

click.disable_unicode_literals_warning = True


class Formatter:
    """Formatter instance is used to format the data retrieved from Elasticsearch."""

    def __init__(self, settings):
        """A formatter can be customized by passing settings as a parameter."""
        self.settings = settings
        self.table_format = "vertical" if self.settings.is_vertical else self.settings.table_format
        self.max_width = self.settings.max_width

        def format_array(val):
            if val is None:
                return self.settings.missingval
            if not isinstance(val, list):
                return val
            return "[" + ",".join(str(format_array(e)) for e in val) + "]"

        def format_arrays(field_data, headers, **_):
            field_data = list(field_data)
            for row in field_data:
                row[:] = [format_array(val) if isinstance(val, list) else val for val in row]

            return field_data, headers

        self.output_kwargs = {
            "sep_title": "RECORD {n}",
            "sep_character": "-",
            "sep_length": (1, 25),
            "missing_value": self.settings.missingval,
            "preprocessors": (format_numbers, format_arrays),
            "disable_numparse": True,
            "preserve_whitespace": True,
            "style": self.settings.style_output,
        }

    def format_output(self, data):
        """Format data.

        :param data: raw data get from ES
        :return: formatted output, it's either table or vertical format
        """
        formatter = TabularOutputFormatter(format_name=self.table_format)

        # parse response data
        datarows = data["datarows"]
        schema = data["schema"]
        total_hits = data["total"]
        cur_size = data["size"]
        # unused data for now,
        fields = []
        types = []

        # get header and type as lists, for future usage
        for i in schema:
            fields.append(i.get("alias", i["name"]))
            types.append(i["type"])

        output = formatter.format_output(datarows, fields, **self.output_kwargs)
        output_message = "fetched rows / total rows = %d/%d" % (cur_size, total_hits)

        # Open Distro for ES sql has a restriction of retrieving 200 rows of data by default
        if total_hits > 200 == cur_size:
            output_message += "\n" + "Attention: Use LIMIT keyword when retrieving more than 200 rows of data"

        # check width overflow, change format_name for better visual effect
        first_line = next(output)
        output = itertools.chain([output_message], [first_line], output)

        if len(first_line) > self.max_width:
            click.secho(message="Output longer than terminal width", fg="red")
            if click.confirm("Do you want to display data vertically for better visual effect?"):
                output = formatter.format_output(datarows, fields, format_name="vertical", **self.output_kwargs)
                output = itertools.chain([output_message], output)

        # TODO: if decided to add row_limit. Refer to pgcli -> main -> line 866.

        return output
