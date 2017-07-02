import { ChartData } from "./abstract.data";

export class ScalingData extends ChartData {
    private _value: number; // metric value
    private _type:string; // metric name
    private _serverTs:string; // server timestamp

    get serverTs(): string {
        return this._serverTs;
    }

    set serverTs(value: string) {
        this._serverTs = value;
    }

    get value(): number {
        return this._value;
    }

    set value(value: number) {
        this._value = value;
    }

    get type(): string {
        return this._type;
    }

    set type(value: string) {
        this._type = value;
    }

    fillFromJSON(_json: any): void {
        if (_json["timeStamp"] != undefined) {
            this.serverTs = _json["timeStamp"];
        }
        if (_json["type"] != undefined) {
            this.type = _json["type"];
        }
        if (_json["value"] != undefined) {
            this.value = _json["value"];
        }
    }
}
