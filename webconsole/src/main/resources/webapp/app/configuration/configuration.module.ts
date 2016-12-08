import { NgModule }      from '@angular/core';
import { ApiClient }     from '../app.restClient';
import { CommonModule }       from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GatewaysComponent }  from './configuration.gateways';
import { HttpModule } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';
import { KeysPipe, RequiredParametersFilter, OptionalParametersFilter } from './configuration.pipes'
import { InlineEditComponent } from './components/inline-edit.component'
import { ParametersTable } from './components/parameters-table.component'
import { BindingTable } from './components/binding-table.component'
import { AddEntity } from './components/add-entity.component'

import { VexModalModule, providers } from 'angular2-modal/plugins/vex';
import { ModalModule } from 'angular2-modal';
import { TooltipModule } from 'ng2-tooltip';


@NgModule({
  imports:      [ CommonModule, HttpModule, FormsModule, ModalModule.forRoot(), VexModalModule, TooltipModule ],
  declarations: [ GatewaysComponent, KeysPipe, InlineEditComponent, ParametersTable, BindingTable,
                    RequiredParametersFilter, OptionalParametersFilter, AddEntity ],
  bootstrap:    [ GatewaysComponent ],
  providers:    [ ApiClient, CookieService, providers]
})
export class ConfigurationModule { }