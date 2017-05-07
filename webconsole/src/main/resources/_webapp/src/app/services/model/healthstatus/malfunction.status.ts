import { HealthStatus } from './health.status';

export abstract class MalfunctionStatus extends HealthStatus {

    private _level:string = "";

    get level(): string {
        return this._level;
    }

    set level(value: string) {
        this._level = value;
    }

    getNotificationLevel(): string {
       return this.level;
    }
}