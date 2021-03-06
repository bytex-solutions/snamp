import { Injectable } from '@angular/core';
import { Http, Headers, Response } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/observable/throw';
import 'rxjs/add/observable/empty';
import 'rxjs/add/operator/catch';
import { SnampLogService } from "./app.logService";
import { RestClientNotification } from "./model/notifications/rest.client.notification";

@Injectable()
export class ApiClient {
    constructor(private http: Http, private _cookieService: CookieService, private _snampLogService: SnampLogService) {}

    createAuthorizationHeader(): Headers {
        let headers = new Headers();
        headers.append('Authorization', 'Bearer ' + this._cookieService.get("snamp-auth-token"));
        headers.append('Content-type', 'application/json');
        return headers;
    }

    // Functional part of code to log and doing some actions
    private attachProcessing(response: Observable<Response>, emptyfyIfError?:boolean, pushError?:boolean): Observable<Response> {
        return response
            .catch((error: Response | any) => {
                if (error instanceof Response && error.status == 401) {
                    console.debug("Auth is not working.", error);
                    window.location.href = "login.html?tokenExpired=true";
                }
                if (pushError) {
                    this._snampLogService.pushLog(new RestClientNotification(response));
                }
                return emptyfyIfError ? Observable.empty() : error;
            }).do(
                ((data: any) => {
                    console.debug("Received data: ", data)
                }),
                ((error: any) => {
                    console.debug("Error occurred: ", error);
                    $("#overlay").fadeOut();
                    if (pushError) {
                        this._snampLogService.pushLog(new RestClientNotification(response));
                    }
                }),
                (() => {
                    $("#overlay").fadeOut()
                })
            );
    }

    get(url) {
        return this.attachProcessing(this.http.get(url, {
            headers: this.createAuthorizationHeader()
        }), true);
    }

    put(url, data) {
        return this.attachProcessing(this.http.put(url, data, {
            headers: this.createAuthorizationHeader()
        }), true, true);
    }

    post(url, data?) {
        return this.attachProcessing(this.http.post(url, data, {
            headers: this.createAuthorizationHeader()
        }), true, true);
    }

    delete(url) {
        return this.attachProcessing(this.http.delete(url, {
            headers: this.createAuthorizationHeader()
        }), true, true);
    }


    getWithErrors(url) {
        return this.attachProcessing(this.http.get(url, {
            headers: this.createAuthorizationHeader()
        }), false);
    }

    postWithErrors(url, data) {
        return this.attachProcessing(this.http.post(url, data, {
            headers: this.createAuthorizationHeader()
        }), false, true);
    }
}

export class REST {
    public static LOGIN_PATH:string = "/snamp/security/login/username";

    public static ROOT_PATH:string = "/snamp/management";

    public static GROOVY_PATH:string = "/snamp/assets/groovy";

    public static CFG_PATH:string = REST.ROOT_PATH + "/configuration";

    public static GATEWAY_CONFIG:string = REST.CFG_PATH + "/gateway";

    public static RESOURCE_CONFIG:string = REST.CFG_PATH + "/resource";

    public static THREAD_POOL_CONFIG:string = REST.CFG_PATH + "/threadPools";

    public static RGROUP_CONFIG:string = REST.CFG_PATH + "/resourceGroup";

    public static COMPONENTS_MANAGEMENT:string = REST.ROOT_PATH + "/components";

    public static AVAILABLE_GATEWAY_LIST:string = REST.COMPONENTS_MANAGEMENT + "/gateways";

    public static AVAILABLE_RESOURCE_LIST:string = REST.COMPONENTS_MANAGEMENT + "/connectors";

    public static AVAILABLE_SUPERVISORS_LIST:string = REST.COMPONENTS_MANAGEMENT + "/supervisors";

    public static AVAILABLE_ENTITIES_BY_TYPE(entityType: string): string {
        return REST.COMPONENTS_MANAGEMENT + "/" + encodeURIComponent(entityType) + "s";
    }

