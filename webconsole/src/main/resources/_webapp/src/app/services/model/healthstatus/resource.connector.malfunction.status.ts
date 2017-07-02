import { MalfunctionStatus } from './malfunction.status';

export class ResourceConnectorMalfunction extends MalfunctionStatus {
    public jmxError:string = "";

    public represent():string {
        return "Jmx error occurred: " + this.jmxError;
    }

    public getShortDescription():string {
        return "Resource is not available";
    }

    public htmlDetails():string {
        let _details:string = "";
         _details += "<strong>Name: </strong>" + this.name + "<br/>";
         _details += "<strong>Level: </strong>" + this.level + "<br/>";
         _details += "<strong>JMX Exception: </strong>" + this.jmxError + "<br/>";
         if (this.serverTimestamp.length > 0) {
             _details += "<strong>Server timestamp: </strong>" + this.serverTimestamp + "<br/>";
         }
         if (this.serverDetails.length > 0) {
             _details += "<strong>Details: </strong>" + this.serverDetails + "<br/>";
         }
         return _details;
    }
}