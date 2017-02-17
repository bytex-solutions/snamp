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
import { CommonSnampUtilsModule } from '../app.module';

import { TemplateView } from './analysis.template';
import { AddView } from './analysis.add.view';
import { MainView } from './analysis.view';

import { TimeIntervalsView } from './components/time.interval.component';

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
      RouterModule.forChild([{
            path: '', component: TemplateView, children: [
                { path: '', component: AddView },
                { path: ':id', component: MainView }
            ]
      }])
    ],
    declarations: [ TemplateView, AddView, MainView, TimeIntervalsView ],
    providers:    PROVIDERS
})
export class AnalysisModule {}
