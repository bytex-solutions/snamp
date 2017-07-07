import { AbstractNotification } from "./abstract.notification";
import { Response } from '@angular/http';

export class RestClientNotification extends AbstractNotification {

    private error:any = undefined;

    constructor(error:any) {
        super();
        this.error = error;
        this.type = AbstractNotification.REST;
        this.level = "error";
    }

    htmlDetails(): string {
         let _details:string = "";
        _details += "<strong>Error type: </strong>server request error<br/>";
        _details += "<strong>Timestamp: </strong>" + this.timestamp + "<br/>";
        if (this.error != undefined ) {
            if (this.error instanceof Response) {
                 _details += "<strong>Path:</strong>" + (<Response>this.error).url + "<br/>";
                 _details += "<strong>Status code:</strong>" + (<Response>this.error).status + "<br/>";
                 _details += "<strong>Status text:</strong>" + (<Response>this.error).statusText + "<br/>";
            } else {
                _details += "<strong>Error (json representation):</strong>" + JSON.stringify(this.error) + "<br/>";
            }
        }
        return _details;
    }

    shortDescription(): string {
        return "Rest request failed";
    }

    fillFromJson(json: any): void {
        // do nothing
    }

}