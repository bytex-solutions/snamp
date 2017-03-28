import { MalfunctionStatus } from './malfunction.status';

export class ResourceIsNotAvailable extends MalfunctionStatus {
    public static CODE:number = 2;
    public jmxError:string = "";

    public represent():string {
        return "Resource " + this.resourceName + " is not available. Caused by: " + this.jmxError;
    }
}