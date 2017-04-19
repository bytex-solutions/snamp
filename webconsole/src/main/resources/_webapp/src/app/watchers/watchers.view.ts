import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Factory } from './model/factory';
import { Watcher } from './model/watcher';
import { ScriptletDataObject } from './model/scriptlet.data.object';
import { ColoredAttributePredicate } from './model/colored.predicate';
import { ColoredAttributeChecker } from './model/colored.checker';
import { AttributeInformation } from '../charts/model/attribute';
import { Overlay } from "angular2-modal";
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';

import 'rxjs/add/operator/publishLast';
import 'smartwizard';

@Component({
    moduleId: module.id,
    templateUrl: './templates/main.html',
    styleUrls: ['./templates/css/main.css']
})
export class MainComponent implements OnInit {

    private components: string[] = [];
    private watchers: Watcher[] = [];
    private activeWatcher: Watcher = undefined;
    private copyWatcher: Watcher = undefined;
    private isNewEntity: boolean = true;

    private selectedComponent: string = undefined;
    private triggerInitialized: boolean = false;
    private checkersInitialized: boolean = false;

    private attributes: AttributeInformation[] = [];
    private selectedAttribute: AttributeInformation = undefined;

    private activeChecker: ScriptletDataObject = new ScriptletDataObject();

    private checkersType: EntityWithDescription[] = EntityWithDescription.generateCheckersTypes();

    private triggerLanguages: string[] = ["Groovy", "JavaScript"];

    constructor(private http: ApiClient, private modal: Modal, overlay: Overlay, vcRef: ViewContainerRef) {
        overlay.defaultViewContainer = vcRef;
    }

    saveCurrentTrigger(): void {
        console.log("Trigger has been saved: ", this.activeWatcher);
    }

    saveCurrentChecker(): void {
        this.activeWatcher.attributeCheckers[this.selectedAttribute.name] = this.activeChecker;
        console.log("Checker has been saved", this.activeWatcher);
    }

    ngOnInit(): void {
        // load the list of watchers
        this.http.get(REST.SUPERVISORS_CONFIG)
            .map((res: Response) => res.json())
            .subscribe((data) => {
                this.watchers = Factory.watchersArrayFromJSON(data);
            });

        // find all the components
        this.http.get(REST.CHART_COMPONENTS)
            .map((res: Response) => {
                return <string[]>res.json()
            })
            .subscribe((data) => {
                this.components = data;
            });
    }

    ngAfterViewInit(): void {
    }

    public initTriggerModal(): void {
        // clean the data if the component was already initialized
        if (this.triggerInitialized) {
            // reset wizard
            $(this.getTriggerWizardId()).off("showStep");
            $(this.getTriggerWizardId()).smartWizard("reset");
        }
        this.initTriggerWizard();
        // open the modal
        $("#editTriggerModal").modal("show");
        // and next time user adds the chart - we will reinit all the dialog
        this.triggerInitialized = true;
    }

    public initCheckersModal(): void {
        // clean the data if the component was already initialized
        if (this.checkersInitialized) {
            // reset wizard
            $(this.getCheckersWizardId()).off("showStep");
            $(this.getCheckersWizardId()).smartWizard("reset");
        }
        this.initCheckersWizard();
        // open the modal
        $("#editCheckerModal").modal("show");
        // and next time user adds the chart - we will reinit all the dialog
        this.checkersInitialized = true;
    }

    private selectCurrentComponent(component: string): void {
        this.selectedComponent = component;
        this.loadAttributesOnComponentSelected();
        this.activeWatcher.name = component;
        this.activeWatcher.trigger = new ScriptletDataObject();
        this.activeWatcher.attributeCheckers = {};
    }

    isTriggerAvailable(): boolean {
        return (this.activeWatcher != undefined && this.activeWatcher.trigger != undefined);
    }

    isCheckersAvailable(): boolean {
        return (this.activeWatcher != undefined
            && this.activeWatcher.attributeCheckers != undefined
            && !$.isEmptyObject(this.activeWatcher.attributeCheckers)
        );
    }

