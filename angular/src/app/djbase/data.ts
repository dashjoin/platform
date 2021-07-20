import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Schema } from '@dashjoin/json-schema-form/public-api';
import jsonata from 'jsonata';
import { AppService } from '../app.service';
import { Table } from '../model';
import { Util } from '../util';

export interface DJPagingCursor {
    cursor: string | number;
}

export interface DJDataPaging {
    next: DJPagingCursor;
    prev: DJPagingCursor;
    self?: DJPagingCursor;
    first?: DJPagingCursor;
    last?: DJPagingCursor;
    rangeTruncated?: boolean;
    totalSize?: number;
    totalSizeEstimate?: number;
}

export interface DJDataPage<T> {
    data: T[];
    paging?: DJDataPaging;
}

export interface DJSort {
    field: string;
    order: 'asc' | 'desc' | undefined;
}

export interface DJDataGetOptions {
    pageSize?: number;
    cursor?: DJPagingCursor;
    cursorBefore?: DJPagingCursor;
    sort?: DJSort[];
    // filter?: DJFilter[] -> search!
    includeRelated?: DJKey[];
    /**
     * Optional arguments for the data source:
     * For a query these can be query parameters,
     * for a REST source this can be additional arguments.
     */
    arguments?: any;
}

export type PrimitiveType = number | boolean | string;
export interface DJSchema {
    properties: {
        [key: string]: PrimitiveType;
    };
}

export interface DJSortCapabilities {
    maxSortableFields?: number;
    sortableFields: string[];
}

export interface DJFilterCapabilities {
    maxFilterableFields?: number;
    filterableFields: string[];
}

export interface DJKeyDef {
    fields: string[];
}

export interface DJDataLink {
    /**
     * ID of the linked object.
     */
    id: DJKey;
    /**
     * The primary key the object is linked on
     */
    pk: string;
    /**
     * The foreign key the object links to
     */
    fk: string;
}

export interface DJKeyInfo {
    /** Name */
    name: string;
    /** Primary key of the object */
    value: DJKey;
}

/**
 * Conceptually DJData is comparable to a DB table.
 */
export interface DJDataMeta {

    /**
     * Unique ID of this data set.
     * I.e. 'instance/path/name', like 'dj/northwind/customers'
     */
    dataId: string;

    /**
     * Total size of the data. Might be null (unknown / too expensive to calculate)
     */
    size?: number;

    /**
     * Estimated size of the data. Might be null (unknown / too expensive to calculate)
     */
    sizeEstimate?: number;

    /**
     * The type of a *single* element T.
     * We always return arrays of T, but the schema is not <array of T> but <T>
     */
    schema: Schema;

    primaryKey?: DJKeyDef;

    // links?: DJDataLink[];

    sortCaps?: DJSortCapabilities;

    filterCaps?: DJFilterCapabilities;
}

export type DJKey = any; // number | string | object;

export interface DJData<T> {

    /**
     *  Returns the meta information about the Data source
     */
    getMeta(): Promise<DJDataMeta>;

    /**
     * Iterates over objects from this data source.
     * @param options Data retrieval options
     */
    get(options?: DJDataGetOptions): Promise<DJDataPage<T>>;

    /**
     * Iterates over keys from this data source.
     * @param options Data retrieval options
     */
    keys(options?: DJDataGetOptions): Promise<DJDataPage<DJKeyInfo>>;

    /**
     * Iterates over incoming links to the object.
     * @param key Key of the object
     * @param options Data retrieval options
     */
    incoming(key: DJKey, options?: DJDataGetOptions): Promise<DJDataPage<DJDataLink>>;

    /**
     * CRUD operations
     */
    create(value: T, key?: DJKey): Promise<DJKey>;
    read(key: DJKey): Promise<T>;
    update(key: DJKey, value: T): Promise<boolean>;
    delete(key: DJKey): Promise<boolean>;

    /**
     * If the key can be derived from the value,
     * returns it (i.e. the key is made up of fields in the object,
     * as specified by the DJDataMeta.primaryKey definition).
     * Otherwise returns undefined
     *
     * @param value Object to get the key from
     */
    getKeyFromValue(value: T): DJKey;
}

export class DJDataBase<T> implements DJData<T> {

    constructor(id: string) {
        this.id = id;
    }

    protected id: string;
    protected meta: DJDataMeta;
    protected key: string[];

    paging?: DJDataPaging;

    dataSize: number;
    dataSizeEstimate: number;

