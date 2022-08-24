# This script creates the docs from README.md
# for Dashjoin versions <2.5

# Create folders
mkdir -p docs/angular/src/assets

# Copy the source markup to docs
cp ../README.md docs/index.md
cp ../angular/src/assets/features.png docs/angular/src/assets/features.png
cp ../angular/src/assets/jsonata.png docs/angular/src/assets/jsonata.png
cp ../angular/src/assets/openid.png docs/angular/src/assets/openid.png

# Patch incompatible markup
sed -i -e 's/```shell\ script/```bash/g' docs/index.md

# Build docs
# Local: mkdocs build or mkdocs serve
# Publish: mike deploy -p X.YY
