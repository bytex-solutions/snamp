import { NgModule }      from '@angular/core';
import { ApiClient }     from '../app.restClient';
import { CommonModule }       from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GatewaysComponent }  from './configuration.gateways';
import { ResourcesComponent }  from './configuration.resources';
import { RGroupsComponent }  from './configuration.rgroups';
import { SnampCfgComponent }  from './configuration.snampcfg';
import { HttpModule } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';
import { KeysPipe, RequiredParametersFilter, OptionalParametersFilter } from './configuration.pipes'
import { InlineEditComponent } from './components/inline-edit.component'
import { ParametersTable } from './components/parameters-table.component'
import { ResourceEntitiesTable } from './components/resource-subentities-table.component'
import { BindingTable } from './components/binding-table.component'
import { AddEntity } from './components/add-entity.component'
import { ConfigurationRoutingModule } from './configuration-routing.module'

import { VexModalModule, providers } from 'angular2-modal/plugins/vex';
import { ModalModule } from 'angular2-modal';
import { TooltipModule } from 'ng2-tooltip';


@NgModule({
  imports:      [ CommonModule,ConfigurationRoutingModule,  HttpModule, FormsModule, ModalModule.forRoot(),
                    VexModalModule, TooltipModule ],
  declarations: [ GatewaysComponent, ResourcesComponent, RGroupsComponent, SnampCfgComponent,
                    KeysPipe,  InlineEditComponent, ParametersTable, BindingTable, ResourceEntitiesTable,
                    RequiredParametersFilter, OptionalParametersFilter, AddEntity ],
  providers:    [ ApiClient, CookieService, providers]
})
export class ConfigurationModule { }