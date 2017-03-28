import { HealthStatus } from './health.status';

export class OkStatus extends HealthStatus {
    public code:number = 0;

    public isCritical():boolean {
        return false;
    }

    public represent():string {
        return "Everything is fine";
    }

    public getShortDescription():string {
        return "n/a";
    }
}