    public static ENABLE_COMPONENT(componentClass: string, componentType: string): string {
        return REST.COMPONENTS_MANAGEMENT + "/" + encodeURIComponent(componentClass) + "/" + encodeURIComponent(componentType) + "/enable";
    }

    public static DISABLE_COMPONENT(componentClass: string, componentType: string): string {
        return REST.COMPONENTS_MANAGEMENT + "/" + encodeURIComponent(componentClass) + "/" + encodeURIComponent(componentType) + "/disable";
    }

    public static GATEWAY_BY_NAME(name: string): string {
        return REST.GATEWAY_CONFIG + "/" + encodeURIComponent(name);
    }

    public static RESOURCE_BY_NAME(name: string): string {
        return REST.RESOURCE_CONFIG + "/" + encodeURIComponent(name);
    }

    public static RGROUP_BY_NAME(name: string): string {
        return REST.RGROUP_CONFIG + "/" + encodeURIComponent(name);
    }

    public static OVERRIDES_BY_NAME(name: string): string {
        return REST.RESOURCE_BY_NAME(name) + "/overriddenProperties";
    }

    public static GATEWAY_TYPE(name: string): string {
        return REST.GATEWAY_BY_NAME(name) + "/type";
    }

    public static RESOURCE_TYPE(name: string): string {
        return REST.RESOURCE_BY_NAME(name) + "/type";
    }

    public static ENTITY_PARAMETERS_DESCRIPTION(entityClass: string, entityType: string): string {
        return REST.COMPONENTS_MANAGEMENT + "/" + encodeURIComponent(entityClass) + "s/" + encodeURIComponent(entityType) + "/description";
    }

    public static SUBENTITY_PARAMETERS_DESCRIPTION(entityType: string, entityClass: string): string {
        return REST.COMPONENTS_MANAGEMENT + "/connectors/" + encodeURIComponent(entityType) + "/" + encodeURIComponent(entityClass) + "/description";
    }

    public static BINDINGS(gatewayName: string, bindingEntityType: string): string {
        return REST.GATEWAY_BY_NAME(gatewayName) + "/" + encodeURIComponent(bindingEntityType) + "/bindings";
    }

    public static ENTITY_PARAMETERS(entityClass: string, entityName: string, key: string): string {
        return REST.CFG_PATH + "/" + encodeURIComponent(entityClass) + "/" + encodeURIComponent(entityName) + "/parameters/" + encodeURIComponent(key);
    }

    public static RESOURCE_CONNECTION_STRING(entityName: string): string {
        return REST.RESOURCE_BY_NAME(entityName) + "/connectionString";
    }

    public static RESOURCE_DISCOVERY(resourceName: string, entityType:string): string {
        return REST.RESOURCE_BY_NAME(resourceName) + "/discovery/" + entityType; //attributes|events|operations
    }

    // save/remove entity(attribute|event|operation) from the resource|resourceGroup by resource name and entity name
    public static RESOURCE_ENTITY_BY_NAME(type:string, resourceName: string, entityType:string, entityName:string): string {
        console.debug("Trying to make it work on path: ", REST.CFG_PATH + "/" + type + "/" + encodeURIComponent(resourceName) + "/" + entityType + "/" + encodeURIComponent(entityName));
        return REST.CFG_PATH + "/" + type + "/" + encodeURIComponent(resourceName) + "/" + entityType + "/" + encodeURIComponent(entityName);
    }

    public static RGROUP_LIST:string = REST.RGROUP_CONFIG + "/list";

    public static RESOURCE_GROUP(name: string): string {
        return REST.RESOURCE_BY_NAME(name) + "/group";
    }

    public static RESOURCE_ENTITY_BY_TYPE_AND_NAME(entityType: string, name: string, entityName: string): string {
        return REST.RESOURCE_BY_NAME(name) + "/" + encodeURIComponent(entityType) + "/" + encodeURIComponent(entityName);
    }

