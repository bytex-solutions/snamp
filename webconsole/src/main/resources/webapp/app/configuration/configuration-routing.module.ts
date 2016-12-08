import { NgModule }            from '@angular/core';
import { RouterModule }        from '@angular/router';

import { GatewaysComponent }    from './configuration.gateways';
import { ResourcesComponent }    from './configuration.resources';
import { RGroupsComponent }    from './configuration.rgroups';
import { SnampCfgComponent }    from './configuration.snampcfg';

@NgModule({
  imports: [RouterModule.forChild([
    { path: '', redirectTo: 'gateways', pathMatch: 'full'},
    { path: 'gateways', component: GatewaysComponent},
    { path: 'resources', component: ResourcesComponent},
    { path: 'rgroups', component: RGroupsComponent},
    { path: 'snampcfg', component: SnampCfgComponent}
  ])],
  exports: [RouterModule]
})
export class ConfigurationRoutingModule {}