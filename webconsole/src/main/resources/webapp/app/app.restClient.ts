import {Injectable} from '@angular/core';
import {Http, Headers, Response} from '@angular/http';
import {CookieService} from 'angular2-cookie/core';
import { Observable } from 'rxjs/Observable';

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
      // In a real world app, we might use a remote logging infrastructure
      let errMsg: string;
      if (error instanceof Response && error.status == 401) {
            console.log("Auth is not working.", error);
            window.location.href = "login.html?tokenExpired=true";
      } else {
        console.error(error.message ? error.message : error.toString());
        return Observable.throw(errMsg);
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

  delete(url) {
    return this.http.delete(url, {
      headers: this.createAuthorizationHeader()
    }).catch(this.handleError);
  }
}