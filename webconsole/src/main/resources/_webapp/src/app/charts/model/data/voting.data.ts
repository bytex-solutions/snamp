import { ChartData } from "./abstract.data";

export class VotingData extends ChartData {

    private _votingResult:number;
    private _castingVote:number;

    get votingResult(): number {
        return this._votingResult;
    }

    set votingResult(value: number) {
        this._votingResult = value;
    }

    get castingVote(): number {
        return this._castingVote;
    }

    set castingVote(value: number) {
        this._castingVote = value;
    }

    fillFromJSON(_json: any): void {
        if (_json["votingResult"] != undefined) {
            this.votingResult = _json["votingResult"];
        }
        if (_json["castingVote"] != undefined) {
            this.castingVote = _json["castingVote"];
        }
    }
}