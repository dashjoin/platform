import ejs from 'ejs';
import jsonata from 'jsonata';

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
        } else if (expression.includes('<%') && expression.includes('%>')) {
            try {
                // Compile the Embedded JavaScript (EJS) template
                const template = ejs.compile(expression);
                // Evaluate the template with our context
                const html = template(context);
                return html;
            }
            catch (evalError) {
                return expression;
            }
        } else if (expression.includes('${')) {
            return expression.replace(/\${(.*?)}/g, (g) => {
                try {
                    return jsonata(g.substring(2, g.length - 1)).evaluate(context);
                }
                catch (evalError) {
                    return g;
                }
            });
        } else {
            return expression;
        }
    }
}
