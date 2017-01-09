import { Injectable } from '@angular/core';
import { Http, Headers, Response } from '@angular/http';
import { CookieService } from 'angular2-cookie/core';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/catch';

@Injectable()
export class ApiClient {
constructor(private http: Http, private _cookieService:CookieService) {}

  createAuthorizationHeader():Headers {
    let headers = new Headers();
    headers.append('Authorization', 'Bearer ' +
      this._cookieService.get("snamp-auth-token"));
    headers.append('Content-type', 'application/json');
    return headers;
  }

    private handleError (error: Response | any) {
      $('#overlay').fadeOut();
      // In a real world app, we might use a remote logging infrastructure
      let errMsg: string;
      if (error instanceof Response && error.status == 401) {
            console.log("Auth is not working.", error);
            window.location.href = "login.html?tokenExpired=true";
      } else {
         return Observable.throw(error);
      }
    }

  get(url) {
    return this.http.get(url, {
      headers: this.createAuthorizationHeader()
    }).catch(this.handleError)
  }

  put(url, data) {
    return this.http.put(url, data, {
      headers: this.createAuthorizationHeader()
    }).catch(this.handleError);
  }

  post(url, data) {
    return this.http.post(url, data, {
      headers: this.createAuthorizationHeader()
    }).catch(this.handleError);
  }

  delete(url) {
    return this.http.delete(url, {
      headers: this.createAuthorizationHeader()
    }).catch(this.handleError);
  }
}

export class REST {
    public static ROOT_PATH = "/snamp/management";
    public static CFG_PATH = REST.ROOT_PATH + "/configuration";

    public static GATEWAY_CONFIG = REST.CFG_PATH + "/gateway";

    public static RESOURCE_CONFIG = REST.CFG_PATH + "/resource";

    public static RGROUP_CONFIG = REST.CFG_PATH + "/resourceGroup";

    public static AVAILABLE_GATEWAY_LIST = REST.ROOT_PATH + "/gateway/list";

    public static AVAILABLE_RESOURCE_LIST = REST.ROOT_PATH + "/resource/list";

    public static AVAILABLE_COMPONENT_LIST = REST.ROOT_PATH + "/components";

    public static AVAILABLE_ENTITIES_BY_TYPE(entityType:string):string {
        return REST.ROOT_PATH + "/" + entityType + "/list";
    }

    public static ENABLE_COMPONENT(componentClass:string, componentType:string):string {
        return REST.ROOT_PATH + "/" + componentClass + "/" + componentType + "/enable";
    }

    public static DISABLE_COMPONENT(componentClass:string, componentType:string):string {
        return REST.ROOT_PATH + "/" + componentClass + "/" + componentType + "/disable";
    }

    public static GATEWAY_BY_NAME(name:string):string {
        return REST.GATEWAY_CONFIG + "/" + name;
    }

    public static RESOURCE_BY_NAME(name:string):string {
        return REST.RESOURCE_CONFIG + "/" + name;
    }

    public static ENTITY

    public static GATEWAY_TYPE(name:string):string {
        return REST.GATEWAY_BY_NAME(name) + "/type";
    }

    public static RESOURCE_TYPE(name:string):string {
        return REST.RESOURCE_BY_NAME(name) + "/type";
    }

    public static ENTITY_PARAMETERS_DESCRIPTION(entityClass:string, entityType:string):string {
        return REST.ROOT_PATH + "/" + entityClass + "/" + entityType + "/configuration";
    }

    public static SUBENTITY_PARAMETERS_DESCRIPTION(entityType:string, entityClass:string ):string {
        return REST.ROOT_PATH + "/resource/" + entityType + "/" + entityClass + "/configuration";
    }

    public static BINDINGS(gatewayName:string, bindingEntityType:string):string {
        return REST.GATEWAY_BY_NAME(gatewayName) + "/" + bindingEntityType + "/bindings";
    }

    public static ENTITY_PARAMETERS(entityClass:string, entityName:string, key:string):string {
        return REST.CFG_PATH + "/" + entityClass + "/" + entityName + "/parameters/" + key;
    }

    public static RESOURCE_CONNECTION_STRING(entityName:string):string {
        return REST.RESOURCE_BY_NAME(entityName) + "/connectionString";
    }

    public static RGROUP_LIST = REST.RGROUP_CONFIG + "/list";

    public static RESOURCE_GROUP(name:string):string {
        return REST.RESOURCE_BY_NAME(name) + "/group";
    }

    public static RESOURCE_ENTITY_BY_TYPE_AND_NAME(entityType:string, name:string, entityName:string):string {
        return REST.RESOURCE_BY_NAME(name) + "/" + entityType + "/" + entityName;
    }

    public static RESOURCE_SUBENTITY(resourceName:string, entityType:string):string {
        return REST.ROOT_PATH + "/resource/" + resourceName + "/" + entityType + "/configuration";
    }
}
