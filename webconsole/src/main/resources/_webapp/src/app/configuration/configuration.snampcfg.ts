import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';
import { Overlay } from 'angular2-modal';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';
import { Observable } from "rxjs";

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

    ngOnInit():void {
        this.http.get(REST.COMPONENTS_MANAGEMENT)
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

    selectComponent(selected:SnampComponent):void {
        this.selectedComponent = selected;
    }

    startComponent(selected:SnampComponent):void {
        $('#overlay').fadeIn();
        this.http.postWithErrors(REST.ENABLE_COMPONENT(selected._class, selected.type), "")
            .map((res:Response) => res.text())
            //.catch((error:any) => Observable.throw(new Error(error.status)))
            .subscribe(data => {
                    console.log("Started " + selected.type + " " + selected._class + " component. Result from server is " + data);
                    if (data == "true") {
                        selected.state = "ACTIVE";
                        for (let i = 0; i < this.components.length; i++) {
                            if (this.components[i].equals(selected)) {
                                this.components[i] = selected;
                                break;
                            }
                        }
                    } else {
                        console.log("Could not start component " + selected.type + " - server responded false");
                    }
                    $('#overlay').fadeOut();
                },
                (err) => {
                    let errorText:string = "Could not start  " + selected._class + " " + selected.type + ".";
                    if (err.status == 400) {
                        errorText += "Wrong entity might selected."
                    }
                    this.showModalMessage(errorText);
                })
    }

    stopComponent(selected:SnampComponent):void {
        $('#overlay').fadeIn();
        this.http.postWithErrors(REST.DISABLE_COMPONENT(selected._class, selected.type), "")
            .map((res:Response) => res.text())
            //.catch((error:any) => Observable.throw(new Error(error.status)))
            .subscribe(data => {
                    console.log("Stopped " + selected.type + " " + selected._class + " component. Result from server is " + data);
                    if (data == "true") {
                        selected.state = "RESOLVED";
                        for (let i = 0; i < this.components.length; i++) {
                            if (this.components[i].equals(selected)) {
                                this.components[i] = selected;
                                break;
                            }
                        }
                    } else {
                        console.log("Could not stop component " + selected.type + " - server responded false");
                    }
                    $('#overlay').fadeOut();
                },
                (err) => {
                    let errorText:string = "Could not stop  " + selected._class + " " + selected.type + ".";
                    if (err.status == 400) {
                        errorText += "Wrong entity might selected."
                    }
                    this.showModalMessage(errorText);
                });
    }

    isComponentSelected(component:SnampComponent):boolean {
        return component.equals(this.selectedComponent);
    }

    private showModalMessage(message:string):void {
        this.modal.alert().className(<VEXBuiltInThemes>'default').message(message).open().catch(() => {});
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
            this._class = parameters["class"];
        }
    }

    equals(component:SnampComponent):boolean {
        return (component._class == this._class) && (component.name == this.name) && (component.type == this.type);
    }
}
