# HOWTO Edit Dashjoin Documentation

Docs are maintained in the docs folder, all in Markup format.

The Doc Site is in mkdocs format <https://www.mkdocs.org>

The main config file is mkdocs.yml

The published docs are hosted on GitHub pages <https://dashjoin.github.io/platform>

The assets are all stored in the Github gh-pages branch <https://github.com/dashjoin/platform/tree/gh-pages>

For each published version, a folder with the version number in the gh-pages branch contains the published documentation files.

## Local Build Environment

You need Python plus you need to setup the mkdocs and mike packages <https://github.com/jimporter/mike>

The init.sh script does that for you.

## Editing and Testing

Use

mkdocs serve

to edit and view locally. This only shows the current version.

Also check the tool's output to see any errors (wrong links, missing assets etc.)

## Publishing the Latest Version

Version management is done through Mike <https://github.com/jimporter/mike>

When the new version is ready, use

mike deploy VERSION

to deploy the currently edited version as VERSION (i.e. 2.5).

The publish-docs.sh script will do the publishing to latest (basically: mike deploy --push VERSION latest)

## Legacy Documentation (versions before 2.5)

Legacy Docs are scaffolded from the single README.md file
See script make-legacy-docs.sh which does the work

Publishing:
also done using mike deploy VERSION
