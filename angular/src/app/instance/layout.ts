import { Widget } from './widget';

/**
 * layouts divide widgets into multiple lists however the overall order must be retained
 * for the layout editor. This structure associates the widget to the overall index
 * (since list index is off for all lists other than the first one)
 */
export class WidgetPos {
    /**
     * constructor
     * @param widget   the widget to display
     * @param pos      overall widget position on the page
     */
    constructor(public widget: Widget, public pos: number) { }
}

/**
 * layout computation
 */
export class Layout {

    /**
     * split the widgets into a list that is on the top with 100% width followed by n-columns
     * @param pageLayout    layout name
     * @param children      widgets to lay out
     */
    static compute(
        pageLayout: '1 column' | '2 column' | '3 column' | 'T 2 column' | 'T 3 column' | 'horizontal' | 'grid',
        children: Widget[]
    ): [WidgetPos[], WidgetPos[][]] {
        let rowKids: WidgetPos[];
        let colKids: WidgetPos[][];
        if (!pageLayout || pageLayout === 'horizontal') {
            rowKids = [];
            let index = 0;
            for (const i of children) {
                rowKids.push(new WidgetPos(i, index));
                index++;
            }
        }
        if (pageLayout === '1 column') {
            colKids = [[]];
            let index = 0;
            for (const i of children) {
                colKids[0].push(new WidgetPos(i, index));
                index++;
            }
        }
        if (pageLayout === '2 column') {
            colKids = [[], []];
            let index = 0;
            for (const i of children) {
                colKids[Math.floor(index * 2 / children.length)].push(new WidgetPos(i, index));
                index++;
            }
        }
        if (pageLayout === '3 column') {
            colKids = [[], [], []];
            let index = 0;
            for (const i of children) {
                colKids[Math.floor(index * 3 / children.length)].push(new WidgetPos(i, index));
                index++;
            }
        }
        if (pageLayout === 'T 2 column') {
            if (children.length === 0) {
                // nothing to display
            } else if (children.length === 1) {
                rowKids = [new WidgetPos(children[0], 0)];
            } else {
                rowKids = [new WidgetPos(children[0], 0)];
                colKids = [[], []];
                let index = 0;
                for (const i of children.slice(1)) {
                    colKids[Math.floor(index * 2 / children.slice(1).length)].push(new WidgetPos(i, index + 1));
                    index++;
                }
            }
        }
        if (pageLayout === 'T 3 column') {
            if (children.length === 0) {
                // nothing to display
            } else if (children.length === 1) {
                rowKids = [new WidgetPos(children[0], 0)];
            } else {
                rowKids = [new WidgetPos(children[0], 0)];
                colKids = [[], [], []];
                let index = 0;
                for (const i of children.slice(1)) {
                    colKids[Math.floor(index * 3 / children.slice(1).length)].push(new WidgetPos(i, index + 1));
                    index++;
                }
            }
        }
        return [rowKids, colKids];
    }
}