    read(key: DJKey): Promise<T> {
        throw new Error('Method not implemented.');
    }
    create(value: T, key?: DJKey): Promise<DJKey> {
        throw new Error('Method not implemented.');
    }
    update(key: DJKey, value: T): Promise<boolean> {
        throw new Error('Method not implemented.');
    }
    delete(key: DJKey): Promise<boolean> {
        throw new Error('Method not implemented.');
    }
    keys(options?: DJDataGetOptions): Promise<DJDataPage<DJKeyInfo>> {
        throw new Error('Method not implemented.');
    }
    incoming(key: DJKey, options?: DJDataGetOptions): Promise<DJDataPage<DJDataLink>> {
        throw new Error('Method not implemented.');
    }

    async getMeta(): Promise<DJDataMeta> {
        if (!this.meta) {
            const page = (await this.get());
            const schema = this.inferSchemaFromData(page.data);
            this.meta = {
                dataId: this.id,
                schema,
                size: page.paging && page.paging.totalSize || null,
                sizeEstimate: page.paging && page.paging.totalSize || null,
                primaryKey: this.key && { fields: this.key } || this.keyDefFromSchema(schema)
            };
        }
        return Promise.resolve(this.meta);
    }

    async get(options?: DJDataGetOptions): Promise<DJDataPage<T>> {
        const res = await this.getInternal(options);
        this.updateStats(res, options);
        return res;
    }

    protected getInternal(options?: DJDataGetOptions): Promise<DJDataPage<T>> {
        throw new Error('Method not implemented.');
    }

    protected updateStats(page: DJDataPage<T>, options?: DJDataGetOptions) {
    }

    size(): number {
        return this.dataSize; // (this.paging && this.paging.totalSize) || this.get().data.length;
    }

    sizeEstimate(): number {
        return this.dataSizeEstimate; // this.size() || this.paging && this.paging.totalSizeEstimate;
    }
    async schema(): Promise<Schema> {
        const data = (await this.get()).data;
        return this.inferSchemaFromData(data);
    }
    inferSchemaFromData(data: T[]) {
        const props = {};
        for (const row of data) {
            for (const key of Object.keys(row)) {
                const val = row[key];
                if (val && !props[key]) {
                    // console.log(key, val);
                    let type;
                    switch (typeof val) {
                        case 'string':
                            type = 'string';
                            break;
                        case 'number':
                            type = 'number';
                            break;
                        case 'boolean':
                            type = 'boolean';
                            break;
                        case 'object':
                            type = 'object';
                            break;
                    }
                    props[key] = { type: type || 'string' };
                }
            }
        }
        const schema: Schema = { properties: props };
        // console.log(schema);
        return schema;
    }

    keyDefFromSchema(schema: Schema): DJKeyDef {
        const keyFields = [];
        for (let i = 0; ; i++) {
            let found = false;
            if (schema.properties) {
                for (const k in schema.properties) {
                    if (schema.properties.hasOwnProperty(k)) {
                        const v = schema.properties[k];
                        if ((v as any).pkpos === i) {
                            keyFields.push(k);
                            found = true; break;
                        }
                    }
                }
            }
            if (!found) { break; }
        }
        const keyDef: DJKeyDef = { fields: keyFields };
        // console.log('keydef', keyDef);
        return keyDef;
    }

    getKeyFromValue(value: T): DJKey {
        const key = [];
        for (const f of this.meta.primaryKey.fields) {
            key.push(value[f]);
        }
        // console.log('keyFromValue', key);
        return key.join('/');
    }
}

export class DJDataDashjoin<T> extends DJDataBase<T> {

    http: HttpClient;
    app: AppService;

    /**
     * remember data so we can use it to compute labels
     */
    data: any;

    constructor(id: string, http: HttpClient, app: AppService) {
        super(id);
        this.http = http;
        this.app = app;
    }

    private getDBAndTable() {
        const parts = this.id.split('/');
        return encodeURIComponent(parts[1]) + '/' + encodeURIComponent(parts[2]);
    }

    // Meta
    // http://localhost:8080/rest/database/crud/config/Table/dj%2Fnorthwind%2FCUSTOMERS
    private getSchemaUri() {
        return '/rest/database/crud/config/Table/' + encodeURIComponent(this.id);
    }

    async getMeta(): Promise<DJDataMeta> {
        if (!this.meta) {
            const parts = Util.parseTableID(this.id);
            const schema = await this.app.getSchema(parts[1], parts[2]).toPromise();
            // const schema = await this.http.get<any>(this.getSchemaUri()).toPromise();
            this.app.log('schema', schema);

            if (false) {
                // Currently not needed
                const related = await this.http.post<any[]>('/rest/database/all/config/Property', { parent: this.id }).toPromise();
                console.log('related', related);
            }

            const page = (await this.get());
            this.meta = {
                dataId: this.id,
                schema, // this.inferSchemaFromData(page.data),
                size: page.paging && page.paging.totalSize || null,
                sizeEstimate: page.paging && page.paging.totalSize || null,
                primaryKey: this.keyDefFromSchema(schema)
            };
            this.computeLabels();
        }
        return Promise.resolve(this.meta);
    }

