import { Component, OnInit, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Factory } from './model/factory';
import { Watcher } from './model/watcher';

import { Router } from '@angular/router';

import { AttributeInformation } from '../charts/model/attribute';

import 'rxjs/add/operator/publishLast';
import 'select2';
import 'smartwizard';

import { overlayConfigFactory, Overlay } from "angular2-modal";
import {
  VEXBuiltInThemes,
  Modal,
  DialogFormModal
} from 'angular2-modal/plugins/vex';


@Component({
  moduleId: module.id,
  templateUrl: './templates/main.html',
  styleUrls: [ './templates/css/main.css' ]
})
export class MainComponent implements OnInit {

    private http:ApiClient;
    components:string[] = [];
    private watchers:Watcher[] = [];
    activeWatcher:Watcher = new Watcher(undefined, {});
    copyWatcher:Watcher = undefined;
    isNewEntity:boolean = true;

    selectedComponent:string = undefined;

    metrics:Observable<AttributeInformation[]>;
    selectedMetric:AttributeInformation = undefined;

    triggerLanguages:string[] = [ "Groovy", "JavaScript" ];

    constructor(apiClient: ApiClient, private _router: Router, private modal: Modal,  overlay: Overlay, vcRef: ViewContainerRef) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

   saveCurrentTrigger():void {
        console.log(this.activeWatcher);
   }

   ngOnInit():void {
        // load the list of watchers
        this.http.get(REST.WATCHERS_LIST)
            .map((res:Response) => res.json())
            .subscribe((data) => {
                this.watchers = Factory.watchersArrayFromJSON(data);
            });

         // find all the components
        this.http.get(REST.CHART_COMPONENTS)
            .map((res:Response) => { return <string[]>res.json()})
            .subscribe((data) => {
                this.components = data;
            });

        var _thisReference = this;
        // initialize select2 logic
        $(document).ready(function() {
             $("#componentSelection").select2();
             $("#componentSelection").on('change', (e) => {
                  _thisReference.selectCurrentComponent($(e.target).val());
             });
        });

        this.initTriggerWizard();
        this.initCheckersWizard();
   }

    private selectCurrentComponent(component:string):void {
        this.selectedComponent = component;
        this.loadMetricsOnComponentSelected();
    }

    isTriggerAvailable():boolean {
        return (this.activeWatcher != undefined && this.activeWatcher.trigger != undefined);
    }

    isCheckersAvailable():boolean {
        return (this.activeWatcher != undefined
            && this.activeWatcher.attributeCheckers != undefined
            && !$.isEmptyObject(this.activeWatcher.attributeCheckers)
        );
    }

    private loadMetricsOnComponentSelected():void {
       this.metrics = this.http.getIgnoreErrors(REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
            .map((res:Response) => {
                let _data:any = res.json();
                let _values:AttributeInformation[] = [];
                for (let i in _data) {
                    _values.push(new AttributeInformation(_data[i]));
                }
                return _values;
            }).catch((res:Response) => Observable.of([])).cache();

       // set auto selected first metric if the array is not empty
       this.metrics.subscribe((data:AttributeInformation[]) => {
           if (data && data.length > 0) {
               this.selectedMetric = data[0];
           }
       })
    }

   public getAvailableComponents():string[] {
        return this.components.filter((element) => {
            let _available:boolean = true;
            for (let i = 0; i < this.watchers.length; i++) {
                if (this.watchers[i].name == element) {
                    _available = false;
                    break;
                }
            }
            return _available;
        });
   }

   public cleanSelection():void {
        for (let i = 0; i < this.watchers.length; i++) {
            if (this.watchers[i].guid == this.activeWatcher.guid) {
                this.watchers[i] = this.copyWatcher;
            }
        }
        this.activeWatcher = new Watcher(undefined, {});
        this.isNewEntity = true;
   }

   public removeWatcher(watcher:Watcher):void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .message('Watcher is going to be removed. Proceed?')
            .open()
             .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                  .then((response) => {
                     this.http.delete(REST.WATCHER_BY_NAME(watcher.name))
                         .map((res:Response) => res.text())
                         .subscribe(data => {
                            console.log("watcher has been removed: ", data);
                         });
                     return response;
                  })
                  .catch(() => {
                    console.log("user preferred to decline watcher removing");
                  });
              });
   }

   public editWatcher(watcher:Watcher):void {
        this.activeWatcher = watcher;
        this.isNewEntity = false;
   }

    public getPanelHeader():string {
        return this.isNewEntity ? "Add new watcher" : ("Edit watcher " + this.activeWatcher.name);
    }

    private getTriggerWizardId():string {
      return "#smartwizardForTrigger";
    }

    private getCheckersWizardId():string {
      return "#smartwizardForCheckers";
    }

    private initTriggerWizard():void {
        console.log($(this.getTriggerWizardId()));
        $(this.getTriggerWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        var _thisReference = this;

        $(this.getTriggerWizardId()).on("showStep", function(e, anchorObject, stepNumber, stepDirection) {
            console.log(stepNumber);
        });
    }

    private initCheckersWizard():void {
        $(this.getCheckersWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        var _thisReference = this;

        $(this.getCheckersWizardId()).on("showStep", function(e, anchorObject, stepNumber, stepDirection) {
            console.log(stepNumber);
        });
    }

}