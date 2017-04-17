import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from "@angular/http";

import 'select2';

@Component({
  moduleId: module.id,
  templateUrl: './templates/logset.html',
  styles: [".hided: { display: none }"]
})
export class SnampLogSettingsComponent implements OnInit {

    private level:string = "DEBUG";
    private allowedTypes:string[] = [];
    private availableTypes:string[] = [];
    private selectedAllTypes:boolean = true;

    private severities:string[] = ["panic", "alert", "critical", "error",
            "warning", "notice", "informational", "debug", "unknown"];

    constructor(private http:ApiClient) {}

    ngOnInit() {
        this.http.get(REST.NOTIFICATIONS_SETTINGS)
            .map((res:Response) => res.json())
            .subscribe((data) => {
                this.level = data["severity"];
                this.allowedTypes = data["notificationTypes"];
                this.selectedAllTypes = (this.allowedTypes.length == 0);
                if (!this.selectedAllTypes) {
                    this.initSelect2();
                    $("#typesSelect").fadeIn("fast");
                }
            });
        this.http.get(REST.NOTIFICATIONS_TYPES)
            .map((res:Response) => res.json())
            .subscribe((data) => {
                this.availableTypes = data;
            })
    }

    private initSelect2():void {
        let _select:any = $("#typesSelect");
        let _thisReference:any = this;
        _select.select2({
            placeholder: "Select types of notifications from the dropdown",
            allowClear: true
        });
        _select.on('change', (e) => {
            _thisReference.onTypeSelect($(e.target).val());
        });
    }

    triggerShowTypes(event:any):void {
        let _select:any = $("#typesSelect");
        this.selectedAllTypes = event;
        if (event == false) {
           this.initSelect2();
            _select.fadeIn("fast");
        } else {
            _select.fadeOut("fast", function(){
                _select.select2("destroy");
            });
        }
    }

    private onTypeSelect(types:string[]):void {
        this.allowedTypes = types;
    }

    saveNotificationSettings():void {
        let _settings:any = {};
        _settings["@type"] = "notificationSettings";
        if (this.selectedAllTypes) {
            _settings["notificationTypes"] = [];
        } else {
            _settings["notificationTypes"] = this.allowedTypes;
        }
        _settings["severity"] = this.level;

        this.http.put(REST.NOTIFICATIONS_SETTINGS, _settings)
            .map((res:Response) => res.text())
            .subscribe((data) => {
                console.log("Notification settings has been stored: ", data);
            })
    }

    isSelected(type:string):boolean {
        return this.allowedTypes.indexOf(type) >= 0;
    }
}

