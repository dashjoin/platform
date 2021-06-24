import { Component, EventEmitter, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { WidgetComponent, Schema } from '@dashjoin/json-schema-form';

/**
 * Widget list
 */
@Component({
    templateUrl: 'widgetlist.component.html',
    styleUrls: ['widgetlist.component.css'],
    encapsulation: ViewEncapsulation.None,
})
export class WidgetListComponent implements WidgetComponent, OnInit, OnDestroy {

    /**
     * see WidgetComponent
     */
    label: string;

    /**
     * see WidgetComponent
     */
    value = '';

    /**
     * see WidgetComponent
     */
    rootValue = '';

    /**
     * see WidgetComponent
     */
    schema: Schema;

    /**
     * see WidgetComponent
     */
    rootSchema: Schema;

    /**
     * emit changes done by the user in the component
     */
    valueChange: EventEmitter<string> = new EventEmitter();

    /**
     * emit validation errors
     */
    errorChange: EventEmitter<string> = new EventEmitter();

    items = [];

    /**
     * init editor
     */
    ngOnInit() {
        this.items = this.schema.enum.
            map(e => { return { id: e, text: this.display(e), icon: this.icon(e) }; }).
            sort((a, b) => { return a.text.localeCompare(b.text); });
    }

    /**
     * cleanup
     */
    ngOnDestroy(): void {
    }

    item(name: string) {
        for (const i of this.items)
            if (i.id === name)
                return i;
        return undefined;
    }

    // tslint:disable
    // Replace display texts for some widgets -> feed from metadata
    texts = {
        'editRelated': 'Edit Related',
        'dj-sidenav': 'Side Navigation',
        'dj-table-metadata': 'Table Metadata'
    };
    // tslint:enable

    display(item) {
        return this.texts[item] || item.substring(0, 1).toUpperCase() + item.substring(1);
    }

    // tslint:disable
    // Map some nice icons for widgets -> feed from metadata
    icons = {
        'button': 'smart_button', //
        'card': 'badge', //
        'chart': 'insert_chart', //
        'container': 'table_chart', //
        'create': 'add_circle', //
        'display': 'list', //
        'dj-sidenav': 'menu_open', //
        'dj-table-metadata': 'widgets', //
        'edit': 'edit', //
        'editRelated': 'share', //
        'expansion': 'expand', //
        'icon': 'insert_emoticon', //
        'links': 'links', //
        'markdown': 'text_snippet', //
        'table': 'table', //
        'text': 'title', //
        'tree': 'account_tree', //
        'variable': 'plus_one' //
    };
    // tslint:enable

    icon(item: string) {
        return this.icons[item] || 'dashboard_customize';
    }

    /**
     * emit value change and error state
     */
    onChange(evt) {
        this.value = evt.value;
        this.valueChange.emit(this.value);
    }
}
