import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import jsonata from 'jsonata';
import { AppService } from './app.service';
import { DJData, DJDataConst, DJDataDashjoin, DJDataDashjoinQuery, DJDataJson, DJDataREST, DJWrappedData } from './djbase/data';
import sampleData from './djbase/data.json';

@Injectable({
  providedIn: 'root'
})
export class DJRuntimeService {

  localStore: any = window;

  containerData: string;

  currentData: any;

  constructor(private http: HttpClient, private app: AppService) { }

  nameToKey(name) {
    return name.replace(':', '_');
  }

  setData(name, data) {
    if (name.startsWith('/'))
      this.setLocalData(name, data);
    else {
      console.log('setData on non-local not allowed');
    }
  }

  setLocalData(name, data) {
    this.localStore[this.nameToKey(name)] = data;
    console.log('setData ' + name + ' = ' + data);
  }

  setContainerData(name) {
    this.containerData = name;
  }

  getContainerData() { return this.containerData; }

  getData(name, ctx?): DJData<any> {
    if (name.startsWith('/'))
      return new DJDataConst<any>([this.getLocalData(name)]);

    if (true) {
      // only DJDataDashjoin supports pushdown, wrap the other sources for paging and sorting
      if (name.startsWith('dj/query/'))
        return new DJWrappedData(new DJDataDashjoinQuery<any>(name, this.http, false));

      if (name.startsWith('dj/queryGraph/'))
        return new DJWrappedData(new DJDataDashjoinQuery<any>(name, this.http, true));

      if (name.startsWith('dj/search/'))
        return new DJWrappedData(new DJDataREST(name, this.http, '/rest/database/search/' + name.substring('dj/search/'.length) + '?limit=100'))

      if (name.startsWith('dj/expression/'))
        return new DJWrappedData(new DJDataJson(ctx));

      return new DJDataDashjoin<any>(name, this.http, this.app);
    }

    const p = name.split('/');
    // console.log(p);
    // like dj/database/name [/55]
    if (p.length === 3) {
      return new DJDataConst<any>(sampleData, 'dj/local/countries', ['Country', 'Capital'],
        [
          '$.{"Country":name, "Capital":capital, "Domain":topLevelDomain, "Phone":callingCodes, "Area":area, "Borders": borders}'
        ],
      );
    }
    if (p.length === 4) {
      // console.log('get idx ' + p[3]);
      return new DJDataConst<any>([sampleData[p[3] as number]]);
    }
    return undefined;
  }
  getLocalData(name) {
    const res = this.localStore[this.nameToKey(name)];
    console.log('getData ' + name + ' = ' + res);
    return res;
  }

  jsonata(expr, data?) {
    if (data == null)
      data = this.getCurrentData();
    try {
      return data ? jsonata(expr).evaluate(data) : expr;
    } catch (error) {
      console.log('Warning: jsonata error', error, expr);
      return expr;
    }
  }

  getCurrentData() {
    return this.currentData;
    //    return this.getData('__current__');
  }

  setCurrentData(data) {
    this.currentData = data;
    //    this.setData('__current__', data);
  }
}
