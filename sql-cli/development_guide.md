## Development Guide
### Development Environment Set Up
- `pip install virtualenv`
- `virtualenv venv` to create virtual environment for **Python 3**
- `source ./venv/bin/activate` activate virtual env.
- `cd` into project root folder.
- `pip install --editable .` will install all dependencies from `setup.py`.

### Run CLI
- Start an Elasticsearch instance from either local, Docker with Open Distro SQL plugin, or AWS Elasticsearch
- To launch the cli, use 'wake' word `odfesql` followed by endpoint of your running ES instance. If not specifying 
any endpoint, it uses http://localhost:9200 by default. If not provided with port number, http endpoint uses 9200 and 
https uses 443 by default.

### Testing
- Prerequisites
    - Build the application
    - Start a local Elasticsearch instance (OSS) with 
    [Open Distro SQL plugin for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/) installed
    and listening at http://localhost:9200.
- Pytest
    - `pip install -r requirements-dev.txt` Install test frameworks including Pytest and mock.
    - `cd` into `tests` and run `pytest`
- Refer to [test_plan](./tests/test_plan.md) for manual test guidance.

### Style
- Use [black](https://github.com/psf/black) to format code, with option of `--line-length 120`

## Release guide

- Package Manager: pip
- Repository of software for Python: PyPI

### Workflow

1. Update version number
    1. Modify the version number in `__init__.py` under `src` package. It will be used by `setup.py` for release.
2. Create/Update `setup.py` (if needed)
    1. For more details refer to https://packaging.python.org/tutorials/packaging-projects/#creating-setup-py 
3. Update README.md, Legal and copyright files(if needed)
    1. Update README.md when there is a critical feature added.
    2. Update `THIRD-PARTY` files if there is a new dependency added.
4. Generate distribution archives
    1. Make sure you have the latest versions of `setuptools` and `wheel` installed:  `python3 -m pip install --user --upgrade setuptools wheel`
    2. Run this command from the same directory where `setup.py` is located: `python3 setup.py sdist bdist_wheel`
    3. Check artifacts under `sql-cli/dist/`, there should be a `.tar.gz` file and a `.whi` file with correct version. Remove other deprecated artifacts.
5. Upload the distribution archives to TestPyPI
    1. Register an account on [testPyPI](https://test.pypi.org/)
    2. `python3 -m pip install --user --upgrade twine`
    3. `python3 -m twine upload --repository-url https://test.pypi.org/legacy/ dist/*`
6. Install your package from TestPyPI and do manual test
    1. `pip install --index-url https://test.pypi.org/simple/ --extra-index-url https://pypi.org/simple odfe-sql-cli`
7. Upload to PyPI
    1. Register an account on [PyPI](https://pypi.org/), note that these are two separate servers and the credentials from the test server are not shared with the main server.
    2. Use `twine upload dist/*` to upload your package and enter your credentials for the account you registered on PyPI.You don’t need to specify --repository; the package will upload to https://pypi.org/ by default.
8. Install your package from PyPI using `pip install [your-package-name]`

### Reference
- https://medium.com/@joel.barmettler/how-to-upload-your-python-package-to-pypi-65edc5fe9c56
- https://packaging.python.org/tutorials/packaging-projects/
- https://packaging.python.org/guides/using-testpypi/