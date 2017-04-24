import { MalfunctionStatus } from './malfunction.status';

export class ResourceIsNotAvailable extends MalfunctionStatus {
    public code:number = 2;
    public jmxError:string = "";

    public represent():string {
        return "Resource " + this.resourceName + " is not available. Caused by: " + this.jmxError;
    }

    public getShortDescription():string {
        return "Resource is not available";
    }

    public htmlDetails():string {
        let _details:string = "";
         _details += "<strong>Name: </strong>" + this.name + "<br/>";
         _details += "<strong>Resource: </strong>" + this.resourceName + "<br/>";
         _details += "<strong>Critical: </strong>" + this.critical + "<br/>";
         _details += "<strong>JMX Exception: </strong>" + this.jmxError + "<br/>";
         if (this.serverDetails.length > 0) {
             _details += "<strong>Details: </strong>" + this.details + "<br/>";
         }
         return _details;
    }
}