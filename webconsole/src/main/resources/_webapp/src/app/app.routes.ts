import { Routes, RouterModule } from '@angular/router';
import { NoContent } from './no-content';

import { DataResolver } from './app.resolver';

export const ROUTES: Routes = [
  { path: '', redirectTo: 'gateways', pathMatch: 'full'},
  { path: 'gateways', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.GatewaysModule) },
  { path: 'resources', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.ResourcesModule) },
  { path: 'rgroups', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.RGroupsModule) },
  { path: 'snampcfg', loadChildren: () => System.import('./configuration/configuration.modules').then(m => m.SnampCFGModule) }
];