    private getCrudUri() {
        return '/rest/database/crud/' + this.getDBAndTable();
    }

    // Get
    // http://localhost:8080/rest/database/crud/northwind/CUSTOMERS?offset=0&limit=10
    private getDataUri(offset, limit) {
        return this.getCrudUri() + '?offset=' + offset + '&limit=' + limit;
    }

    protected async getInternal(options?: DJDataGetOptions): Promise<DJDataPage<T>> {
        const offset: number = options && options.cursor && options.cursor.cursor as number || 0;
        const sz = options && options.pageSize || 100;

        const sort = options && options.sort && options.sort[0];
        let uri = this.getDataUri(offset, sz);
        if (sort) { uri += '&sort=' + sort.field + '&descending=' + (sort.order === 'desc'); }

        const data = await this.http.get<any>(uri).toPromise();
        this.app.log('data', data);
        this.data = data;
        this.computeLabels();
        return Promise.resolve({
            data,
            paging: {
                totalSize: null,
                next: { cursor: data.length === sz ? (offset + sz) : null },
                prev: { cursor: offset > 0 ? Math.max(offset - sz, 0) : null },
                rangeTruncated: data.length < sz
            }
        });
    }

    /**
     * called from getMeta() and getInternal()
     * if both are available, make sure to populate the label cache
     */
    private computeLabels() {
        if (true) return;
        if (this.meta && this.data) {
            try {
                const t = this.meta.schema as Table;
                for (const i of this.data) {
                    const pks = [];
                    for (const pk of this.meta.primaryKey?.fields) {
                        pks.push(i[pk]);
                    }
                    this.app.getObjectLabel(Util.parseTableID(t.ID)[1], t.name, pks, i);
                }
            } catch (e) {
                this.app.log('error during label computation', e);
            }
        }
    }

    private getKeysUri() {
        return '/rest/database/keys/' + this.id.substring(3);
    }

    async keys(options?: DJDataGetOptions): Promise<DJDataPage<DJKeyInfo>> {

        let uri = this.getKeysUri() + '?';
        const offset = options && options.cursor && options.cursor.cursor || undefined;
        if (offset) {
            uri += '&prefix=' + offset;
        }

        const sz = options && options.pageSize || undefined;
        if (sz) {
            uri += '&limit=' + sz;
        }

        const sort = options && options.sort && options.sort[0];
        if (sort) { uri += '&sort=' + sort.field + '&descending=' + (sort.order === 'desc'); }

        const data = await this.http.get<DJKeyInfo[]>(uri).toPromise();
        this.app.log('keys', data);
        return Promise.resolve({
            data,
            /* TODO - currently don't have paging support: paging: {
                totalSize: null,
                next: { cursor: data.length == sz ? (offset + sz) : null },
                prev: { cursor: offset > 0 ? Math.max(offset - sz, 0) : null },
                rangeTruncated: data.length < sz
            }*/
        });
    }

    private getIncomingUri(key, offset, limit) {
        return '/rest/database/incoming/' + this.getDBAndTable() + '/' + key + '?offset=' + offset + '&limit=' + limit;
    }

    async incoming(key: DJKey, options?: DJDataGetOptions): Promise<DJDataPage<DJDataLink>> {
        const offset: number = options && options.cursor && options.cursor.cursor as number || 0;
        const sz = options && options.pageSize || 100;

        const sort = options && options.sort && options.sort[0];
        let uri = this.getIncomingUri(key, offset, sz);
        if (sort) { uri += '&sort=' + sort.field + '&descending=' + (sort.order === 'desc'); }

        const data = await this.http.get<DJDataLink[]>(uri).toPromise();
        this.app.log('incoming', data);
        return Promise.resolve({
            data,
            paging: {
                totalSize: null,
                next: { cursor: data.length === sz ? (offset + sz) : null },
                prev: { cursor: offset > 0 ? Math.max(offset - sz, 0) : null },
                rangeTruncated: data.length < sz
            }
        });
    }

