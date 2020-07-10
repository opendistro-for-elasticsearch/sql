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
import errno
import os
import platform
import shutil

from os.path import expanduser, exists, dirname
from configobj import ConfigObj


def config_location():
    """Return absolute conf file path according to different OS."""
    if "XDG_CONFIG_HOME" in os.environ:
        return "%s/odfesql-cli/" % expanduser(os.environ["XDG_CONFIG_HOME"])
    elif platform.system() == "Windows":
        # USERPROFILE is typically C:\Users\{username}
        return "%s\\AppData\\Local\\dbcli\\odfesql-cli\\" % os.getenv("USERPROFILE")
    else:
        return expanduser("~/.config/odfesql-cli/")


def _load_config(user_config, default_config=None):
    config = ConfigObj()
    config.merge(ConfigObj(default_config, interpolation=False))
    config.merge(ConfigObj(expanduser(user_config), interpolation=False, encoding="utf-8"))
    config.filename = expanduser(user_config)

    return config


def ensure_dir_exists(path):
    """
    Try to create config file in OS.

    Ignore existing destination. Raise error for other OSError, such as errno.EACCES (Permission denied),
    errno.ENOSPC (No space left on device)
    """
    parent_dir = expanduser(dirname(path))
    try:
        os.makedirs(parent_dir)
    except OSError as exc:
        if exc.errno != errno.EEXIST:
            raise


def _write_default_config(source, destination, overwrite=False):
    destination = expanduser(destination)
    if not overwrite and exists(destination):
        return

    ensure_dir_exists(destination)
    shutil.copyfile(source, destination)


# https://stackoverflow.com/questions/40193112/python-setuptools-distribute-configuration-files-to-os-specific-directories
def get_config(clirc_file=None):
    """
    Get config for odfesql cli.

    This config comes from either existing config in the OS, or create a config file in the OS, and write default config
    including in the package to it.
    """
    from .conf import __file__ as package_root

    package_root = os.path.dirname(package_root)

    clirc_file = clirc_file or "%sconfig" % config_location()
    default_config = os.path.join(package_root, "clirc")

    _write_default_config(default_config, clirc_file)

    return _load_config(clirc_file, default_config)
