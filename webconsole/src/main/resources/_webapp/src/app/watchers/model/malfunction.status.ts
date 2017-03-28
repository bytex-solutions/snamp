import { HealthStatus } from './health.status';

export abstract class MalfunctionStatus extends HealthStatus {
    public critical:boolean = false;

    public isCritical():boolean {
        return this.critical;
    }
}