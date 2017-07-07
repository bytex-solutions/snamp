import { Injectable } from '@angular/core';
import { CookieService } from 'angular2-cookie/core';
import { JwtHelper } from 'angular2-jwt';

@Injectable()
export class UserProfileService {
    private jwtHelper: JwtHelper = new JwtHelper();

    constructor(private _cookieService: CookieService) {}

    public decodeProfile():any {
        return this.jwtHelper.decodeToken(this._cookieService.get("snamp-auth-token"));
    }

    public isUserHasAdminRole():boolean {
        return this.decodeProfile()["roles"].indexOf("admin") >= 0;
    }

    public isUserHasManagerOrAdminRole():boolean {
        return (this.decodeProfile()["roles"].indexOf("admin") >= 0)
            || (this.decodeProfile()["roles"].indexOf("manager") >= 0);
    }

    public getUserName():string {
        return this.decodeProfile()["sub"];
    }
}