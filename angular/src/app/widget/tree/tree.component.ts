import { CollectionViewer } from '@angular/cdk/collections';
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { DashjoinWidget } from '../widget-registry';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { LinksComponent } from '../links/links.component';
import { NavigationEnd, Router } from '@angular/router';
import { Util } from '../../util';

/**
 * tree node interface
 */
interface TreeNode {

  /**
   * primary key of the current node
   */
  name: any;

  /**
   * children - if null, no load has been performed yet
   */
  children?: TreeNode[];

  /**
   * optional link
   */
  href?: string[];

  /**
   * node highlighted
   */
  highlight?: boolean;
}

/**
 * subclass of MatTreeNestedDataSource that allows us to intercept expansion events
 */
export class MyDataSource extends MatTreeNestedDataSource<TreeNode> {

  /**
   * constructor
   *
   * @param treeControl tree controller we subscribe to
   * @param tree  main component (so we can trigger reload)
   */
  constructor(public treeControl: NestedTreeControl<TreeNode>, public tree: TreeComponent) {
    super();
  }

  /**
   * intercept expansion events
   */
  connect(collectionViewer: CollectionViewer): Observable<TreeNode[]> {
    this.treeControl.expansionModel.changed.subscribe(change => {
      if (change.added?.length > 0 && !change.added[0].children) {
        change.added[0].children = [];

        const getOptions = { arguments: new Map<string, any>(['node', change.added[0].name]) };
        this.tree.getData().get(getOptions).then(res => {
          for (const row of res.data) {
            let vv = null;
            for (const v of Object.values(row)) {
              vv = v;
            }
            if (typeof vv === 'object') {
              change.added[0].children.push(vv);
            } else {
              change.added[0].children.push({ name: vv });
            }
          }
          const data = this.tree.dataSource.data;
          this.tree.dataSource.data = null;
          this.tree.dataSource.data = data;
        });
      }
    });

    return super.connect(collectionViewer);
  }
}

/**
 * table widget
 */
@DashjoinWidget({
  name: 'tree',
  category: 'Default',
  description: 'Component that displays a tree',
  htmlTag: 'dj-tree',
  fields: ['title', 'database', 'query', 'arguments']
})
@Component({
  selector: 'app-tree',
  templateUrl: './tree.component.html',
  styleUrls: ['./tree.component.css']
})
export class TreeComponent extends LinksComponent {

  /**
   * tree controller
   */
  treeControl = new NestedTreeControl<TreeNode>(node => node.children);

  /**
   * data source (simple object tree that intercepts expansion events for dyn loading)
   */
  dataSource = new MyDataSource(this.treeControl, this);

  /**
   * result column name
   */
  column: string;

  /**
   * init data source with the current element and prepare the query metadata
   */
  async initWidget() {
    const res = (await this.getData().getMeta()).schema as any;
    this.queryMeta = { ID: null, name: null, parent: null, type: null, properties: res };
    this.column = Object.keys(res)[0];
    const root = [];

    const dres = (await this.getData().get()).data;
    for (const row of dres) {
      let vv = null;
      for (const v of Object.values(row)) {
        vv = v;
      }
      if (typeof vv === 'object') {
        root.push(vv);
      } else {
        root.push({ name: vv });
      }
    }
    const expand = this.checkHighlights(null, root);
    this.dataSource.data = root;
    if (expand) {
      this.treeControl.expand(expand);
    }

    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        this.init();
        const expand2 = this.checkHighlights(null, root);
        if (expand2) {
          this.treeControl.expand(expand2);
        }
      }
    });
  }

  /**
   * generate the href[] for the current page
   */
  getHref(): string[] {
    if (!this.table && !this.pk1) {
      return ['/page', this.database];
    } else if (this.database === 'config' && this.table === 'Table') {
      return ['/table', Util.parseTableID(this.pk1)[1], Util.parseTableID(this.pk1)[2]];
    } else {
      return ['/resource', this.database, this.table, this.pk1];
    }
  }

  /**
   * check array equality
   */
  arrEquals(a, b) {
    if (a.length === 3 && b.length === 4 && a[0] === '/table' && b[0] === '/resource') {
      // for instance, hightlight containing table
      if (a[1] === 'config' && a[2] === 'dj-database') {
        // unless it is a DB
        return false;
      }
      return a[1] === b[1] && a[2] === b[2];
    }
    return a.length === b.length && a.every((v, i) => v === b[i]);
  }

  /**
   * check whether the result contains a node with href of the current page
   */
  checkHighlights(parent: TreeNode, res: TreeNode[]): TreeNode {
    for (const node of res) {
      node.highlight = false;
    }
    for (const node of res) {
      if (node.href && this.arrEquals(node.href, this.getHref())) {
        node.highlight = true;
        return parent;
      }
      if (node.children) {
        const expand = this.checkHighlights(node, node.children);
        if (expand) {
          return expand;
        }
      }
    }
  }

  /**
   * callback to determine whether node is expandable (not loaded yet or has kids)
   */
  hasChild = (_: number, node: TreeNode) => !node.children || node.children.length > 0;

  /**
   * wrap the node name into a fake object that matches the table metadata
   */
  element(node: TreeNode) {
    const res = {};
    res[this.column] = node.name;
    return res;
  }
}
