import { Component } from '@angular/core';
import { UserProfileService } from "../../services/app.user.profile";
import {ApiClient, REST} from "../../services/app.restClient";
import { Response } from "@angular/http";

@Component({
    moduleId: module.id,
    selector: 'profile',
    templateUrl: './profile.component.html',
})
export class UserProfileComponent  {
    username:string;

    constructor(private profileService: UserProfileService, private http: ApiClient) {
        this.http.get(REST.LOGIN_PATH)
            .map((res: Response) => res.text())
            .subscribe(res => {
                    this.username = res;
                    // until we got the very first authenticated response from the service -
                    // all the layout will be hided with overlay
                    $('#overlay').fadeOut();
                    $('#mainBodyContainer').fadeIn();
                },
                err => {
                    if (err.status == 500) {
                        window.location.href = "login.html?tokenExpired=true";
                    }
                }
            );
    }

    getProfilePicture():string {
        return "assets/img/" + (this.isAdmin() ? "adminUser.png" : "anyUser.png");
    }

    isAdmin():boolean {
        return this.profileService.isUserHasAdminRole();
    }
}