    async create(value: T, key?: DJKey): Promise<DJKey> {
        const res = await this.http.put(this.getCrudUri(), value,
            { headers: new HttpHeaders({ 'Content-Type': 'application/json' }), responseType: 'text' }).toPromise();
        this.app.log('create', res);
        return Promise.resolve(res);
    }
    async read(key: DJKey): Promise<T> {
        const res = await this.http.get<T>(this.getCrudUri() + '/' + key).toPromise();
        this.app.log('read', res);
        return res;
    }
    async update(key: DJKey, value: T): Promise<boolean> {
        const res = await this.http.post<any>(this.getCrudUri() + '/' + key, value).toPromise();
        this.app.log('update', res);
        // HTTP 201 created - empty/no result
        return Promise.resolve(true);
    }
    async delete(key: DJKeyDef): Promise<boolean> {
        const res = await this.http.delete<any>(this.getCrudUri() + '/' + key).toPromise();
        this.app.log('delete', res);
        // HTTP 201 created - empty/no result
        return Promise.resolve(true);
    }
}

export class DJDataDashjoinQuery<T> extends DJDataBase<T> {

    http: HttpClient;
    catDbTable: string;

    /**
     * Constructor
     *
     * @param id dj/query/<database>/<queryid>
     */
    constructor(id: string, http: HttpClient) {
        super(id);
        this.http = http;
        const parts = id.split('/');
        // we're in the DB/* category:
        // any change in any table might change the query result
        this.catDbTable = encodeURIComponent(parts[2]) + '/*';
    }

    private getQueryUri(): string {
        const parts = this.id.split('/');
        return '/rest/database/query/' + encodeURIComponent(parts[2]) + '/' + encodeURIComponent(parts[3]);
    }

    protected async getInternal(options?: DJDataGetOptions): Promise<DJDataPage<T>> {
        const args: any = options?.arguments || {};

        // Map the paging args to query arguments 'limit' and 'offset'
        // This might change in the future to out-of-band args
        if (options?.pageSize) {
            // Note: args might be a string (usually JSON object),
            // in this case we get an error here (expected)
            args.limit = options.pageSize;
        }
        if (options?.cursor) {
            args.offset = options.cursor.cursor;
        }

        const data = await this.http.post<any>(this.getQueryUri(), args,
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json',
                    'x-dj-cache': this.catDbTable
                })
            }).toPromise();
        return Promise.resolve({
            data
        });
    }

    private getMetaUri(): string {
        return '/rest/database/queryMeta/' + this.id.substring('dj/query/'.length);
    }

    async getMeta(): Promise<DJDataMeta> {
        if (!this.meta) {
            const props = await this.http.post<any>(this.getMetaUri(), null,
                {
                    headers: new HttpHeaders({
                        'Content-Type': 'application/json',
                        'x-dj-cache': this.catDbTable
                    })
                }).toPromise();

            const schema = { properties: props, type: 'object' } as Schema;

            if (false) {
                // Currently not needed
                const related = await this.http.post<any[]>('/rest/database/all/config/Property', { parent: this.id }).toPromise();
                console.log('related', related);
            }

            this.meta = {
                dataId: this.id,
                schema,
                // size: page.paging && page.paging.totalSize || null,
                // sizeEstimate: page.paging && page.paging.totalSize || null,
                primaryKey: this.keyDefFromSchema(schema)
            };
        }
        return Promise.resolve(this.meta);
    }
}

export class DJDataREST<T> extends DJDataBase<T> {

    http: HttpClient;
    url: string;

    constructor(id: string, http: HttpClient, url: string) {
        super(id);
        this.http = http;
        this.url = url;
    }

    protected async getInternal(options?: DJDataGetOptions): Promise<DJDataPage<T>> {
        const data = await this.http.get<any>(this.url,
            { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }).toPromise();
        return Promise.resolve({
            data
        });
    }
}

export class DJDataConst<T> extends DJDataBase<T> {

    data: T[];

    constructor(data: T[], id?: string, key?: string[], xforms?: string[]) {
        super(id);
        if (xforms) {
            for (const xform of xforms) {
                data = jsonata(xform).evaluate(data);
                // console.log('after xform ' + xform, data[0]);
            }
        }
        this.data = data;
        this.key = key;
    }

    protected getInternal(options?: DJDataGetOptions): Promise<DJDataPage<T>> {
        const offset: number = options && options.cursor && options.cursor.cursor as number || 0;
        const sz = options && options.pageSize || 100;

        const dret = this.data.slice(offset, offset + sz);

        return Promise.resolve({
            data: dret,
            paging: {
                totalSize: this.data.length,
                next: { cursor: offset + sz < this.data.length ? (offset + sz) : null },
                prev: { cursor: offset > 0 ? Math.max(offset - sz, 0) : null },
                rangeTruncated: offset + sz >= this.data.length
            }
        });
    }
}