    public static RESOURCE_SUBENTITY(resourceName: string, entityType: string): string {
        return REST.ROOT_PATH + "/resource/" + encodeURIComponent(resourceName) + "/" + encodeURIComponent(entityType) + "/description";
    }

    // setting resource thread pool by resource name
    public static RESOURCE_THREAD_POOL(resourceName: string): string {
        return REST.RESOURCE_BY_NAME(resourceName) + "/parameters/threadPool";
    }

    // certain tread pool configuration by name
    public static THREAD_POOL_BY_NAME(name:string):string {
        return REST.THREAD_POOL_CONFIG + "/" + name;
    }

    // SNAMP WEB API SECTION (belongs to webconsole module)
    public static ROOT_WEB_API_PATH:string = "/snamp/web/api";

    // web console api (chart related and others)
    public static CHART_DASHBOARD:string = REST.ROOT_WEB_API_PATH + "/charts/settings";

    // web console api (chart related and others)
    public static CHARTS_COMPUTE:string = REST.ROOT_WEB_API_PATH + "/charts/compute";

    // receiving all groups of managed resources
    public static GROUPS_WEB_API:string = REST.ROOT_WEB_API_PATH + "/groups";

    // receiving all resources that belong to certain group
    public static GROUPS_RESOURCE_BY_COMPONENT_NAME(componentName: string): string {
        return REST.GROUPS_WEB_API + "/" +  encodeURIComponent(componentName) +  "/resources/";
    }

    // receiving all the available resources
    public static GROUPS_RESOURCES: string = REST.GROUPS_WEB_API + "/resources/";


    public static CHART_METRICS_BY_COMPONENT(componentName: string): string {
        return REST.GROUPS_WEB_API + "/" + encodeURIComponent(componentName) + "/attributes";
    }

    // attributes for resource list
    public static CHART_METRICS_BY_INSTANCES: string =  REST.GROUPS_WEB_API + "/resources/attributes";

    // web console api (view related and others)
    public static VIEWS_DASHBOARD:string = REST.ROOT_WEB_API_PATH + "/e2e/settings";

    // compute e2e view
    public static COMPUTE_VIEW:string = REST.ROOT_WEB_API_PATH + "/e2e/compute";

    // reset e2e view
    public static RESET_VIEW:string = REST.ROOT_WEB_API_PATH + "/e2e/reset";

    // reset chart view
    public static RESET_ELASTICITY(name:string, classifier:string):string {
        return REST.ROOT_WEB_API_PATH + "/resource-group-watcher/" + encodeURIComponent(name) + "/" + classifier + "/reset";
    }

    // path for storing/receiving full snamp configuration as a json
    public static CURRENT_CONFIG:string = REST.ROOT_PATH + "/configuration";

    // configuration path for supervisors
    public static SUPERVISORS_CONFIG:string = REST.CFG_PATH + "/supervisor";

    // endpoint for certain supervisor
    public static SUPERVISOR_BY_NAME(name: string): string {
        return REST.SUPERVISORS_CONFIG + "/" + encodeURIComponent(name);
    }

    // all supervisors statuses
    public static SUPERVISORS_STATUS:string = REST.ROOT_WEB_API_PATH + "/resource-group-watcher/groups/status";

    // get recommendation for policy (see OpRange class)
    public static SUPERVISOR_POLICY_RECOMMENDATION(supervisorName:string, policyName:string):string {
        return REST.ROOT_WEB_API_PATH + "/resource-group-watcher/" + encodeURIComponent(supervisorName)
            + "/scaling-policies/attribute-based/" + encodeURIComponent(policyName) + "/recommendation";
    }

    // notification settings
    public static NOTIFICATIONS_SETTINGS:string = REST.ROOT_WEB_API_PATH + "/notifications/settings";

    // list of the available notifications types
    public static NOTIFICATIONS_TYPES:string = REST.ROOT_WEB_API_PATH + "/notifications/types";
}
