import {isNullOrUndefined} from "util";
export class OpRange {

    private _begin:number;
    private _end:number;

    constructor(begin?:number, end?:number) {
        if (!isNullOrUndefined(begin)) {
            this.begin = begin;
        }
        if (!isNullOrUndefined(end)) {
            this.end = end;

        }
    }

    get begin(): number {
        return this._begin;
    }

    set begin(value: number) {
        this._begin = value;
    }

    get end(): number {
        return this._end;
    }

    set end(value: number) {
        this._end = value;
    }

    public toString():string {
        return "[" + this.begin + ".." + this.end + "]";
    }

    public fromString(str:string):void {
        let re1 = /.*\[\s+(.*)\s+\.\..*/;
        this.begin = Number.parseFloat(str.replace(re1, "$1"));
        let re2 = /.*\\.\.s+(.*)\s+\].*/;
        this.end = Number.parseFloat(str.replace(re2, "$1"));
    }
}