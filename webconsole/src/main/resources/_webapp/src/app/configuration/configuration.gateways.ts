import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Gateway } from './model/model.gateway';
import { KeyValue } from './model/model.entity';
import { Binding } from './model/model.binding';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

import 'select2';

@Component({
  moduleId: module.id,
  templateUrl: './templates/gateways.html'
})
export class GatewaysComponent implements OnInit {

   gateways:Gateway[] = [];
   activeGateway:Gateway;
   oldTypeValue:string = "";
   http:ApiClient;
   availableGateways :any[] = [];

   constructor(apiClient: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        this.http = apiClient;
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
                  $("#select2-gatewaySelection-container").html(this.activeGateway.name);
                }
                this.oldTypeValue = this.activeGateway.type;
            });

        // Get all the available bundles that belong to Gateways
        this.http.get(REST.AVAILABLE_GATEWAY_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => this.availableGateways = data);
    }

    initSelectionComponent() {
      $("#gatewaySelection").select2('destroy');
      $("#gatewaySelection").select2();
    }

    ngAfterViewInit() {
       var _this = this;
       $(document).ready(function() {
          $("#gatewaySelection").select2();
          $("#gatewaySelection").on('change', (e) => {
            _this.selectCurrentlyActiveGateway($(e.target).val());
          });
        });
    }

    selectCurrentlyActiveGateway(gatewayName:string) {
        let selection:Gateway;
        for (let i = 0; i < this.gateways.length; i++) {
          if (this.gateways[i].name == gatewayName) {
            selection = this.gateways[i];
          }
        }
        this.activeGateway = selection;
        this.oldTypeValue = selection.type;
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
