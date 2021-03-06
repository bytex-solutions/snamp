import { ChangeDetectorRef, Component, OnInit, ViewContainerRef } from '@angular/core';
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
import { HealthStatusBasedScalingPolicy } from "./model/policy/health.status.based.scaling.policy";
import { AttributeBasedScalingPolicy } from "./model/policy/attribute.based.scaling.policy";
import { isNullOrUndefined } from "util";
import { OpRange } from "./model/policy/operational.range";

@Component({
    moduleId: module.id,
    templateUrl: './templates/main.html',
    styleUrls: ['./templates/css/main.css']
})
export class MainComponent implements OnInit {

    private components: string[] = [];
    private watchers: Watcher[] = [];
    private activeWatcher: Watcher = undefined;
    private isNewEntity: boolean = true;

    private selectedComponent: string = undefined;
    private triggerInitialized: boolean = false;
    private checkersInitialized: boolean = false;
    private policiesInitialized: boolean = false;

    private attributes: AttributeInformation[] = [];
    private selectedAttribute: AttributeInformation = undefined;

    private activeChecker: ScriptletDataObject = new ScriptletDataObject({});

    private activePolicy: ScriptletDataObject = new ScriptletDataObject({});
    private activePolicyName:string = "";

    private checkersType: EntityWithDescription[] = EntityWithDescription.generateCheckersTypes();
    private policyTypes: EntityWithDescription[] = EntityWithDescription.generatePoliciesTypes();
    private strategyTypes:EntityWithDescription[] = EntityWithDescription.generateStrategyTypes();

    private triggerLanguages: string[] = ["Groovy"/*, "JavaScript"*/];

    private availableSupervisors :any[] = [];

    private healthStatusLevels:string[] = ["LOW", "MODERATE", "SUBSTANTIAL", "SEVERE", "CRITICAL"];
    private aggregations:string[] = ["MAX", "MIN", "MEAN", "MEDIAN", "PERCENTILE_90", "PERCENTILE_95", "PERCENTILE_97", "SUM"];

    private defaultGroovyCheckerScript:string = "";
    private defaultGroovyTriggerScript:string = "";
    private defaultGroovyPolicyScript:string = "";

    private operationalRangeVisible:boolean = false;

    private currentRecommendation:OpRange = undefined;

    private newPolicyAppended:boolean = false;

    private groupSelection:boolean = false;
    private groupNameChanged:boolean = false;
    private oldSelectedComponent:string = "";

    constructor(private http: ApiClient, private modal: Modal, overlay: Overlay, vcRef: ViewContainerRef, private cd: ChangeDetectorRef) {
        overlay.defaultViewContainer = vcRef;
    }

    toggleOperationalRangeDialog():void {
        this.operationalRangeVisible = !this.operationalRangeVisible;
    }

    saveCurrentTrigger(): void {
        console.debug("Trigger has been saved: ", this.activeWatcher);
    }

    saveCurrentChecker(): void {
        this.activeWatcher.attributeCheckers[this.selectedAttribute.name] = this.activeChecker;
        console.debug("Checker has been saved", this.activeWatcher);
    }

    getMainHeader():string {
        return isNullOrUndefined(this.activeWatcher) ? "Setup supervisors" : "Setup " + this.activeWatcher.name + " supervisor";
    }

    ngOnInit(): void {
        // load the list of watchers
        this.http.get(REST.SUPERVISORS_CONFIG)
            .map((res: Response) => res.json())
            .subscribe((data) => {
                this.watchers = Factory.watchersArrayFromJSON(data);
                console.debug("All the watchers list: ", this.watchers);
            });

        // find all the components
        this.http.get(REST.GROUPS_WEB_API)
            .map((res: Response) => {
                return <string[]>res.json()
            })
            .subscribe((data) => {
                this.components = data;
            });

        // fill available supervisors list
        this.http.get(REST.AVAILABLE_SUPERVISORS_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => {
                console.debug("Available supervisors list is: ", data);
                this.availableSupervisors = data;
            });

        this.http.get(REST.GROOVY_PATH + "/AttributeChecker.groovy")
            .map((res:Response) => res.text())
            .subscribe(data => {
                this.defaultGroovyCheckerScript = data;
                this.activeChecker.script = data;
            });

        this.http.get(REST.GROOVY_PATH + "/HealthTrigger.groovy")
            .map((res:Response) => res.text())
            .subscribe(data => {
                this.defaultGroovyTriggerScript = data;
            });

        this.http.get(REST.GROOVY_PATH + "/ScalingPolicy.groovy")
            .map((res:Response) => res.text())
            .subscribe(data => {
                this.defaultGroovyPolicyScript = data;
                this.activePolicy.script = data;
            });
    }

    ngAfterViewInit(): void {}

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

    public initPoliciesModal(): void {
        // clean the data if the component was already initialized
        this.newPolicyAppended = false;
        if (this.policiesInitialized) {
            // reset wizard
            $(this.getPoliciesWizardId()).off("showStep");
            $(this.getPoliciesWizardId()).smartWizard("reset");
        }
        this.initPoliciesWizard();
        // open the modal
        $("#editPolicyModal").modal("show");
        // and next time user adds the chart - we will reinit all the dialog
        this.policiesInitialized = true;
    }

