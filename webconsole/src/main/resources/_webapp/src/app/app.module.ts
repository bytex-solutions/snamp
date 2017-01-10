import { NgModule, ApplicationRef } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';
import { removeNgStyles, createNewHosts, createInputTransfer } from '@angularclass/hmr';


import { ENV_PROVIDERS } from './environment';
import { ROUTES } from './app.routes';
import { App } from './app.component';
import { Footer } from './controls/footer.component';
import { Sidebar } from './menu/sidebar.component';
import { TopNavBar } from './menu/topnavbar.component';
import { FlotCmp } from './controls/network-activities.component';
import { UsernameComponent } from './app.username';
import { SnampLogService } from './app.logService';
import { NoContent } from './no-content';
import { FontAwesomeDirective } from 'ng2-fontawesome';
import { ApiClient } from './app.restClient'
import { CookieService } from 'angular2-cookie/core';
import { DropdownModule } from 'ng2-bootstrap';

import { UiSwitchComponent } from './ui-switch.component';

import { PanelComponent } from './panel.component';

import { KeysPipe, RequiredParametersFilter, OptionalParametersFilter } from './configuration/configuration.pipes';
import { InlineEditComponent } from './configuration/components/inline-edit.component';
import { ParametersTable } from './configuration/components/parameters-table.component';
import { ResourceEntitiesTable } from './configuration/components/resource-subentities-table.component';
import { AddEntity } from './configuration/components/add-entity.component';
import { ModalModule } from 'angular2-modal';
import { BootstrapModalModule } from 'angular2-modal/plugins/bootstrap';

import { LocalStorageModule } from 'angular-2-local-storage';

import { TooltipModule } from 'ng2-tooltip';

import { MomentModule } from 'angular2-moment';


// Application wide providers
const APP_PROVIDERS = [
  ApiClient,
  SnampLogService,
  CookieService
];


/**
 * `AppModule` is the main entry point into Angular2's bootstraping process
 */
@NgModule({
  bootstrap: [ App ],
  declarations:[
    App,
    Footer,
    Sidebar,
    TopNavBar,
    FlotCmp,
    NoContent,
    FontAwesomeDirective,
    UsernameComponent,
    KeysPipe
  ],
  imports: [ // import Angular's modules
    BrowserModule,
    BootstrapModalModule,
    ModalModule.forRoot(),
    TooltipModule,
    FormsModule,
    HttpModule,
    MomentModule,
    LocalStorageModule.withConfig({
      prefix: 'snamp-app',
      storageType: 'localStorage'
    }),
    DropdownModule.forRoot(),
    RouterModule.forRoot(ROUTES, { useHash: true })
  ],
  providers: [ // expose our Services and Providers into Angular's dependency injection
    ENV_PROVIDERS,
    APP_PROVIDERS
  ]
})
export class AppModule {
}


// http://stackoverflow.com/questions/39927357/many-modules-using-the-same-component-causes-error-angular-2
const EXPORTS:any = [
  PanelComponent,
  InlineEditComponent,
  ParametersTable,
  ResourceEntitiesTable,
  RequiredParametersFilter,
  OptionalParametersFilter,
  AddEntity,
  UiSwitchComponent
];

@NgModule({
  imports: [
    CommonModule,
    TooltipModule,
    FormsModule,
    HttpModule
  ],
  declarations: EXPORTS,
  exports: EXPORTS
})
export class SharedConfigurationModule {}

