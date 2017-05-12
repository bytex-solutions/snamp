import { ChartData } from "./abstract.data";

export class ResourceCountData extends ChartData {
    private _count: number;
    private _serverTs:string;

    get count(): number {
        return this._count;
    }

    set count(value: number) {
        this._count = value;
    }

    get serverTs(): string {
        return this._serverTs;
    }

    set serverTs(value: string) {
        this._serverTs = value;
    }

    fillFromJSON(_json: any): void {
        if (_json["count"] != undefined) {
            this.count = _json["count"];
        }
        if (_json["timeStamp"] != undefined) {
            this.serverTs = _json["timeStamp"];
        }
    }
}
