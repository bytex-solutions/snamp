import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ApiClient }     from './app.restClient';
import { FormsModule } from '@angular/forms';
import { Gateways }  from './app.gateways';
import { HttpModule } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';
import { KeysPipe, RequriedParametersFilter, OptionalParametersFilter } from './app.pipes'
import { Header } from './app.header'
import { InlineEditComponent } from './components/inline-edit.component'
import { ParametersTable } from './components/parameters-table.component'
import { BindingTable } from './components/binding-table.component'
import { AddEntity } from './components/add-entity.component'

import { VexModalModule, providers } from 'angular2-modal/plugins/vex';
import { ModalModule } from 'angular2-modal';


@NgModule({
  imports:      [ BrowserModule, HttpModule, FormsModule, ModalModule.forRoot(), VexModalModule ],
  declarations: [ Gateways, Header, KeysPipe, InlineEditComponent, ParametersTable, BindingTable,
                    RequriedParametersFilter, OptionalParametersFilter, AddEntity ],
  bootstrap:    [ Gateways ],
  providers:    [ ApiClient, CookieService, providers]
})
export class AppModule { }