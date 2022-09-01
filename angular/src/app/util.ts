/**
 * collection of helper classes
 */
export class Util {

    static localName(s: string): string {
        if (s && typeof (s) === 'string') {
            if (s.startsWith('http://')) {
                while (s.endsWith('#') || s.endsWith('/')) {
                    s = s.substring(0, s.length - 1);
                }
                if (s.indexOf('#') >= 0) return s.split('#').pop();
                if (s.indexOf('/') >= 0) return s.split('/').pop();
            }
        }
        return s;
    }

    /**
     * in the config DB, table and column IDs are concatenated: dj/database/table/column. Therefore,
     * this method URLEncodes / and %
     */
    static encodeTableOrColumnName(s: string): string {
        return (s as any).replaceAll('%', '%25').replaceAll('/', '%2F');
    }

    /**
     * decode version of above
     */
    static decodeTableOrColumnName(s: string): string {
        return (s as any).replaceAll('%2F', '/').replaceAll('%25', '%');
    }

    /**
     * Column IDs in the config database look like dj/northwind/EMP/LAST_NAME
     * replaces id.split('/') since the table name might have / in it
     */
    static parseColumnID(id: string) {
        if (id.split('/').length === 4) {
            const res = id.split('/');
            res[2] = Util.decodeTableOrColumnName(res[2]);
            res[3] = Util.decodeTableOrColumnName(res[3]);
            return res;
        }
        throw new Error('illegal column id: ' + id);
    }

    /**
     * Table IDs in the config database look like dj/northwind/EMP
     * replaces id.split('/') since the table name might have / in it
     */
    static parseTableID(id: string): string[] {
        if (id.split('/').length === 3) {
            const res = id.split('/');
            res[2] = Util.decodeTableOrColumnName(res[2]);
            return res;
        }
        if (id.split('/')[2] === 'http:') {
            const first = id.indexOf('/');
            const second = id.indexOf('/', first + 1);
            return [id.substring(0, first), id.substring(first + 1, second), id.substring(second + 1)];
        }
        throw new Error('illegal table id: ' + id);
    }

    /**
     * Database IDs in the config database look like dj/northwind
     * replaces id.split('/') since the table name might have / in it.
     * In the quey editor, the database ID might be initialized as only 'dj' which is legal
     */
    static parseDatabaseID(id: string): string[] {
        if (id.split('/').length === 2 || id.split('/').length === 1) {
            return id.split('/');
        }
        throw new Error('illegal database id: ' + id);
    }

    /**
     * there might be cases where a par can be one or the other
     */
    static parseTableOrColumnID(id: string): string[] {
        try {
            return this.parseTableID(id);
        } catch (e) {
            return this.parseColumnID(id);
        }
    }

    /**
     * format / limit text for snack bar
     */
    static limitTextForSnackBar(s: string): string {
        if (!s) {
            return s;
        }
        if (s.includes('\nIf the above error is not helpful, you may want to try EJS-Lint:')) {
            s = s.split('\n')[0];
        }
        const maxCharsPerLine = 40;
        let res = '';
        for (const line of s.split(/\s+/)) {
            for (let i = 0; i < Math.ceil(line.length / maxCharsPerLine); i++) {
                res = res + line.substring(i * maxCharsPerLine, maxCharsPerLine) + ' ';
            }
        }
        return res.substring(0, 400);
    }

    /**
     * log error, filter out the right message and return it
     */
    static errorMsg(error: any) {
        console.error(error);
        let msg = '' + error;
        if (typeof error.error === 'string') {
            msg = error.error;
        } else if (error.error instanceof ProgressEvent) {
            msg = 'Server timeout';
        } else if (error.status === 404) {
            msg = 'Not Found';
        } else if (error.statusText) {
            msg = error.statusText;
        } else if (error.details) {
            msg = error.details;
        }

        // If we got a HTML page, show the title as error msg
        if (msg.indexOf('<title>') > 0) {
            const i1 = msg.indexOf('<title>');
            const i2 = msg.indexOf('</title>');
            msg = msg.substring(i1 + '<title>'.length, i2);
        }

        if (msg === 'Unknown database: dj') {
            msg = 'No database is defined to run the query on. Select a database on the main form or select an intial table in this popup.'
        }

        return Util.limitTextForSnackBar(msg);
    }

    /**
     * we use json schema for both table metadata and form layout. This method
     * strips any table metadata fields before the schema is saved in custom
     * form information for edit / create widgets
     */
    static cleanJsonSchema(schema: any): any {
        delete schema.name;
        delete schema.ID;
        delete schema.parent;
        delete schema.tableLayout;
        delete schema.instanceLayout;
        if (schema.properties) {
            for (const prop of Object.values(schema.properties)) {
                const p: any = prop;

                // delete some unused table / column metadata
                // ref / parent is important for foreign key autocomplete lookup
                // createOnly gets "translated" to readOnly
                delete p.name;
                delete p.ID;
                delete p.dbType;
                delete p.pkpos;
                delete p.parent;
                if (p.createOnly) {
                    delete p.createOnly;
                    p.readOnly = true;
                }
            }
        }
        return schema;
    }

    /**
     * routing always encodes parameters. This method allows to convert a URL like /resource/a/b%2fc
     * to be passed to router.navigate like this router.navigate(url2array(url)).
     * The url is converted to ['/resource', 'a', 'b/c']
     */
    static url2array(url: string) {
        const absolute = url.startsWith('/');
        if (absolute) {
            url = url.substring(1);
        }
        const parts = url.split('/');
        if (absolute) {
            parts[0] = '/' + parts[0];
        }
        return parts.map(s => decodeURIComponent(s));
    }
}
