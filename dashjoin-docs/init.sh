# Install py mkdocs
python3 -m venv ./.venv
.venv/bin/pip install mkdocs mkdocs-material mike
export PATH=$PATH:.venv/bin
