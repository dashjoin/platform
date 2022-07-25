import ejs from 'ejs';

/**
 * handles client side expression evaluation
 */
export class Expression {

    /**
     * evaluate simple templates client side
     *
     * @param expression    expression to evaluate
     * @param context       expression context
     * @param def           default value
     */
    static template(expression: string, context: any, def: string) {
        if (!expression) {
            return def;
        } else if (expression.includes('${')) {
            return expression.replace(/\${(.*?)}/g, (g) => {
                return this.traverse(g.substring(2, g.length - 1), context);
            });
        } else if (expression.includes('<%')) {
            // Compile the Embedded JavaScript (EJS) template
            const template = ejs.compile(expression);
            // Evaluate the template with our context
            const html = template(context);
            return html;
        } else {
            return expression;
        }
    }

    /**
     * traverse JSON tree
     * @param res       path to traverse
     * @param context   object to traverse
     */
    static traverse(res: string, context: any) {
        res = res.replace(/"/g, '');
        for (const part of res.split('.')) {
            context = context ? context[part] : undefined;
        }
        return context;
    }
}
