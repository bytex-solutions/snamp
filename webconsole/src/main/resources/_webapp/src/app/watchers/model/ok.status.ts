import { HealthStatus } from './health.status';

export class OkStatus extends HealthStatus {
    public static CODE:number = 0;

    public isCritical():boolean {
        return false;
    }

    public represent():string {
        return "Everything is fine";
    }
}