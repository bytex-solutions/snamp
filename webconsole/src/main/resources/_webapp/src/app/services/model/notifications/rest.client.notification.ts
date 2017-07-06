import { AbstractNotification } from "./abstract.notification";

export class RestClientNotification extends AbstractNotification {

    private path:string = "";
    private error:any = undefined;

    constructor(path:string, error:any) {
        super();
        this.path = path;
        this.error = error;
        this.type = AbstractNotification.REST;
        this.level = "error";
    }

    htmlDetails(): string {
        let _details:string = "<strong>PATH:</strong>" + this.path + "<br/>";
        if (this.error != undefined) {
            _details += "<strong>Error:</strong>" + this.error.toSource() + "<br/>";
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