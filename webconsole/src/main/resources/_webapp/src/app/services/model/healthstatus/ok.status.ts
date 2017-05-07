import { HealthStatus } from './health.status';

export class OkStatus extends HealthStatus {

    getNotificationLevel(): string {
        return "ok";
    }

    public represent():string {
        return "Everything is fine";
    }

    public getShortDescription():string {
        return "n/a";
    }

    public htmlDetails():string {
        let _details:string =  "<strong>Everything is fine</strong></br>";
        if (this.serverTimestamp.length > 0) {
            _details += "<strong>Server timestamp: </strong>" + this.serverTimestamp + "<br/>";
        }
        return _details;
    }
}