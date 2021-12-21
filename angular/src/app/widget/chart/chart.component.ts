import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Schema } from '@dashjoin/json-schema-form';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * chart (Pie, Line, or Bar charts)
 */
@DashjoinWidget({
  name: 'chart',
  category: 'Default',
  description: 'Component that draws a chart (Pie, Line, or Bar charts)',
  htmlTag: 'dj-chart',
  fields: ['title', 'database', 'query', 'arguments', 'chart', 'style']
})
@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent extends DJBaseComponent implements OnInit {

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
        if (k === 'xAxes') {
          if (!ctx.xAxes) ctx.xAxes = [{}];
          ctx = ctx.xAxes[0];
        } else if (k === 'yAxes') {
          if (!ctx.yAxes) ctx.yAxes = [{}];
          ctx = ctx.yAxes[0];
        } else {
          if (!ctx[k]) ctx[k] = {};
          ctx = ctx[k];
        }
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
    this.setOptions();
    try {
      await this.page({ pageIndex: 0, pageSize: 50, length: null });
      this.prepareDataForChart();
    } catch (e) {
      this.errorHandler(e);
    }
  }

  /**
   * generate data / label objects for chart
   */
  prepareDataForChart() {

    // this.all = [{ name: 'bus', count: 3, other: 6 }, { name: 'car', count: 2, other: 5 }, { name: null, count: 4 }]

    const types = this.checkColumns(this.all);
    if (!(Object.values(types)[0] === 'string' && Object.values(types)[1] !== 'string')) {
      this.prepareDataForMultiDimChart();
      return;
    }

    // treat the first column as the label
    // all other columns are treate as series - this only makes sense for queries like
    // select type, min(weight), max(weight) group by type
    const res = this.all;
    this.columns = [];
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
}
