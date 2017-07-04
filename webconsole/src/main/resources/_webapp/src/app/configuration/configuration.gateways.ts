import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Gateway } from './model/model.gateway';
import { Response } from '@angular/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

import 'select2';
import { isNullOrUndefined } from "util";

@Component({
  moduleId: module.id,
  templateUrl: './templates/gateways.html'
})
export class GatewaysComponent implements OnInit {

   gateways:Gateway[] = [];
   activeGateway:Gateway = undefined;
   oldTypeValue:string = "";
   availableGateways :any[] = [];

   private static select2Id:string = "#gatewaySelection";
   private static selectionId:string = "#select2-gatewaySelection-container";

   constructor(private http: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        overlay.defaultViewContainer = vcRef;
   }

    ngOnInit() {
        // Get all configured gateways from the server
        this.http.get(REST.GATEWAY_CONFIG)
            .map((res: Response) => res.json())
            .subscribe(data => {
                for (let key in data) {
                    this.gateways.push(new Gateway(this.http, key, data[key]['type'], data[key]['parameters']))
                }
                if (this.gateways.length > 0) {
                    this.activeGateway = this.gateways[0];
                    // dirty hack to make select element work
                    let _thisReference = this;
                    $(document).ready(function () {
                        $(GatewaysComponent.select2Id).select2();
                        $(GatewaysComponent.select2Id).on('change', (e) => {
                            _thisReference.selectCurrentlyActiveGateway($(e.target).val());
                        });
                    });
                    $(GatewaysComponent.selectionId).html(this.activeGateway.name);
                    this.oldTypeValue = this.activeGateway.type;
                }
            });

        // Get all the available bundles that belong to Gateways
        this.http.get(REST.AVAILABLE_GATEWAY_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => this.availableGateways = data);
    }

    dispatchNewGateway(newGateway:Gateway):void {
        let _thisReference = this;
        if ($(GatewaysComponent.select2Id).data('select2')) {
            $(GatewaysComponent.select2Id).select2('destroy');
        }
        $(GatewaysComponent.select2Id).select2({
            placeholder: "Select gateway",
            width: '100%',
            allowClear: true
        });
        $(GatewaysComponent.select2Id).on('change', (e) => {
            _thisReference.selectCurrentlyActiveGateway($(e.target).val());
        });

        if (this.gateways.length > 0) {
            this.activeGateway = newGateway;
            $(GatewaysComponent.selectionId).html(this.activeGateway.name);
        }
    }

    ngAfterViewInit() {}

    selectCurrentlyActiveGateway(gatewayName:string):void {
        let selection:Gateway;
        for (let i = 0; i < this.gateways.length; i++) {
          if (this.gateways[i].name == gatewayName) {
            selection = this.gateways[i];
          }
        }
        this.activeGateway = selection;
        if (!isNullOrUndefined(selection)) {
            this.oldTypeValue = selection.type;
        }
    }

    changeType(event:any) {
         let dialog = this.modal.confirm()
            .isBlocking(true)
            .keyboard(27)
            .message("If you change gateway type - all resources will be removed. Are you sure?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                  .then((response) => {
                    this.oldTypeValue = event.target.value;
                    this.http.put(REST.GATEWAY_TYPE(this.activeGateway.name), event.target.value);
                    return response;
                  })
                  .catch(() => {
                    this.activeGateway.type = this.oldTypeValue;
                    return false;
                  });
              });
    }
}
