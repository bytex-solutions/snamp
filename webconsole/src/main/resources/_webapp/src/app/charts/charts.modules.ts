import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';
import { ApiClient }     from '../app.restClient';
import { TooltipModule } from 'ng2-tooltip';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { PanelComponent } from '../panel.component';
import { Routes, RouterModule } from '@angular/router';
import { ModalModule } from 'angular2-modal';
import { VexModalModule } from 'angular2-modal/plugins/vex';
import { NgGridModule } from '../controls/nggrid/modules/NgGrid.module';
import { Dashboard } from './charts.dashboard';
import { CommonSnampUtilsModule } from '../app.module';

const PROVIDERS:any =  [
  ApiClient
];

@NgModule({
   imports: [
      CommonModule,
      TooltipModule,
      FormsModule,
      ModalModule.forRoot(),
      VexModalModule,
      HttpModule,
      CommonSnampUtilsModule,
      NgGridModule,
      RouterModule.forChild([{ path: '', component: Dashboard }])
    ],
    declarations: [ Dashboard ],
    providers:    PROVIDERS
})
export class DashboardModule {}
