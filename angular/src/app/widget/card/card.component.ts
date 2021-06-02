import { ChangeDetectorRef, Component, ElementRef, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DJBaseComponent } from '../../djbase/djbase.component';
import { ContainerComponent } from '../container/container.component';
import { DashjoinWidget } from '../widget-registry';

/**
 * card container
 */
@DashjoinWidget({
  name: 'card',
  category: 'Default',
  description: 'Component that draws card container',
  htmlTag: 'dj-card',
  isContainer: true,
  fields: ['title', 'text', 'roles', 'layout', 'if', 'foreach', 'class', 'style']
})
@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.css']
})
export class CardComponent extends ContainerComponent implements OnInit {
}
