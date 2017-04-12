import { MalfunctionStatus } from './malfunction.status';

export class ConnectionProblem extends MalfunctionStatus {
    public code:number = 1;
    public ioException:string = "";

    public represent():string {
        return "Connection problems detected. Caused by " + this.ioException;
    }

    public getShortDescription():string {
        return "Connection problems";
    }

    public htmlDetails():string {
        let _details:string = "";
         _details += "<strong>Name: </strong>" + this.name + "<br/>";
         _details += "<strong>Resource: </strong>" + this.resourceName + "<br/>";
         _details += "<strong>Critical: </strong>" + this.critical + "<br/>";
         _details += "<strong>IO Exception: </strong>" + this.ioException + "<br/>";
         return _details;
    }
}