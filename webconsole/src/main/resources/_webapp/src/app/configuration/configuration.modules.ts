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

import { BindingTable } from './components/binding-table.component'

import { VexModalModule, providers } from 'angular2-modal/plugins/vex';
import { ModalModule } from 'angular2-modal';
import { TooltipModule } from 'ng2-tooltip';
import { Routes, RouterModule } from '@angular/router';

import { SharedConfigurationModule } from '../app.module';

import { NgxDatatableModule } from '@swimlane/ngx-datatable';


// must read http://blog.angular-university.io/angular2-ngmodule/

const IMPORTS:any = [
  CommonModule,
  NgxDatatableModule,
  HttpModule,
  FormsModule,
  ModalModule.forRoot(),
  VexModalModule,
  TooltipModule,
  SharedConfigurationModule
];

const PROVIDERS:any =  [
  ApiClient,
  CookieService
];

@NgModule({
  imports:      IMPORTS.concat([RouterModule.forChild([{ path: '', component: GatewaysComponent }])]),
  declarations: [ GatewaysComponent, BindingTable ],
  providers:    PROVIDERS
})
export class GatewaysModule { }

@NgModule({
  imports:      IMPORTS.concat([RouterModule.forChild([{ path: '', component: ResourcesComponent }])]),
  declarations: [ ResourcesComponent ],
  providers:    PROVIDERS
})
export class ResourcesModule { }

@NgModule({
  imports:      IMPORTS.concat([RouterModule.forChild([{ path: '', component: RGroupsComponent }])]),
  declarations: [ RGroupsComponent ],
  providers:    PROVIDERS
})
export class RGroupsModule { }

@NgModule({
  imports:      IMPORTS.concat([RouterModule.forChild([{ path: '', component: SnampCfgComponent }])]),
  declarations: [ SnampCfgComponent ],
  providers:    PROVIDERS
})
export class SnampCFGModule { }
