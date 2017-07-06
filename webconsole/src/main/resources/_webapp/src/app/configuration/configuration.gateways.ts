import {ChangeDetectorRef, Component, OnInit, ViewContainerRef} from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Gateway } from './model/model.gateway';
import { Response } from '@angular/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { Overlay } from 'angular2-modal';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';

import 'select2';
import { isNullOrUndefined } from "util";
import { ActivatedRoute } from "@angular/router";

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

   constructor(private http: ApiClient, overlay: Overlay, vcRef: ViewContainerRef,
               public modal: Modal, private cd: ChangeDetectorRef, private route: ActivatedRoute) {
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
                    $(GatewaysComponent.select2Id).val(this.activeGateway.name).trigger('change.select2');
                    this.oldTypeValue = this.activeGateway.type;
                }

                this.route
                    .queryParams
                    .subscribe(params => {
                        // Defaults to 0 if no query param provided.
                        let gatewayName:string = params['gateway'] || "";
                        if (!isNullOrUndefined(this.activeGateway) && gatewayName.length > 0
                            && gatewayName != this.activeGateway.name && this.gateways.length > 0) {
                            for (let i = 0; i < this.gateways.length; i++) {
                                if (this.gateways[i].name == gatewayName) {
                                    this.setActiveGateway(this.gateways[i]);
                                    break;
                                }
                            }
                        }
                    });
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

        if (this.gateways.length > 0) {
            this.setActiveGateway(newGateway, true);

            this.cd.detectChanges(); // draw my select pls!

            $(GatewaysComponent.select2Id).select2({
                placeholder: "Select gateway",
                width: '100%'
            });
            $(GatewaysComponent.select2Id).on('change', (e) => {
                _thisReference.selectCurrentlyActiveGateway($(e.target).val());
            });
            $(GatewaysComponent.select2Id).val(this.activeGateway.name).trigger('change.select2');
        }
    }

    private setActiveGateway(gateway:Gateway, setURL?:boolean):void {
        this.activeGateway = gateway;
        this.oldTypeValue = gateway.type;
        if (history.pushState && setURL) {
            let newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + window.location.hash.split("?")[0] + "?gateway=" + gateway.name;
            window.history.pushState({path:newurl},'',newurl);
        }
        $(GatewaysComponent.select2Id).val(this.activeGateway.name).trigger('change.select2');
    }

    removeGateway():void {
        this.modal.confirm()
            .isBlocking(true)
            .className(<VEXBuiltInThemes>'default')
            .keyboard(27)
            .message("Gateway " + this.activeGateway.name + " is being deleted. Are You sure?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this.http.delete(REST.GATEWAY_BY_NAME(this.activeGateway.name))
                            .subscribe(() => {
                                for (let i = 0; i < this.gateways.length; i++) {
                                    if (this.gateways[i].name == this.activeGateway.name) {
                                        this.gateways.splice(i, 1);
                                        if (this.gateways.length > 0) {
                                            this.setActiveGateway(this.gateways[0], true);

                                            if ($(GatewaysComponent.select2Id).data('select2')) {
                                                $(GatewaysComponent.select2Id).select2('destroy');
                                            }

                                            this.cd.detectChanges(); // draw my select pls!

                                            $(GatewaysComponent.select2Id).select2({
                                                placeholder: "Select gateway",
                                                width: '100%'
                                            });
                                            let _thisReference = this;
                                            $(GatewaysComponent.select2Id).on('change', (e) => {
                                                _thisReference.selectCurrentlyActiveGateway($(e.target).val());
                                            });
                                            $(GatewaysComponent.select2Id).val(this.activeGateway.name).trigger('change.select2');

                                        }
                                        break;
                                    }
                                }
                            });
                        return response;
                    })
                    .catch(() => {
                        return false;
                    });
            });
    }

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
