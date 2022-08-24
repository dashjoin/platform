# Markdown tool to lower headings of whole doc
# i.e. ### Section -> ## Section
# Useful when extracting subsections into new doc
echo Lowering Markdown Heading of $1
sed -i .bak 's/^##/#/g' $1
