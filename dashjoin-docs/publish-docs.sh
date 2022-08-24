echo This will PUBLISH version $(cat ../../VERSION) of the Dashjoin documentation

function ask_yes_or_no() {
    read -p "$1 ([y]es or [n]o): "
    case $(echo $REPLY | tr '[A-Z]' '[a-z]') in
        y|yes) echo "yes" ;;
        *)     echo "no" ;;
    esac
}
if [[ "no" == $(ask_yes_or_no "Are you sure?") ]]
then
    echo "Skipped."
    exit 0
fi

mike deploy --push --verbose $(cat ../../VERSION) latest
