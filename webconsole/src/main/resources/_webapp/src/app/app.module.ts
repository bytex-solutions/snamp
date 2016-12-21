import { NgModule, ApplicationRef } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';
import { removeNgStyles, createNewHosts, createInputTransfer } from '@angularclass/hmr';

/*
 * Platform and Environment providers/directives/pipes
 */
import { ENV_PROVIDERS } from './environment';
import { ROUTES } from './app.routes';
// App is our top level component
import { App } from './app.component';
import { APP_RESOLVER_PROVIDERS } from './app.resolver';
import { AppState, InteralStateType } from './app.service';
import { Footer } from './controls/footer.component';
import { Sidebar } from './menu/sidebar.component';
import { TopNavBar } from './menu/topnavbar.component';
import { FlotCmp } from './controls/network-activities.component'
import { UsernameComponent } from './app.username'
import { NoContent } from './no-content';
import { FontAwesomeDirective } from 'ng2-fontawesome';
import { ApiClient } from './app.restClient'
import { CookieService } from 'angular2-cookie/core';
import { DropdownModule } from 'ng2-bootstrap';

import { KeysPipe, RequiredParametersFilter, OptionalParametersFilter } from './configuration/configuration.pipes';
import { InlineEditComponent } from './configuration/components/inline-edit.component';
import { ParametersTable } from './configuration/components/parameters-table.component';
import { ResourceEntitiesTable } from './configuration/components/resource-subentities-table.component';
import { AddEntity } from './configuration/components/add-entity.component';

import { TooltipModule } from 'ng2-tooltip';

// Application wide providers
const APP_PROVIDERS = [
  ...APP_RESOLVER_PROVIDERS,
  AppState,
  ApiClient,
  CookieService
];

type StoreType = {
  state: InteralStateType,
  restoreInputValues: () => void,
  disposeOldHosts: () => void
};

const DECLARATIONS:any = [
  KeysPipe,
  InlineEditComponent,
  ParametersTable,
  ResourceEntitiesTable,
  RequiredParametersFilter,
  OptionalParametersFilter,
  AddEntity
];

/**
 * `AppModule` is the main entry point into Angular2's bootstraping process
 */
@NgModule({
  bootstrap: [ App ],
  declarations: DECLARATIONS.concat([
    App,
    Footer,
    Sidebar,
    TopNavBar,
    FlotCmp,
    NoContent,
    FontAwesomeDirective,
    UsernameComponent
  ]),
  imports: [ // import Angular's modules
    BrowserModule,
    TooltipModule,
    FormsModule,
    HttpModule,
    DropdownModule.forRoot(),
    RouterModule.forRoot(ROUTES, { useHash: true })
  ],
  providers: [ // expose our Services and Providers into Angular's dependency injection
    ENV_PROVIDERS,
    APP_PROVIDERS
  ]
})
export class AppModule {
  constructor(public appRef: ApplicationRef, public appState: AppState) {}

  hmrOnInit(store: StoreType) {
    if (!store || !store.state) return;
    console.log('HMR store', JSON.stringify(store, null, 2));
    // set state
    this.appState._state = store.state;
    // set input values
    if ('restoreInputValues' in store) {
      let restoreInputValues = store.restoreInputValues;
      setTimeout(restoreInputValues);
    }

    this.appRef.tick();
    delete store.state;
    delete store.restoreInputValues;
  }

  hmrOnDestroy(store: StoreType) {
    const cmpLocation = this.appRef.components.map(cmp => cmp.location.nativeElement);
    // save state
    const state = this.appState._state;
    store.state = state;
    // recreate root elements
    store.disposeOldHosts = createNewHosts(cmpLocation);
    // save input values
    store.restoreInputValues  = createInputTransfer();
    // remove styles
    removeNgStyles();
  }

  hmrAfterDestroy(store: StoreType) {
    // display new elements
    store.disposeOldHosts();
    delete store.disposeOldHosts;
  }

}

