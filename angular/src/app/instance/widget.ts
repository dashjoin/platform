/**
 * the widget layout description underlying a node in the component tree
 */
export interface Widget {

    /**
     * type of widget
     */
    widget:
    // chart.js chart displaying query results
    'chart'
    |
    // record / instance edit widget
    'edit'
    |
    // edit session / user variable
    'variable'
    |
    // like chart but shows table
    'table'
    |
    // displays session variables / results of backend REST calls
    'display'
    |
    // shows incoming and outgoing instance links
    'links'
    |
    // upload
    'upload'
    |
    // indicates http busy state
    'activity-status'
    |
    // query editor
    'queryeditor'
    |
    // page
    'page'
    |
    // container
    'container'
    |
    // grid container
    'grid'
    |
    // card container
    'card'
    |
    // expansion container
    'expansion'
    |
    // toolbar container
    'toolbar'
    |
    // widget to toggle layout edit mode / save layout
    'layout-edit-switch'
    |
    // simple text
    'text'
    |
    // icon with tooltip and hyperlink
    'icon'
    |
    // spacer that makes the remainder of the div align right
    'spacer'
    |
    // create new instance
    'create'
    |
    // action button
    'button'
    |
    // edit 1:n relationship from parent record
    'editRelated'
    |
    // search box in toolbar
    'search'
    |
    // markdown widget
    'markdown'
    ;

    /**
     * database the widget operates on
     */
    database: string;

    /**
     * query to run
     */
    query: string;

    /**
     * query argument
     */
    arguments: any;

    /**
     * like "a href" attribute
     */
    href: string;

    /**
     * display expression
     */
    display: any;

    /**
     * allows overriding model schema in a layout
     */
    schema: any;

    /**
     * container children
     */
    children: Widget[];

    /**
     * ready only input component (used in toolbar in order to limit the edit context menus)
     */
    readOnly: boolean;

    /**
     * query is a graph query
     */
    graph: boolean;

    /**
     * static text
     */
    text: string;

    /**
     * widget title on a page
     */
    title: string;

    /**
     * available page layouts
     */
    pageLayout: '1 column' | '2 column' | '3 column' | 'T 2 column' | 'T 3 column' | 'horizontal' | 'grid';

    /**
     * material icon
     */
    icon: string;

    /**
     * widget tooltip
     */
    tooltip: string;

    /**
     * chart type
     */
    chart: 'bar' | 'line' | 'doughnut' | 'radar' | 'polarArea';

    /**
     * property ID reference (editRelated)
     */
    prop: 'string';

    /**
     * schema for create op
     */
    createSchema: any;

    /**
     * columns to display in the editRelated table display
     */
    columns: string[];

    /**
     * allowed roles for this widget
     */
    roles: string[];

    /**
     * show a confirmation message before deleting
     */
    deleteConfirmation: string;

    /**
     * expression to evaluate onClick
     */
    print: string;

    /**
     * expression to evaluate onClick
     */
    navigate: string;

    /**
     * container vertical (default) / horizontal
     */
    layout: string;

    /**
     * markdown text for markdown widget
     */
    markdown: string;

    /**
     * CSS class
     */
    class: string[];

    /**
     * CSS style
     */
    style: object;

    /**
     * properties for button input schema
     */
    properties: object;

    /**
     * additional context expression
     */
    context: string;
}
