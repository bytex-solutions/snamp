import {Injectable} from '@angular/core';
import {Http, Headers} from '@angular/http';
import {CookieService} from 'angular2-cookie/core';

@Injectable()
export class ApiClient {
constructor(private http: Http, private _cookieService:CookieService) {}
  createAuthorizationHeader():Headers {
    let headers = new Headers();
    headers.append('Authorization', 'Bearer ' +
      this._cookieService.get("snamp-auth-token"));
    return headers;
  }

  get(url) {
    return this.http.get(url, {
      headers: this.createAuthorizationHeader()
    });
  }

  post(url, data) {
    return this.http.post(url, data, {
      headers: this.createAuthorizationHeader()
    });
  }

  delete(url) {
    return this.http.delete(url, {
      headers: this.createAuthorizationHeader()
    });
  }
}