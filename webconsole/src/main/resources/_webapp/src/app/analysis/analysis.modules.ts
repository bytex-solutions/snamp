import { NgModule }      from '@angular/core';
import { CommonModule }  from '@angular/common';
import { ApiClient }     from '../services/app.restClient';
import { TooltipModule } from 'ng2-tooltip';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { PanelComponent } from '../controls/panel.component';
import { Routes, RouterModule } from '@angular/router';
import { ModalModule } from 'angular2-modal';
import {
  VexModalModule,
  providers
} from 'angular2-modal/plugins/vex';
import { CommonSnampUtilsModule } from '../app.module';

import { TemplateView } from './analysis.template';
import { AddView } from './analysis.add.view';
import { MainView } from './analysis.view';

import { TimeIntervalsView } from './components/time.interval.component';
import { CheckboxGroupView } from './components/checkbox.group.component';

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
            path: '', component: TemplateView, children: [
                { path: '', component: AddView },
                { path: ':id', component: MainView }
            ]
      }])
    ],
    declarations: [ TemplateView, AddView, MainView, TimeIntervalsView, CheckboxGroupView ],
    providers:    PROVIDERS
})
export class AnalysisModule {}
