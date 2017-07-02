import { AbstractChart } from './abstract.chart';

export class Dashboard {
    public type:string = "dashboardOfCharts";
    public charts:AbstractChart[] = [];
    public groups:string[] = [];

    constructor(){};

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        let _charts:any[] = [];
        for (let i = 0; i < this.charts.length; i++) {
            _charts.push(this.charts[i].toJSON());
        }
        _value["charts"] = _charts;
        _value["groups"] = this.groups;
        return _value;
    }
}