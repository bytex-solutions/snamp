import { ChartData } from "./abstract.data";

export class AttributeChartData extends ChartData {
    private _attributeName: string;
    private _attributeValue: any;
    private _resourceName: string;

    get attributeName(): string {
        return this._attributeName;
    }

    set attributeName(value: string) {
        this._attributeName = value;
    }

    get attributeValue(): any {
        return this._attributeValue;
    }

    set attributeValue(value: any) {
        this._attributeValue = value;
    }

    get resourceName(): string {
        return this._resourceName;
    }

    set resourceName(value: string) {
        this._resourceName = value;
    }

    private static isNumber(n):boolean {
        return !isNaN(parseFloat(n)) && isFinite(n);
    }

    private static toFixed(n):any {
        return AttributeChartData.isNumber(n) ? n.toFixed(2) : n;
    }

    fillFromJSON(_json: any): void {
        if (_json["attributeName"] != undefined) {
            this.attributeName = _json["attributeName"];
        }
        if (_json["attributeValue"] != undefined) {
            this.attributeValue = AttributeChartData.toFixed(_json["attributeValue"]);
        }
        if (_json["resourceName"] != undefined) {
            this.resourceName = _json["resourceName"];
        }
    }
}
