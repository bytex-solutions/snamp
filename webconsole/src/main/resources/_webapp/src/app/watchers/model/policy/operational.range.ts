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
        return "[" + this.begin + "‥" + this.end + "]";
    }

    public static fromString(str:string):OpRange {
        let splits:string[] = str.split("‥");
        let begin = Number.parseFloat(splits[0].substr(1));
        let end = Number.parseFloat(splits[1].substr(0, splits[1].indexOf("]") - 1));
        return new OpRange(begin, end);
    }
}