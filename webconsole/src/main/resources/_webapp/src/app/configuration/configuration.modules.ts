import { NgModule }      from '@angular/core';
import { ApiClient }     from '../services/app.restClient';
import { CommonModule }       from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GatewaysComponent }  from './configuration.gateways';
import { ResourcesComponent }  from './configuration.resources';
import { RGroupsComponent }  from './configuration.rgroups';
import { SnampCfgComponent }  from './configuration.snampcfg';
import { SnampLogViewComponent }  from './configuration.logview';
import { FullSaveComponent }  from './configuration.fullsave';
import { SnampLogSettingsComponent } from "./configuration.logsettings";
import { HttpModule } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';

import { BindingTable } from './components/binding-table.component'

import { VexModalModule } from 'angular2-modal/plugins/vex';
import { ModalModule } from 'angular2-modal';
import { TooltipModule } from 'ng2-tooltip';
import { RouterModule } from '@angular/router';

import { SharedConfigurationModule, CommonSnampUtilsModule } from '../app.module';

import { Ng2SmartTableModule } from 'ng2-smart-table';


// must read http://blog.angular-university.io/angular2-ngmodule/

const IMPORTS:any = [
  CommonSnampUtilsModule,
  CommonModule,
  Ng2SmartTableModule,
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

@NgModule({
  imports:      IMPORTS.concat([RouterModule.forChild([{ path: '', component: SnampLogViewComponent }])]),
  declarations: [ SnampLogViewComponent ],
  providers:    PROVIDERS
})
export class SnampLogViewModule { }

@NgModule({
  imports:      IMPORTS.concat([RouterModule.forChild([{ path: '', component: FullSaveComponent }])]),
  declarations: [ FullSaveComponent ],
  providers:    PROVIDERS
})
export class FullSaveModule { }

@NgModule({
  imports:      IMPORTS.concat([RouterModule.forChild([{ path: '', component: SnampLogSettingsComponent }])]),
  declarations: [ SnampLogSettingsComponent ],
  providers:    PROVIDERS
})
export class NotificationsModule { }