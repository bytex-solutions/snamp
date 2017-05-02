import { ChartData } from "./abstract.data";
import { HealthStatus } from "../../../services/model/healthstatus/health.status";
import { StatusFactory } from "../../../services/model/healthstatus/factory";

export class HealthStatusChartData extends ChartData {
    private _summary: boolean;
    private _status: HealthStatus;
    private _name: string;

    get summary(): boolean {
        return this._summary;
    }

    set summary(value: boolean) {
        this._summary = value;
    }

    get status(): HealthStatus {
        return this._status;
    }

    set status(value: HealthStatus) {
        this._status = value;
    }

    get name(): string {
        return this._name;
    }

    set name(value: string) {
        this._name = value;
    }

    fillFromJSON(_json: any): void {
        if (_json["summary"] != undefined) {
            this.summary = _json["summary"];
        }
        if (_json["name"] != undefined) {
            this.name = _json["name"];
        }
        if (_json["status"] != undefined) {
            this.status = StatusFactory.healthStatusFromJSON(this.name, _json["status"]);
        }
    }
}
