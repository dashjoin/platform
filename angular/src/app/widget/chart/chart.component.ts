import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Schema } from '@dashjoin/json-schema-form';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';
import { baseColors } from 'ng2-charts';
import 'chartjs-adapter-date-fns';
import { Util } from '../../util';
import { Property } from '../../model';

/**
 * chart (Pie, Line, or Bar charts)
 */
@DashjoinWidget({
  name: 'chart',
  category: 'Default',
  description: 'Component that draws a chart (Pie, Line, or Bar charts)',
  htmlTag: 'dj-chart',
  fields: ['title', 'database', 'query', 'arguments', 'chart', 'style', 'graph', 'expression']
})
@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent extends DJBaseComponent implements OnInit {

  /**
   * like columns, but dj-label
   */
  colLabels: string[] = [];

  /**
 * is this a multi dimensional chart
 */
  multiDim = false;

  /**
   * see https://www.chartjs.org/docs/2.9.4/configuration/
   */
  chartoptions: any = {}

  /**
   * parses style key/value to config object
   */
  setOptions() {
    for (let [key, value] of Object.entries(this.layout.style)) {
      let ctx = this.chartoptions;
      const arr = key.split('.');
      const last = arr.pop();
      for (const k of arr) {
        if (!ctx[k]) ctx[k] = {};
        ctx = ctx[k];
      }
      if (value === 'true')
        value = true;
      if (value === 'false')
        value = false;
      if (!isNaN(Number(value)))
        value = Number(value);
      ctx[last] = value;
    }
  }

  /**
   * get data and generate chart data
   */
  async initWidget() {

    while (baseColors.length < 100) {
      baseColors.push(...baseColors)
    }

    if (this.layout.style)
      this.setOptions();
    try {
      await this.page({ pageIndex: 0, pageSize: 50, length: null });
      await this.prepareDataForChart();
    } catch (e) {
      this.errorHandler(e);
    }
  }

  /**
   * generate data / label objects for chart
   */
  async prepareDataForChart() {

    // this.all = [{ name: 'bus', count: 3, other: 6 }, { name: 'car', count: 2, other: 5 }, { name: null, count: 4 }]

    const types = this.checkColumns(this.all);
    if (!(Object.values(types)[0] === 'string' && Object.values(types)[1] !== 'string') && Object.values(types).length > 2) {
      this.prepareDataForMultiDimChart();
      return;
    }

    // treat the first column as the label
    // all other columns are treate as series - this only makes sense for queries like
    // select type, min(weight), max(weight) group by type
    const res = this.all;
    this.columns = [];
    this.colLabels = [];
    this.all = [];

    const cols = {};
    let first = null;
    for (const row of res) {
      for (const key of Object.keys(row)) {
        if (!cols[key]) {
          cols[key] = [];
          if (!first) {
            first = key;
          }
        }
      }
    }
    for (const row of res) {
      for (const [k, v] of Object.entries(row)) {
        cols[k].push(v);
      }
    }
    this.columns = cols[first];

    // lazy get metadata (only required in single dim case, since dj-label / clicks are not supported otherwise)
    if (!this.meta) {
      this.meta = await this.getData().getMeta();
    }

    await this.setColumnLabels();
    this.all = [];

    delete cols[first];
    for (const [k, v] of Object.entries(cols)) {
      this.all.push({ data: v, label: k });
    }
  }

  /**
   * interpret the first col as the series, the second as the labels
   */
  prepareDataForMultiDimChart() {
    this.multiDim = true;
    const types = this.checkColumns(this.all);
    const series = {};
    const labels = [];

    // assume three columns
    const first = Object.keys(types)[0];
    const second = Object.keys(types)[1];
    const data = Object.keys(types)[2];

    // get all labels and prepare series object with empty child objects
    for (const row of this.all) {
      const serie = row[first];
      const label = row[second];
      if (!labels.includes(label)) {
        labels.push(label);
      }
      if (!series[serie]) {
        series[serie] = {};
      }
    }

    // copy data into new structure
    for (const row of this.all) {
      const serie = row[first];
      const label = row[second];
      series[serie][label] = row[data];
    }

    // missing values are null
    for (const row of Object.values(series)) {
      for (const label of labels) {
        if (!row[label]) {
          row[label] = null;
        }
      }
    }

    // generate structure
    this.columns = labels;
    this.colLabels = labels;
    this.all = [];
    for (const [k, v] of Object.entries(series)) {
      // make sure d has the order of labels
      const d = [];
      for (const label of labels) {
        d.push(v[label]);
      }
      this.all.push({ data: d, label: k, stack: 'a' });
    }
  }

  /**
   * check the column datatypes
   */
  checkColumns(table: object[]) {
    const res = {};
    for (const row of table) {
      for (const [k, v] of Object.entries(row)) {
        if (res[k]) {
          if (typeof (v) === 'string') {
            res[k] = 'string';
          }
        } else {
          res[k] = typeof (v);
        }
      }
    }
    return res;
  }

  /**
   * chart click handler
   */
  async chartClicked(event) {
    // navigation not supported for multi dim charts
    if (this.multiDim)
      return;

    if (event.active[0] === undefined)
      return;

    const idarr = this.getIdArr(event.active[0].index);
    if (idarr)
      this.router.navigate(idarr);
  }

  /**
   * given this.columns, looks up the labels and writen them to this.colLabels
   */
  async setColumnLabels() {
    let idx = 0;
    for (const c of this.columns) {
      const idarr = this.getIdArr(idx);
      idx++;
      // is this column linkable?
      if (idarr) {
        const l = await this.labelId(idarr).toPromise();
        this.colLabels.push(l);
      } else {
        this.colLabels.push(c);
      }
    }
  }

  /**
   * checks if one of the columns contains a key, 
   * if so, returns a link to the record using the columnIndex-th label as the key value
   * undefined otherwise
   */
  getIdArr(columnIndex: number): string[] {
    let count = 0;
    let pk: Property;
    for (const p of Object.values(this.meta.schema.properties)) {
      const t = p as Property;
      if (t.ref) {
        const idarr = Util.parseColumnID(t.ref as string);
        idarr[0] = 'resource';
        idarr[3] = this.columns[columnIndex];
        if (idarr[3]) {
          return idarr;
        }
      }
      if (t.pkpos != null && t.pkpos >= 0) {
        pk = t;
        count++;
      }
    }
    if (pk && count === 1) {
      const idarr = Util.parseColumnID(pk.ID as string);
      idarr[0] = 'resource';
      idarr[3] = this.columns[columnIndex];
      if (idarr[3]) {
        return idarr;
      }
    }
  }
}
