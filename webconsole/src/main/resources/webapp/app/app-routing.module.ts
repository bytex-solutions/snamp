import { NgModule }             from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ApiClient }     from './app.restClient';
import { HttpModule } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';

export const routes: Routes = [
  { path: '', redirectTo: 'configuration', pathMatch: 'full'},
  { path: 'charts', loadChildren: 'app/charts/charts.module#ChartsModule' },
  { path: 'analysis', loadChildren: 'app/analysis/analysis.module#AnalysisModule' },
  { path: 'watchers', loadChildren: 'app/watchers/watchers.module#WatchersModule' },
  { path: 'configuration', loadChildren: 'app/configuration/configuration.module#ConfigurationModule' }
];

@NgModule({
  imports: [ HttpModule, RouterModule.forRoot(routes) ],
  exports: [ RouterModule ],
  providers: [ ApiClient, CookieService ]
})
export class AppRoutingModule {}