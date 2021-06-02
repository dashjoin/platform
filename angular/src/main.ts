import { NgModuleRef } from '@angular/core';
import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { DepInjectorService } from './app/dep-injector.service';
import { DJBaseComponent } from './app/djbase/djbase.component';
import { WidgetRegistry } from './app/widget/widget-registry';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule).then(ngm => {
  DepInjectorService.setModuleType(AppModule);
  // console.log('All widgets', WidgetRegistry.getInstance().getWidgets());
})
  .catch(err => console.error(err));
