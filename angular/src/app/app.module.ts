import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, Injector, NgModule } from '@angular/core';

import { AppComponent, MainComponent } from './app.component';
import { JsonSchemaFormModule } from '@dashjoin/json-schema-form';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { InstanceComponent } from './instance/instance.component';
import { AppRoutingModule } from '../app/app-routing.module';
import { MatTableModule } from '@angular/material/table';
import { QueryComponent } from './query/query.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NgChartsModule } from 'ng2-charts';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { DjInterceptor } from './dj-interceptor';
import { LoginComponent } from '../app/login/login.component';
import { MatDividerModule } from '@angular/material/divider';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { EditWidgetDialogComponent } from './edit-widget-dialog/edit-widget-dialog.component';
import { GridsterModule } from 'angular-gridster2';
import { MatRadioButton, MatRadioModule } from '@angular/material/radio';
import { environment } from 'src/environments/environment';
import { UploadDialogComponent } from './upload-dialog/upload-dialog.component';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { FlexLayoutModule } from '@angular/flex-layout';
import { DeleteConfirmDialogComponent } from './delete-confirm-dialog/delete-confirm-dialog.component';
import { ExpressionComponent } from './expression/expression.component';
import { StreamExpressionComponent } from './expression/stream.expression.component';
import { MatListModule } from '@angular/material/list';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { HoverClassDirective } from './instance/hover-class.directive';
import { MatSortModule } from '@angular/material/sort';
import { MappingComponent, MappingDialogComponent } from './mapping/mapping.component';

import { DepInjectorService } from './dep-injector.service';

import { DJBaseComponent } from './djbase/djbase.component';
import { TableComponent } from './widget/table/table.component';
import { TextComponent } from './widget/text/text.component';
import { ContainerComponent } from './widget/container/container.component';
import { EditComponent } from './widget/edit/edit.component';
import { CreateComponent } from './widget/create/create.component';
import { DisplayComponent } from './widget/display/display.component';
import { MarkdownComponent } from './widget/markdown/markdown.component';
import { CardComponent } from './widget/card/card.component';
import { ChartComponent } from './widget/chart/chart.component';
import { JsonCellDialogCopyComponent, LinksComponent } from './widget/links/links.component';
import { EditRelatedComponent } from './widget/edit-related/edit-related.component';
import { IconComponent } from './widget/icon/icon.component';
import { SearchComponent } from './widget/search/search.component';
import { CompDirective } from './instance/comp.directive';
import { UploadComponent } from './widget/upload/upload.component';
import { ButtonComponent } from './widget/button/button.component';
import { VariableComponent } from './widget/variable/variable.component';
import { ToolbarComponent } from './widget/toolbar/toolbar.component';
import { ActivityStatusComponent } from './widget/activity-status/activity-status.component';
import { LayoutEditSwitchComponent } from './widget/layout-edit-switch/layout-edit-switch.component';
import { SpacerComponent } from './widget/spacer/spacer.component';
import { ExpansionComponent } from './widget/expansion/expansion.component';
import { GridComponent } from './widget/grid/grid.component';
import { TreeComponent } from './widget/tree/tree.component';
import { MatTreeModule } from '@angular/material/tree';
import { MatSidenavModule } from '@angular/material/sidenav';
import { SidenavSwitchComponent } from './widget/sidenav-switch/sidenav-switch.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';
import { WidgetListComponent } from './edit-widget-dialog/widgetlist.component';
import { MapComponent } from './widget/map/map.component';
import { HTMLComponent } from './widget/html/html.component';

export const __DJ_TYPES = [
  DJBaseComponent,
  TableComponent,
  TextComponent,
  ContainerComponent,
  EditComponent,
  CreateComponent,
  DisplayComponent,
  MarkdownComponent,
  CardComponent,
  ChartComponent,
  LinksComponent,
  EditRelatedComponent,
  IconComponent,
  SearchComponent,
  UploadComponent,
  ButtonComponent,
  VariableComponent,
  ToolbarComponent,
  ActivityStatusComponent,
  LayoutEditSwitchComponent,
  SpacerComponent,
  ExpansionComponent,
  GridComponent,
  TreeComponent,
  SidenavSwitchComponent,
  MapComponent,
  HTMLComponent
];
// Decouple access to avoid circular deps:
(window as any).__DJ_TYPES = __DJ_TYPES;

@NgModule({
  declarations: [
    CompDirective,
    AppComponent,
    MainComponent,
    InstanceComponent,
    QueryComponent,
    MappingDialogComponent,
    LoginComponent,
    EditWidgetDialogComponent,
    UploadDialogComponent,
    DeleteConfirmDialogComponent,
    JsonCellDialogCopyComponent,
    ExpressionComponent,
    StreamExpressionComponent,
    HoverClassDirective,
    MappingComponent,
    ConfirmationDialogComponent,
    WidgetListComponent,
    ...__DJ_TYPES,
    HTMLComponent
  ],
  imports: [
    AppRoutingModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    JsonSchemaFormModule,
    MatButtonModule,
    MatExpansionModule,
    MatCardModule,
    MatTableModule,
    MatToolbarModule,
    MatIconModule,
    MatTooltipModule,
    MatDialogModule,
    NgChartsModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
    MatMenuModule,
    GridsterModule,
    MatRadioModule,
    MatSnackBarModule,
    MatTabsModule,
    MatSelectModule,
    MatSortModule,
    FlexLayoutModule,
    MatListModule,
    MatButtonToggleModule,
    MatTreeModule,
    MatSidenavModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: DjInterceptor,
      multi: true,
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(private injector: Injector) {
    DepInjectorService.setInjector(injector);
  }
}
