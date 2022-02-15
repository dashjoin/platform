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

  async initWidget() {
    const displayData = await this.evaluateExpression(this.layout.display);
    const res: any = await this.http.get('https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(displayData) + '&format=json&limit=1').
      toPromise();

    this.map = new Map({
      target: 'map_widget',
      layers: [
        new TileLayer({
          source: new OSM()
        })
      ],
      view: new View({
        center: olProj.fromLonLat([res[0].lon, res[0].lat]),
        zoom: 8
      })
    });
  }
}
