import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';

// main components
import { ENV_PROVIDERS } from './environment';
import { ROUTES } from './app.routes';
import { App } from './app.component';

// injectable services
import { SnampLogService } from './services/app.logService';
import { ChartService } from './services/app.chartService';
import { ViewService } from './services/app.viewService';
import { UserProfileService } from "./services/app.user.profile";
import { ApiClient } from './services/app.restClient'

// controls
import { UiSwitchComponent } from './controls/switcher/ui-switch.component';
import { PanelComponent } from './controls/panel/panel.component';
import { InlineEditComponent } from './controls/editor/inline-edit.component';
import { Footer } from './controls/footer/footer.component';
import { UserProfileComponent } from "./controls/userinfo/user.info.panel";

//menu items
import { Sidebar } from './menu/sidebar/sidebar.component';
import { TopNavBar } from './menu/topbar/topnavbar.component';

// configuration components
import { ParametersTable } from './configuration/components/parameters-table.component';
import { ResourceEntitiesTable } from './configuration/components/resource-subentities-table.component';
import { AddEntity } from './configuration/components/add-entity.component';
import { KeysPipe, RequiredParametersFilter, OptionalParametersFilter } from './configuration/configuration.pipes';

// 3rd side components
import { ModalModule } from 'angular2-modal';
import { BootstrapModalModule } from 'angular2-modal/plugins/bootstrap';
import { LocalStorageModule } from 'angular-2-local-storage';
import { TooltipModule } from 'ng2-tooltip';
import { MomentModule } from 'angular2-moment';
import { CookieService } from 'angular2-cookie/core';
import { DropdownModule } from "ng2-dropdown";
import { FontAwesomeDirective } from 'ng2-fontawesome';
import { VexModalModule, providers } from 'angular2-modal/plugins/vex';

// Application wide providers
const APP_PROVIDERS = [
  ApiClient,
  SnampLogService,
  ChartService,
  CookieService,
  ViewService,
  UserProfileService,
  providers
];


@NgModule({
  bootstrap: [ App ],
  declarations:[
    App,
    Footer,
    Sidebar,
    TopNavBar,
    FontAwesomeDirective,
    UserProfileComponent,
    KeysPipe
  ],
  imports: [
    BrowserModule,
    BootstrapModalModule,
    ModalModule.forRoot(),
    TooltipModule,
    FormsModule,
    VexModalModule,
    HttpModule,
    MomentModule,
    LocalStorageModule.withConfig({
      prefix: 'snamp-app',
      storageType: 'localStorage'
    }),
    DropdownModule,
    RouterModule.forRoot(ROUTES, { useHash: true })
  ],
  providers: [
    ENV_PROVIDERS,
    APP_PROVIDERS
  ]
})
export class AppModule {
}

const EXPORTS_UTILS:any = [
  PanelComponent,
  InlineEditComponent,
  UiSwitchComponent
];

@NgModule({
  imports: [
    CommonModule,
    TooltipModule,
    FormsModule
  ],
  declarations: EXPORTS_UTILS,
  exports: EXPORTS_UTILS
})
export class CommonSnampUtilsModule {}


// http://stackoverflow.com/questions/39927357/many-modules-using-the-same-component-causes-error-angular-2
const EXPORTS_CONFIG:any = [
  ParametersTable,
  ResourceEntitiesTable,
  RequiredParametersFilter,
  OptionalParametersFilter,
  AddEntity
];

@NgModule({
  imports: [
    CommonModule,
    CommonSnampUtilsModule,
    TooltipModule,
    FormsModule,
    HttpModule
  ],
  declarations: EXPORTS_CONFIG,
  exports: EXPORTS_CONFIG
})
export class SharedConfigurationModule {}
