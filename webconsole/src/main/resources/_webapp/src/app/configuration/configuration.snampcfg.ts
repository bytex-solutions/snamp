import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

@Component({
    moduleId: module.id,
    templateUrl: './templates/snampcfg.html',
    styleUrls: ['./templates/css/snampcfg.css']
})
export class SnampCfgComponent implements OnInit {

    public selectedComponent:SnampComponent;
    public components:SnampComponent[] = [];

    constructor(private http: ApiClient,
                overlay: Overlay,
                vcRef: ViewContainerRef,
                private modal: Modal) {
        overlay.defaultViewContainer = vcRef;
    }

    ngOnInit() {
        this.http.get(REST.AVAILABLE_COMPONENT_LIST)
            .map((res:Response) => res.json())
            .subscribe(data => {
                for (let i = 0; i < data.length; i++) {
                    this.components.push(new SnampComponent(data[i]));
                }
                if (this.components.length > 0) {
                    this.selectedComponent = this.components[0];
                }
            });
    }

    selectComponent(selected:SnampComponent) {
        this.selectedComponent = selected;
    }

    startComponent(selected:SnampComponent) {
        $('#overlay').fadeIn();
        this.http.post(REST.ENABLE_COMPONENT(selected._class, selected.type), "")
            .map((res:Response) => res.text())
            .subscribe(data => {
                console.log("started " + selected.type + " component. result from server is " + data);
                if (data == "true") {
                    selected.state = "ACTIVE";
                    for (let i = 0; i < this.components.length; i++) {
                        if (this.components[i].type == selected.type) {
                            this.components[i] = selected;
                            break;
                        }
                    }
                } else {
                    console.log("Could not start component " + selected.type + " - server responded false");
                }
                $('#overlay').fadeOut();
            })
    }

    stopComponent(selected:SnampComponent) {
        $('#overlay').fadeIn();
        this.http.post(REST.DISABLE_COMPONENT(selected._class, selected.type), "")
            .map((res:Response) => res.text())
            .subscribe(data => {
                console.log("stopped " + selected.type + " component. result from server is " + data);
                if (data == "true") {
                    selected.state = "RESOLVED";
                    for (let i = 0; i < this.components.length; i++) {
                        if (this.components[i].type == selected.type) {
                            this.components[i] = selected;
                            break;
                        }
                    }
                } else {
                    console.log("Could not stop component " + selected.type + " - server responded false");
                }
                $('#overlay').fadeOut();
            })
    }
}


class SnampComponent {
    public name:string = "";
    public description:string = "";
    public state:string = "";
    public version:string = "";
    public type:string = "";
    public _class:string = "";
    constructor(parameters:any) {
        if (parameters["name"] != undefined) {
            this.name = parameters["name"];
        }
        if (parameters["description"] != undefined) {
            this.description = parameters["description"];
        }
        if (parameters["state"] != undefined) {
            this.state = parameters["state"];
        }
        if (parameters["version"] != undefined) {
            this.version = parameters["version"];
        }
        if (parameters["type"] != undefined) {
            this.type = parameters["type"];
        }
        if (parameters["class"] != undefined) {
            this._class = SnampComponent.typeToResourceClassName(parameters["class"]) + "s";
        }
    }

    private static typeToResourceClassName(type:string):string {
        return type.substring(0, type.indexOf("Type")).toLowerCase();
    }
}