    public removeCheckerForAttribute(attr: AttributeInformation): void {
        delete this.activeWatcher.attributeCheckers[attr.name];
    }

    public editCheckerForAttribute(attr: AttributeInformation): void {
        if (!this.activeWatcher.checkerExists(attr.name)) {
            this.activeWatcher.attributeCheckers[attr.name] = new ScriptletDataObject();
        }
        this.activeChecker = this.activeWatcher.attributeCheckers[attr.name];
        this.selectedAttribute = attr;
    }

    private loadAttributesOnComponentSelected(): void {
        console.log("Looking for attributes for group: ", this.selectedComponent);
        this.http.get(REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
            .map((res: Response) => {
                let _data: any = res.json();
                let _values: AttributeInformation[] = [];
                for (let i in _data) {
                    _values.push(new AttributeInformation(_data[i]));
                }
                return _values;
            }).catch((res: Response) => Observable.of([])).cache()
            .subscribe((data) => {
                this.attributes = data;
                console.log("attributes: ", data);
            });
    }

    public selectCheckerType(type: string): void {
        if (type == "ColoredAttributeChecker") {
            this.activeChecker.object = new ColoredAttributeChecker();
        } else {
            this.activeChecker.object = undefined;
        }
    }

    public cleanSelection(): void {
        for (let i = 0; i < this.watchers.length; i++) {
            if (this.watchers[i].guid == this.activeWatcher.guid) {
                this.watchers[i] = this.copyWatcher;
            }
        }
        this.activeWatcher = undefined;
        this.isNewEntity = true;
    }

    public removeWatcher(watcher: Watcher): void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .message('Watcher is going to be removed. Proceed?')
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this.http.delete(REST.SUPERVISOR_BY_NAME(watcher.name))
                            .map((res: Response) => res.text())
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

    public editWatcher(watcher: Watcher): void {
        this.activeWatcher = watcher;
        this.copyWatcher = watcher;
        this.isNewEntity = false;
        this.selectedComponent = watcher.name;
        this.loadAttributesOnComponentSelected();
    }

    public getPanelHeader(): string {
        return this.isNewEntity ? "Add new watcher" : ("Edit watcher " + this.activeWatcher.name);
    }

    private getTriggerWizardId(): string {
        return "#smartwizardForTrigger";
    }

    private getCheckersWizardId(): string {
        return "#smartwizardForCheckers";
    }

    private initTriggerWizard(): void {
        $(this.getTriggerWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        $(this.getTriggerWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.log(stepNumber);
        });
    }

    private initCheckersWizard(): void {
        $(this.getCheckersWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        $(this.getCheckersWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.log(stepNumber);
        });
    }

    public addNewWatcher(): void {
        this.activeWatcher = new Watcher(undefined, {});
        this.selectedComponent = "";
    }

    public onGreenNotify(event: ColoredAttributePredicate): void {
        this.activeChecker.object.green = event;
        console.log("Saved green condition: ", event);
    }

    public onYellowNotify(event: ColoredAttributePredicate): void {
        this.activeChecker.object.yellow = event;
        console.log("Saved yellow condition: ", event);
    }

    public saveActiveWatcher(): void {
        console.log("Saving selected watcher: ", this.activeWatcher, ", json is: ", this.activeWatcher.toJSON());
        this.http.put(REST.SUPERVISOR_BY_NAME(this.activeWatcher.name), this.activeWatcher.toJSON())
            .map((res: Response) => res.text())
            .subscribe(data => {
                console.log("watcher has been saved: ", data);
            });
    }

}

export class EntityWithDescription {
    id: string;
    description: string;

    constructor(id: string, description: string) {
        this.id = id;
        this.description = description;
    }

    public static generateCheckersTypes(): EntityWithDescription[] {
        let _value: EntityWithDescription[] = [];
        _value.push(new EntityWithDescription("Groovy", "Groovy checker"));
        _value.push(new EntityWithDescription("JavaScript", "Javascript checker"));
        _value.push(new EntityWithDescription("ColoredAttributeChecker", "Green and yellow conditions based checker"));
        return _value;
    }
}