    private selectCurrentComponent(component: string): void {
        this.selectedComponent = component;
        this.oldSelectedComponent = component;
        this.loadAttributesOnComponentSelected();
        this.activeWatcher.name = component;
        this.activeWatcher.trigger = new ScriptletDataObject({});
        this.activeWatcher.trigger.script = this.defaultGroovyTriggerScript;
        this.activeWatcher.attributeCheckers = {};
        this.activeWatcher.scalingPolicies = {};
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
            this.activeWatcher.attributeCheckers[attr.name] = new ScriptletDataObject({});
            this.activeWatcher.attributeCheckers[attr.name].script = this.defaultGroovyCheckerScript;
        }
        this.activeChecker = this.activeWatcher.attributeCheckers[attr.name];
        this.selectedAttribute = attr;
        $(this.getCheckersWizardId()).smartWizard("next");
    }

    private loadAttributesOnComponentSelected(): void {
        console.debug("Looking for attributes for group: ", this.selectedComponent);
        this.attributes = [];
        this.http.get(REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
            .map((res: Response) => {
                let _data: any = res.json();
                let _values: AttributeInformation[] = [];
                for (let i in _data) {
                    _values.push(new AttributeInformation(_data[i]));
                }
                return _values;
            })
            .catch((res: Response) => Observable.of([])).cache()
            .subscribe((data) => {
                this.attributes = data;
                console.debug("attributes: ", data);
            });
    }

    public selectCheckerType(type: string): void {
        if (type == "ColoredAttributeChecker") {
            this.activeChecker.object = new ColoredAttributeChecker();
        } else {
            this.activeChecker.object = undefined;
            this.activeChecker.script = this.defaultGroovyCheckerScript;
        }
    }

    public cleanSelection(): void {
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
                                console.debug("watcher has been removed: ", data);
                                for (let i = 0; i < this.watchers.length; i++) {
                                    if (this.watchers[i].name == watcher.name) {
                                        this.watchers.splice(i, 1);
                                        break;
                                    }
                                }
                                this.cd.detectChanges();
                            });
                        return response;
                    })
                    .catch(() => {
                        console.debug("user preferred to decline watcher removing");
                    });
            });
    }

    public editWatcher(watcher: Watcher): void {
        this.activeWatcher = $.extend(true, {}, watcher);
        this.isNewEntity = false;
        this.selectedComponent = watcher.name;
        this.oldSelectedComponent = watcher.name;
        this.groupNameChanged = false;
        this.loadAttributesOnComponentSelected();
        this.groupSelection = this.getGroupSelectionForActiveResource();
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

    private getPoliciesWizardId(): string {
        return "#smartwizardForPolicies";
    }

    private initTriggerWizard(): void {
        $(this.getTriggerWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        $(this.getTriggerWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.debug(stepNumber);
        });
    }

    private initCheckersWizard(): void {
        this.activeChecker = new ScriptletDataObject({});
        this.activeChecker.script = this.defaultGroovyCheckerScript;
        $(this.getCheckersWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        $(this.getCheckersWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.debug(stepNumber);
        });
    }

    private initPoliciesWizard(): void {
        this.activePolicy = new ScriptletDataObject({});
        this.activePolicyName = "";
        this.activePolicy.script = this.defaultGroovyPolicyScript;
        this.operationalRangeVisible = false;
        this.currentRecommendation = undefined;
        $(this.getPoliciesWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        $(this.getPoliciesWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.debug(stepNumber);
        });
    }

    public addNewWatcher(): void {
        this.activeWatcher = new Watcher(undefined, {});
        this.activeWatcher.trigger.script = this.defaultGroovyTriggerScript;
        this.selectedComponent = "";
    }

    public onGreenNotify(event: ColoredAttributePredicate): void {
        this.activeChecker.object.green = event;
        console.debug("Saved green condition: ", event);
    }

    public onYellowNotify(event: ColoredAttributePredicate): void {
        this.activeChecker.object.yellow = event;
        console.debug("Saved yellow condition: ", event);
    }

    public saveActiveWatcher(): void {
        console.debug("Saving selected watcher: ", this.activeWatcher, ", json is: ", this.activeWatcher.toJSON());
        this.http.put(REST.SUPERVISOR_BY_NAME(this.activeWatcher.name), this.activeWatcher.toJSON())
            .map((res: Response) => res.text())
            .subscribe(data => {
                console.debug("watcher has been saved: ", data);
                let _found:boolean = false;
                for (let i = 0; i < this.watchers.length; i++) {
                    if (this.watchers[i].name == this.activeWatcher.name) {
                        this.watchers[i] = this.activeWatcher;
                        _found = true;
                        break;
                    }
                }
                if (!_found) {
                    this.watchers.push(this.activeWatcher);
                }
                this.cleanSelection();
            });
    }

    public isWatcherActive(_watcher:Watcher):boolean {
        return this.activeWatcher != null && this.activeWatcher.name == _watcher.name;
    }


    public isPolicyActive(policyKey:string):boolean {
        return this.activePolicy != undefined && this.activePolicyName != undefined && this.activePolicyName == policyKey;
    }

    public editPolicy(policyKey:string, policyValue:ScriptletDataObject):void {
        this.activePolicyName = policyKey;
        this.activePolicy = policyValue;
        $(this.getPoliciesWizardId()).smartWizard("next");
    }

    public removePolicy(policyKey:string):void {
        delete this.activeWatcher.scalingPolicies[policyKey];
        let newMap:{ [key:string]:ScriptletDataObject; } = {};
        for (let key in this.activeWatcher.scalingPolicies) {
            newMap[key] = this.activeWatcher.scalingPolicies[key];
        }
        this.activeWatcher.scalingPolicies = newMap;
    }

    public addNewPolicy():void {
        this.modal.prompt()
            .className(<VEXBuiltInThemes>'default')
            .message('New policy')
            .placeholder('Please set the name for a new policy')
            .open()
            .then(dialog => dialog.result)
            .then(result => {
                this.activeWatcher.scalingPolicies[result] = new ScriptletDataObject({});
                this.activeWatcher.scalingPolicies[result].script = this.defaultGroovyPolicyScript;
                let newMap:{ [key:string]:ScriptletDataObject; } = {};
                for (let key in this.activeWatcher.scalingPolicies) {
                    newMap[key] = this.activeWatcher.scalingPolicies[key];
                }
                this.activeWatcher.scalingPolicies = newMap;
                this.activePolicy = this.activeWatcher.scalingPolicies[result];
                this.activePolicyName = result;
                this.newPolicyAppended = true;
                $(this.getPoliciesWizardId()).smartWizard("next");
                this.cd.markForCheck();
            })
            .catch(() => {});
    }

    public selectPolicyType(type:string):void {
        if (type == "HealthStatusBased") {
            this.activePolicy.policyObject = new HealthStatusBasedScalingPolicy();
        } else if (type == "MetricBased") {
            this.activePolicy.policyObject = new AttributeBasedScalingPolicy();
        } else {
            this.activeChecker.policyObject = undefined;
            this.activePolicy.script = this.defaultGroovyPolicyScript;
        }
    }

    public saveCurrentPolicy():void {
        this.activeWatcher.scalingPolicies[this.activePolicyName] = this.activePolicy;
        console.debug("Policy has been saved");
    }

    public selectVotingStrategy(type:string):void {
        this.activeWatcher.votingStrategy = type;
        this.activeWatcher.recalculateVotes();
    }

    public updatePolicyRecommendation():void {
        this.http.get(REST.SUPERVISOR_POLICY_RECOMMENDATION(this.activeWatcher.name, this.activePolicyName))
            .map((res:Response) => {console.debug("Response is: ", res.json()); return res.json();})
            .subscribe((data:any) => {
                this.currentRecommendation = OpRange.fromString(data);
                this.cd.detectChanges();
            });
    }

    public applyRecommendation():void {
        (<AttributeBasedScalingPolicy>this.activePolicy.policyObject).operationalRange = this.currentRecommendation;
        this.currentRecommendation = undefined;
        this.cd.detectChanges();
    }

    // group setting

    saveManualGroupName():void {
        this.selectCurrentComponent(this.selectedComponent);
        console.debug("Manual group name has been saved, no reload is required");
        this.groupNameChanged = false;
    }

    triggerGroupNameChanged(value:string):void {
        this.groupNameChanged = (this.oldSelectedComponent != value);
    }

    private getGroupSelectionForActiveResource():boolean {
        if (isNullOrUndefined(this.components) || this.components.length == 0) {
            return false;
        } else if (!isNullOrUndefined(this.activeWatcher)
            && !isNullOrUndefined(this.activeWatcher.name)
            && this.activeWatcher.name.length > 0) {
            for (let i = 0; i < this.components.length; i++) {
                if (this.components[i] == this.activeWatcher.name) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
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
        //_value.push(new EntityWithDescription("JavaScript", "Javascript checker"));
        _value.push(new EntityWithDescription("ColoredAttributeChecker", "Green and yellow conditions based checker"));
        return _value;
    }


    public static generatePoliciesTypes(): EntityWithDescription[] {
        let _value: EntityWithDescription[] = [];
        _value.push(new EntityWithDescription("Groovy", "Groovy policy"));
        _value.push(new EntityWithDescription("HealthStatusBased", "Health status based scaling policy"));
        _value.push(new EntityWithDescription("MetricBased", "Attribute based scaling policy"));
        return _value;
    }

    public static generateStrategyTypes(): EntityWithDescription[] {
        let _value: EntityWithDescription[] = [];
        _value.push(new EntityWithDescription("all", "All policies voted for scaling decision"));
        _value.push(new EntityWithDescription("any", "At least one policy voted for scaling decision"));
        _value.push(new EntityWithDescription("most", "Majorities voted for scaling decision"));
        _value.push(new EntityWithDescription("custom", "User defined vote weights"));
        return _value;
    }
}