echo Lowering Markdown Heading of $1
sed -i .bak 's/^##/#/g' $1
