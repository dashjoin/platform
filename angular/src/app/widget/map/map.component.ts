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
  fields: ['display', 'style']
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
    "FR": "France",
    "DE": "Germany"
  }

  async initWidget() {

    this.style = this.layout.style ? JSON.parse(JSON.stringify(this.layout.style)) : {};
    if (!this.style.width) this.style.width = '400px';
    if (!this.style.height) this.style.height = '400px';

    let displayData = await this.evaluateExpression(this.layout.display);

    if (Array.isArray(displayData)) {
      displayData = displayData[0];
    }

    if (this.countries[displayData]) {
      displayData = this.countries[displayData];
    }

    const res: any = await this.http.get('https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(displayData) + '&format=json&limit=1').
      toPromise();

    const delta = res[0]?.boundingbox[1] - res[0]?.boundingbox[0];

    let zoom = 10;
    if (delta > 1) zoom = 4;
    if (delta < 0.1) zoom = 16;

    if (res[0]?.lon && res[0]?.lat) {
      this.map = new Map({
        target: 'map_widget',
        layers: [
          new TileLayer({
            source: new OSM()
          }),
          new VectorLayer({
            source: new VectorSource({
              features: [
                new Feature({
                  geometry: new Point(olProj.fromLonLat([res[0].lon, res[0].lat]))
                })
              ],
            })
          }
          )
        ],
        view: new View({
          center: olProj.fromLonLat([res[0].lon, res[0].lat]),
          zoom
        })
      });
    } else {
      this.error = 'Cannot resolve address: ' + displayData;
    }
  }
}
