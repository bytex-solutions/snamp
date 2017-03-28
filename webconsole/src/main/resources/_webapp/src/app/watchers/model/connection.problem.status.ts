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
}