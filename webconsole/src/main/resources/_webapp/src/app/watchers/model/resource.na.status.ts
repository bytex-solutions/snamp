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
}