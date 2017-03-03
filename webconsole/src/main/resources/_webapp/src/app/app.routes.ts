import { Routes, RouterModule } from '@angular/router';
import { DataResolver } from './app.resolver';

export const ROUTES: Routes = [
  { path: '', redirectTo: 'gateways', pathMatch: 'full'},
  { path: 'gateways', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.GatewaysModule) },
  { path: 'resources', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.ResourcesModule) },
  { path: 'rgroups', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.RGroupsModule) },
  { path: 'snampcfg', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.SnampCFGModule) },
  { path: 'logview', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.SnampLogViewModule) },
  { path: 'configuration', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.FullSaveModule) },
  { path: 'charts', loadChildren: () => System.import('./charts/charts.modules').then(m => m.DashboardModule) },
  { path: 'view', loadChildren: () => System.import('./analysis/analysis.modules').then(m => m.AnalysisModule) },
  { path: 'watchers', loadChildren: () => System.import('./watchers/watchers.modules').then(m => m.WatchersModule) }
];
