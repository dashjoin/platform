/**
 * JSON schema description of a property
 */
export class Property {

    /**
     * property name
     */
    public name: string;

    /**
     * property primary key
     */
    public ID: string;

    /**
     * parent pk
     */
    public parent: string;

    /**
     * null if prop is no PK, the position in the (composite) PK otherwise (starting from 0)
     */
    public pkpos: number;

    /**
     * null if the property is no FK, PK of the property it points to otherwise
     */
    public ref: string;

    /**
     * poperty type
     */
    public type: string;

    /**
     * json schema title
     */
    public title: string;

    /**
     * json schema description
     */
    public description: string;

    /**
     * json schema nested properties
     */
    public properties: { [key: string]: Property };
}

/**
 * represents a table
 */
export class Table {

    /**
     * table name
     */
    public name: string;

    /**
     * table PK
     */
    public ID: string;

    /**
     * pk of the containing DB
     */
    public parent: string;

    /**
     * defaults to object
     */
    public type: string;

    /**
     * table columns
     */
    public properties: { [key: string]: Property };
}
