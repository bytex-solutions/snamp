import { MalfunctionStatus } from "./malfunction.status";

export class CommonHealthStatus extends MalfunctionStatus {

    public additionalFields:any = {};

    represent(): string {
        return "Cluster status received: " + this.innerType;
    }

    getShortDescription(): string {
        return "Cluster status: " + this.innerType;
    }

    htmlDetails(): string {
        let _details:string = "";
        _details += "<strong>Name: </strong>" + this.name + "<br/>";
        _details += "<strong>Resource: </strong>" + this.resourceName + "<br/>";
        _details += "<strong>Critical: </strong>" + this.critical + "<br/>";
        if (this.serverDetails.length > 0) {
            _details += "<strong>Details: </strong>" + this.details + "<br/>";
        }
        if (!$.isEmptyObject(this.additionalFields)) {
            _details += "<strong>Additional fields: </strong><br/>";
            for (let key in this.additionalFields) {
                _details += key + ": " + this.additionalFields[key] + "<br/>";
            }
        }
        return _details;
    }
}