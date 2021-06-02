import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { routes } from './login/login.component';

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
