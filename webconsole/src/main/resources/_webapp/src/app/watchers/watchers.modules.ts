import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';
import { ApiClient }     from '../app.restClient';
import { TooltipModule } from 'ng2-tooltip';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { PanelComponent } from '../panel.component';
import { Routes, RouterModule } from '@angular/router';
import { ModalModule } from 'angular2-modal';
import {
  VexModalModule,
  providers
} from 'angular2-modal/plugins/vex';
import { CommonSnampUtilsModule } from '../app.module';

import { TemplateComponent } from './watchers.template';
import { MainComponent } from './watchers.view';

import { CheckersComponent } from './components/checkers.component';
import { TriggerComponent } from './components/trigger.component';
import { ColoredCondition } from './components/condition.block';

import { KeysPipe } from './watchers.pipes';

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
                { path: '', component: MainComponent }
            ]
      }])
    ],
    declarations: [ TemplateComponent, MainComponent, CheckersComponent, TriggerComponent, ColoredCondition, KeysPipe ],
    providers:    PROVIDERS
})
export class WatchersModule {}
