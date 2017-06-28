import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';
import { ApiClient }     from '../services/app.restClient';
import { TooltipModule } from 'ng2-tooltip';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';
import { ModalModule } from 'angular2-modal';
import {
  VexModalModule,
  providers
} from 'angular2-modal/plugins/vex';
import { CommonSnampUtilsModule } from '../app.module';

import { TemplateComponent } from './watchers.template';
import { MainComponent } from './watchers.view';
import { WatcherDashboard } from './watchers.dashboard';

import { CheckersComponent } from './components/checkers.component';
import { TriggerComponent } from './components/trigger.component';
import { ColoredCondition } from './components/condition.block';

import { KeysPipe } from './watchers.pipes';
import { WatcherProvisioning } from "./watchers.prov";
import {PTable} from "./components/ptable.component";

const PROVIDERS:any =  [
  ApiClient,
  providers
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
      RouterModule.forChild([{
            path: '', component: TemplateComponent, children: [
                { path: '', component: MainComponent },
                { path: 'dashboard', component: WatcherDashboard },
                { path: 'prov', component: WatcherProvisioning }
            ]
      }])
    ],
    declarations: [
        TemplateComponent,
        MainComponent,
        WatcherDashboard,
        WatcherProvisioning,
        CheckersComponent,
        TriggerComponent,
        ColoredCondition,
        PTable,
        KeysPipe
    ],
    providers:    PROVIDERS
})
export class WatchersModule {}
