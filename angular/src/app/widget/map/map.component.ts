import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
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

  /**
   * Ref to the map element
   */
  @ViewChild('map_widget') mapDiv: ElementRef;

  countries = {
    "CN": "China",
    "FR": "France",
    "IT": "Italy",
    "PL": "Poland",
    "GR": "Greece",
    "RU": "Russia",
    "DE": "Germany",
    NL: "Netherlands",
    IN: "India",
    BR: "Brazil",
    GE: "Georgia",
    KE: "Kenya",
    JP: "Japan",
    KR: "South Korea",
    SG: "Singapore",
    GB: "United Kingdom",
    UK: "United Kingdom",
    KH: "Cambodia",
    CO: "Colombia",
    US: "United States",
    USA: "United States",
    ID: "Indonesia",
    VN: "Vietnam",
    AR: "Argentina",
    TW: "Taiwan",
    IE: "Ireland",
    AU: "Australia",
    TN: "Tunisia",
    VE: "Venezuela",
    CY: "Cyprus",
    TR: "Turkey",
    TH: "Thailand",
    BA: "Bosnia and Herzegovina",
    BG: "Bulgaria",
    AT: "Austria",
    ZA: "South Africa",
    PK: "Pakistan",
    PS: "Palestine, State of",
    UA: "Ukraine",
    HK: "Hong Kong, China",
    ES: "Spain",
    PR: "Puerto Rico",
    BE: "Belgium",
    AE: "United Arab Emirates",
    CH: "Switzerland",
    CL: "Chile",
    MX: "Mexico",
    QA: "Qatar",
    CM: "Cameroon",
    SA: "Saudi Arabia",
    EG: "Egypt",
    BD: "Bangladesh",
    CA: "Canada",
    HU: 'Hungary',
    DO: 'Dominican Republic',
    RS: 'Serbia',
    KW: 'Kuwait',
  };

  cache = {
    Hungary: [{ "lat": "47.1817585", "lon": "19.5060937", "boundingbox": ["45.737128", "48.585257", "16.1138866", "22.8977094"] }],
    Greece: [{ "lat": "38.9953683", "lon": "21.9877132", "boundingbox": ["34.7188863", "41.7488889", "19.1127535", "29.68381"] }],
    'Dominican Republic': [{ "lat": "19.0974031", "lon": "-70.3028026", "boundingbox": ["17.2701708", "21.2928771", "-72.0574706", "-68.1101463"] }],
    Serbia: [{ "lat": "44.1534121", "lon": "20.55144", "boundingbox": ["42.2314466", "46.1900524", "18.8149945", "23.006309"] }],
    Kuwait: [{ "lat": "29.2733964", "lon": "47.4979476", "boundingbox": ["28.5243622", "30.1038082", "46.5526837", "49.0046809"] }],
    "Puerto Rico": [{ "lat": "18.19897685", "lon": "-66.26152219418967", "boundingbox": ["17.7306659", "18.6663822", "-68.1109184", "-65.1100908"] }],
    "Palestine, State of": [{ "lat": "32.1332947", "lon": "35.2111045", "boundingbox": ["32.1332447", "32.1333447", "35.2110545", "35.2111545"] }],
    "Hong Kong, China": [{ "lat": "22.2793278", "lon": "114.1628131", "boundingbox": ["22.1926504", "22.3002834", "114.1098795", "114.3090495"] }],
    "Germany": [{ lon: "10.4234469", lat: "51.0834196", boundingbox: ['47.2701114', '55.099161', '5.8663153', '15.0419309'] }],
    "France": [{ lat: "46.603354", lon: "1.8883335", boundingbox: ['-50.2187169', '51.3055721', '-178.3873749', '172.3057152'] }],
    "China": [{ "boundingbox": ["8.6537474", "53.5608154", "73.4997347", "134.7754563"], "lat": "35.000074", "lon": "104.999927", }],
    Italy: [{ "boundingbox": ["35.2889616", "47.0921462", "6.6272658", "18.7844746"], "lat": "42.6384261", "lon": "12.674297", }],
    Netherlands: [{ "boundingbox": ["11.825", "53.744395", "-68.6255319", "7.2274985"], "lat": "52.15517", "lon": "5.38721", }],
    "United Kingdom": [{ "lat": "54.7023545", "lon": "-3.2765753", "boundingbox": ["49.674", "61.061", "-14.015517", "2.0919117"] }],
    Canada: [{ "lat": "61.0666922", "lon": "-107.991707", "boundingbox": ["41.6765556", "83.3362128", "-141.00275", "-52.3237664"] }],
    India: [{ "lat": "22.3511148", "lon": "78.6677428", "boundingbox": ["6.5531169", "35.6745457", "67.9544415", "97.395561"] }],
    Brazil: [{ "lat": "-10.3333333", "lon": "-53.2", "boundingbox": ["-33.8689056", "5.2695808", "-73.9830625", "-28.6289646"] }],
    Georgia: [{ "lat": "41.6809707", "lon": "44.0287382", "boundingbox": ["41.0552376", "43.5864294", "39.8844803", "46.7365373"] }],
    Kenya: [{ "lat": "1.4419683", "lon": "38.4313975", "boundingbox": ["-4.8995204", "4.62", "33.9096888", "41.9067502"] }],
    Japan: [{ "lat": "36.5748441", "lon": "139.2394179", "boundingbox": ["20.2145811", "45.7112046", "122.7141754", "154.205541"] }],
    "South Korea": [{ "lat": "36.638392", "lon": "127.6961188", "boundingbox": ["32.0936158", "38.623477", "124.3727348", "132.1467806"] }],
    Singapore: [{ "lat": "1.357107", "lon": "103.8194992", "boundingbox": ["1.1263707", "1.513675", "103.5641457", "104.5721815"] }],
    Colombia: [{ "lat": "4.099917", "lon": "-72.9088133", "boundingbox": ["-4.2316872", "16.0495518", "-82.1243611", "-66.8511571"] }],
    "United States": [{ "lat": "39.7837304", "lon": "-100.445882", "boundingbox": ["-14.7608358", "71.6048217", "-180", "180"] }],
    Indonesia: [{ "lat": "-2.4833826", "lon": "117.8902853", "boundingbox": ["-11.2085669", "6.2744496", "94.7717124", "141.0200345"] }],
    Vietnam: [{ "lat": "13.2904027", "lon": "108.4265113", "boundingbox": ["7.8905854", "23.3926918", "102.1438589", "114.6838596"] }],
    Argentina: [{ "lat": "-34.9964963", "lon": "-64.9672817", "boundingbox": ["-55.1850761", "-21.7808568", "-73.5605371", "-53.6374515"] }],
    Taiwan: [{ "lat": "23.9739374", "lon": "120.9820179", "boundingbox": ["10.3266667", "26.4372222", "114.2880556", "122.297"] }],
    Ireland: [{ "lat": "52.865196", "lon": "-7.9794599", "boundingbox": ["51.222", "55.636", "-11.0133788", "-5.6582363"] }],
    Australia: [{ "lat": "-24.7761086", "lon": "134.755", "boundingbox": ["-55.3228175", "-9.0880125", "72.2461932", "168.2261259"] }],
    Tunisia: [{ "lat": "33.8439408", "lon": "9.400138", "boundingbox": ["30.229061", "37.7612052", "7.5219807", "11.8801133"] }],
    Russia: [{ "lat": "64.6863136", "lon": "97.7453061", "boundingbox": ["41.1850968", "82.0586232", "-180", "180"] }],
    Venezuela: [{ "lat": "8.0018709", "lon": "-66.1109318", "boundingbox": ["0.647529", "15.9158431", "-73.3529632", "-59.5427079"] }],
    Cyprus: [{ "lat": "34.9823018", "lon": "33.1451285", "boundingbox": ["34.4383706", "35.913252", "32.0227581", "34.8553182"] }],
    Turkey: [{ "lat": "38.9597594", "lon": "34.9249653", "boundingbox": ["35.8076804", "42.297", "25.5656305", "44.8176638"] }],
    Thailand: [{ "lat": "14.8971921", "lon": "100.83273", "boundingbox": ["5.612851", "20.4648337", "97.3438072", "105.636812"] }],
    "Bosnia and Herzegovina": [{ "lat": "44.3053476", "lon": "17.5961467", "boundingbox": ["42.5553114", "45.2764135", "15.7287433", "19.6237311"] }],
    Bulgaria: [{ "lat": "42.6073975", "lon": "25.4856617", "boundingbox": ["41.2353678", "44.2156497", "22.3571459", "28.8875409"] }],
    Austria: [{ "lat": "47.2", "lon": "13.2", "boundingbox": ["46.3722987", "49.0205249", "9.5307487", "17.1607728"] }],
    Poland: [{ "lat": "52.215933", "lon": "19.134422", "boundingbox": ["49.0020468", "55.03605", "14.0696389", "24.145783"] }],
    "South Africa": [{ "lat": "-28.8166236", "lon": "24.991639", "boundingbox": ["-47.1788335", "-22.1250301", "16.3335213", "38.2898954"] }],
    Pakistan: [{ "lat": "30.3308401", "lon": "71.247499", "boundingbox": ["23.4341977", "37.084107", "60.872855", "77.1203914"] }],
    Ukraine: [{ "lat": "49.4871968", "lon": "31.2718321", "boundingbox": ["44.184598", "52.3797464", "22.137059", "40.2275801"] }],
    Spain: [{ "lat": "39.3260685", "lon": "-4.8379791", "boundingbox": ["27.4335426", "43.9933088", "-18.3936845", "4.5918885"] }],
    Belgium: [{ "lat": "50.6402809", "lon": "4.6667145", "boundingbox": ["49.4969821", "51.550781", "2.3889137", "6.408097"] }],
    "United Arab Emirates": [{ "lat": "24.0002488", "lon": "53.9994829", "boundingbox": ["22.6316214", "26.1517219", "51.4160714", "56.6024458"] }],
    Switzerland: [{ "lat": "46.7985624", "lon": "8.2319736", "boundingbox": ["45.8179716", "47.8084648", "5.9559113", "10.4922941"] }],
    Chile: [{ "lat": "-31.7613365", "lon": "-71.3187697", "boundingbox": ["-56.725", "-17.4983998", "-109.6795789", "-66.0753474"] }],
    Mexico: [{ "lat": "23.6585116", "lon": "-102.0077097", "boundingbox": ["14.3886243", "32.7186553", "-118.599188", "-86.493266"] }],
    Qatar: [{ "lat": "25.3336984", "lon": "51.2295295", "boundingbox": ["24.4707534", "26.3830212", "50.5675", "52.638011"] }],
    Cameroon: [{ "lat": "4.6125522", "lon": "13.1535811", "boundingbox": ["1.6517489", "13.083333", "8.3822176", "16.1911011"] }],
    "Saudi Arabia": [{ "lat": "25.6242618", "lon": "42.3528328", "boundingbox": ["16.29", "32.1543377", "34.4571718", "55.6666851"] }],
    Egypt: [{ "lat": "26.2540493", "lon": "29.2675469", "boundingbox": ["21.9936506", "31.8330854", "24.6499112", "37.1153517"] }],
    Bangladesh: [{ "lat": "24.4769288", "lon": "90.2934413", "boundingbox": ["20.3679092", "26.6382534", "88.007915", "92.6804979"] }],
    Cambodia: [{ "lat": "13.5066394", "lon": "104.869423", "boundingbox": ["9.4552664", "14.6904224", "102.3338282", "107.6276788"] }],
  }

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

      if (this.cache[dd]) {
        res = this.cache[dd];
      } else {
        res = await this.http.get('https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(dd) + '&format=json&limit=1').
          toPromise();
        console.log(dd, res[0].display_name)
        console.log(res[0].display_name + ': ' + JSON.stringify([{ lat: res[0].lat, lon: res[0].lon, boundingbox: res[0].boundingbox }]))
      }

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

    setTimeout(() => {
      const delta = max - min;

      let zoom = 10;
      if (delta > 1) zoom = 4;
      if (delta > 10) zoom = 1;
      if (delta < 0.1) zoom = 16;

      if (!error || features.length > 0) {
        this.map = new Map({
          target: this.mapDiv.nativeElement,
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
    }, 10);
  }
}
