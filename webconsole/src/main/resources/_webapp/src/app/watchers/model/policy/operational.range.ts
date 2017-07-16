import { isNullOrUndefined } from "util";

export class OpRange {

    private _begin:number = 0.0;
    private _end:number = 1.0;

    public isBeginInfinite:boolean = false;
    public isEndInfinite:boolean = false;

    public isBeginIncluding:boolean = true;
    public isEndIncluding:boolean = true;

    private static DELIMITER:string = "‥";
    private static MINUS_INFINITE:string = "-∞";
    private static PLUS_INFINITE:string = "+∞";

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

    private getLeftBracket():string {
        return (this.isBeginIncluding && !this.isBeginInfinite) ? "[" : "(";
    }

    private getRightBracket():string {
        return (this.isEndIncluding && !this.isEndInfinite) ? "]" : ")";
    }

    private getBeginString():string {
        if (this.isBeginInfinite || isNullOrUndefined(this.begin)) {
            return OpRange.MINUS_INFINITE;
        } else {
            return this.begin.toString();
        }
    }

    private getEndString():string {
        if (this.isEndInfinite || isNullOrUndefined(this.end)) {
            return OpRange.PLUS_INFINITE;
        } else {
            return this.end.toString();
        }
    }

    public toString():string {
        return this.getLeftBracket() + this.getBeginString() + OpRange.DELIMITER + this.getEndString() + this.getRightBracket();
    }

    public static fromString(str:string):OpRange {
        console.debug("String to parse operational: ", str);
        let splits:string[] = str.split(OpRange.DELIMITER);
        let _result:OpRange = new OpRange(0.0, 0.0);

        // begining parsing
        if (splits[0].substr(0,1) == "[") {
            _result.isBeginInfinite = false;
            _result.isBeginIncluding = true;
        } else {
            _result.isBeginIncluding = false;
        }
        let beginStr:string = splits[0].substr(1);
        if (beginStr == OpRange.MINUS_INFINITE) {
            _result.isBeginInfinite = true;
        } else {
            _result.begin = Number.parseFloat(beginStr);
        }

        // ending parse
        if (splits[1].substr(splits[1].length - 1, 1) == "]") {
            _result.isEndInfinite = false;
            _result.isEndIncluding = true;
        } else {
            _result.isEndIncluding = false;
        }
        let endStr:string = splits[1].substr(0, splits[1].length - 1);
        if (endStr == OpRange.PLUS_INFINITE) {
            _result.isEndInfinite = true;
        } else {
            _result.end = Number.parseFloat(endStr);
        }

        return _result;
    }
}