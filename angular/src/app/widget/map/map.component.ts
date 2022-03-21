import { Component, OnInit } from '@angular/core';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { DashjoinWidget } from '../widget-registry';
import Map from 'ol/Map';
import View from 'ol/View';
import VectorLayer from 'ol/layer/Vector';
import Style from 'ol/style/Style';
import Icon from 'ol/style/Icon';
import OSM from 'ol/source/OSM';
import * as olProj from 'ol/proj';
import TileLayer from 'ol/layer/Tile';
import VectorSource from 'ol/source/Vector';
import Feature from 'ol/Feature';
import Point from 'ol/geom/Point';

/**
 * displays a text with optional link and icon
 */
@DashjoinWidget({
  name: 'map',
  category: 'Default',
  description: 'Component that displays a map',
  htmlTag: 'dj-map',
  fields: ['title', 'display', 'style']
})
@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent extends DJBaseComponent implements OnInit {

  /**
   * map handle
   */
  map: Map;

  error: string;

  style: any;

  countries = {
    "CN": "China",
    "FR": "France",
    "IT": "Italy",
    "PL": "Poland",
    "GR": "Greece",
    "RU": "Russia",
    "DE": "Germany"
  };

  async initWidget() {

    this.style = this.layout.style ? JSON.parse(JSON.stringify(this.layout.style)) : {};
    if (!this.style.width) this.style.width = '400px';
    if (!this.style.height) this.style.height = '400px';

    let displayData: any = await this.evaluateExpression(this.layout.display);

    // convert single value to [single]
    if (!Array.isArray(displayData)) {
      displayData = [displayData];
    }

    // map points
    const features = [];

    let error = "";

    // min / max bounding box, used to compute the center
    let min = 9999;
    let max = -9999;
    let min2 = 9999;
    let max2 = -9999;

    for (let dd of displayData) {
      if (this.countries[dd]) {
        dd = this.countries[dd];
      }

      let res: any;

      res = await this.http.get('https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(dd) + '&format=json&limit=1').
        toPromise();

      if (res[0]) {
        features.push(
          new Feature({
            geometry: new Point(olProj.fromLonLat([res[0].lon, res[0].lat]))
          })
        )

        max = Math.max(max, res[0]?.boundingbox[1]);
        min = Math.min(min, res[0]?.boundingbox[0]);
        max2 = Math.max(max2, res[0]?.boundingbox[3]);
        min2 = Math.min(min2, res[0]?.boundingbox[2]);
      } else {
        error = 'Cannot resolve address: ' + dd;
      }
    }

    const delta = max - min;

    let zoom = 10;
    if (delta > 1) zoom = 4;
    if (delta > 10) zoom = 1;
    if (delta < 0.1) zoom = 16;

    if (!error || features.length > 0) {
      this.map = new Map({
        target: 'map_widget',
        layers: [
          new TileLayer({
            source: new OSM()
          }),
          new VectorLayer({
            source: new VectorSource({
              features
            })
          }
          )
        ],
        view: new View({
          center: olProj.fromLonLat([(min2 + max2) / 2, (min + max) / 2]),
          zoom
        })
      });
    } else {
      this.error = error;
    }
  }
}
