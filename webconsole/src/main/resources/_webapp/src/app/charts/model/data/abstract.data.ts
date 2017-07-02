export abstract class ChartData {
    public timestamp:Date = new Date();
    public chartType:string;

    abstract fillFromJSON(json:any):void;
}