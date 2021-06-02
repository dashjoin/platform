import { Schema } from '@dashjoin/json-schema-form';
import { createCustomElement } from '@angular/elements';
import { DepInjectorService } from '../dep-injector.service';
/**
 * List of registered Widget class types.
 * Note:
 * We used NgModule's ɵmod.declarations, but they are tree shaken in prod build
 */
// import { __DJ_TYPES } from 'src/app/app.module';

/**
 * Widget configuration
 */
export interface WidgetInfo {

    /**
     * Name of the widget.
     * Example: 'Chart Widget'
     */
    name: string;

    /**
     * Category of the widget.
     * Example: 'Dashjoin Display'
     */
    category: string;

    /**
     * Description of the widget.
     * Example: 'Chart widget to display graphs'
     */
    description?: string;

    /**
     * Icon of the widget.
     * Example: '<svg>definition</svg>'
     */
    icon?: string;

    /**
     * HTML tag. This is used to register it as an Angular Element
     *
     * Example: 'dj-chart' can then be used as <dj-chart ...>
     */
    htmlTag?: string;

    /**
     * Schema of the config attributes.
     * Example: JSON schema
     */
    configSchema?: Schema;

    /**
     * list of fields to display when editing this widget (see edit-widget-dialog.ts),
     * if set to null, the widget is not selectable / editable in the widget editor
     * TODO: allow widgets to define new fields
     */
    fields: string[];

    /**
     * true if the widget has a children field with is an array of contained widgets
     */
    isContainer?: boolean;
}

/**
 * Central registry of Widgets
 */
export class WidgetRegistry {

    private constructor() {
        this.widgets = new Map<string, WidgetInfo>();

        // Get list of all declared classes in @NgModule
        const module = DepInjectorService.getModuleType();
        const decl = this.getModuleDeclarations(module);
        for (const w of decl) {
            this.registerWidgetClass(w);
        }
    }

    private static instance: WidgetRegistry;

    widgets: Map<any, WidgetInfo>;

    /**
     * Return the singleton instance
     * @returns Instance
     */
    static getInstance() {
        if (!WidgetRegistry.instance) {
            WidgetRegistry.instance = new WidgetRegistry();
        }
        return WidgetRegistry.instance;
    }

    getWidgetInfoFromClass(w: any): WidgetInfo {
        let wi: WidgetInfo;
        // static __dj_getWidgetInfo() injected by decorator
        try {
            wi = (w as any).__dj_getWidgetInfo() as WidgetInfo;
        } catch { }
        return wi;
    }

    registerWidgetClass(w: any) {
        const wi = this.getWidgetInfoFromClass(w);
        if (!wi) {
            // No decoration
            return;
        }

        this.registerWidget(w, wi);
    }

    registerWidget(w: any, wi: WidgetInfo) {
        // We use the class itself as ID
        const id = w;

        // As the function might be overriden, additionally check the info
        // if (!wi.id)
        //     throw new Error('WidgetInfo has no ID');
        if (this.widgets.get(id)) {
            throw new Error('Duplicate ID ' + id);
        }

        this.widgets.set(id, wi);

        if (wi.htmlTag) {
            const injector = DepInjectorService.getInjector();
            customElements.define(wi.htmlTag, createCustomElement(w, { injector }));
        }
    }

    /**
     * Returns widget info for the given class
     * @param id Class ID
     * @returns WidgetInfo
     */
    getWidgetInfo(id: any): WidgetInfo {
        return this.widgets.get(id);
    }

    /**
     * Returns all registered widget classes
     * @returns Array of classes
     */
    getWidgets(): any[] {
        return Array.from(this.widgets.keys());
    }

    /**
     * Returns all infos of registered widgets
     * @returns Array of infos
     */
    getWidgetInfos(): WidgetInfo[] {
        return Array.from(this.widgets.values());
    }

    /**
     * lookup widget info by name
     */
    getWidgetInfoFromName(name: string): WidgetInfo {
        for (const wi of this.widgets.values()) {
            if (wi.name === name) {
                return wi;
            }
        }
        return null;
    }

    /**
     * Get module declarations
     */
    getModuleDeclarations(module: any): [] {
        const res = (window as any).__DJ_TYPES;
        // console.log('DJ_TYPES', res);
        return res;
    }
}

/**
 * Class decorator that specifies this is a Widget
 *
 * @param info WidgetInfo that configures the widget
 */
export function DashjoinWidget(info: WidgetInfo): ClassDecorator {

    return (constructor: any) => {
        // Inject static __dj_getWidgetInfo()
        constructor.__dj_getWidgetInfo = () => {
            return info;
        };
    };
}
