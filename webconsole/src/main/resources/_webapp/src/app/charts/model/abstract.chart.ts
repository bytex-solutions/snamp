import { Observable } from 'rxjs/Observable';
import { ChartData } from "./data/abstract.data";

const Chart = require('chart.js');

export abstract class AbstractChart {
    public static VBAR:string = "verticalBarChartOfAttributeValues";
    public static PIE:string = "pieChartOfAttributeValues";
    public static HBAR:string = "horizontalBarChartOfAttributeValues";
    public static PANEL:string = "panelOfAttributeValues";
    public static LINE:string = "lineChartOfAttributeValues";
    public static HEALTH_STATUS:string = "groupHealthStatus";
    public static RESOURCE_COUNT:string = "numberOfResourcesInGroup";

    // map chartjs types to current hierarchy types
    public static TYPE_MAPPING:{[key:string]:string} = {
        'doughnut':         AbstractChart.PIE,
        'horizontalBar':    AbstractChart.HBAR,
        'bar':              AbstractChart.VBAR,
        'line':             AbstractChart.LINE,
        'panel':            AbstractChart.PANEL,
        'statuses':         AbstractChart.HEALTH_STATUS,
        'resources':        AbstractChart.RESOURCE_COUNT
    };

    public name:string;
    public preferences:{ [key: string]: any } = { };
    public id:string = "chart" + GUID.newGuid();
    public chartData: ChartData[] = [];
    public abstract toJSON():any;

    private _stopUpdate:boolean;

    private _pausedTime:Date;

    public getGroupName():string {
        return this.preferences["groupName"];
    }

    public setGroupName(groupName:string):void {
        this.preferences["groupName"] = groupName;
    }

    protected setCol(n:number): void {
        this.preferences["gridcfg"]['col'] = n;
    }

    protected setRow(n:number): void {
        this.preferences["gridcfg"]['row'] = n;
    }

    protected setSizeX(n:number): void {
        this.preferences["gridcfg"]['sizex'] = n;
    }

    protected setSizeY(n:number): void {
        this.preferences["gridcfg"]['sizey'] = n;
    }

    public get updateStopped():boolean {
        return this._stopUpdate;
    }

    public toggleUpdate():void {
        this._stopUpdate = !this._stopUpdate;
        this.pausedTime = new Date();
    }

    get pausedTime(): Date {
        return this._pausedTime;
    }

    set pausedTime(value: Date) {
        this._pausedTime = value;
    }

    abstract get type():string;

    constructor() {
        this.preferences["gridcfg"] = {};
        this.preferences["gridcfg"]['dragHandle'] = '.handle';
        this.setCol(1);
        this.setRow(1);
        this.setSizeX(2);
        this.setSizeY(2);
        this._stopUpdate = false;
        this.pausedTime = new Date();
        Chart.defaults.global.maintainAspectRatio = false;
    }

    // different types of charts should be rendered in different ways
    public abstract draw():void;

    // when new value comes - we should process it. see abstract.chart.attributes.values as a default implementation
    public newValue(_data:ChartData):void {};

    // when new values comes - we should process it. see abstract.chart.attributes.values as a default implementation
    public newValues(_data:ChartData[]):void {
        for (let i = 0; i < _data.length; i++) {
            this.newValue(_data[i]);
        }
    }

    protected isChartVisible():boolean {
        return $('#' + this.id).length && !this.updateStopped;
    }

    public subscribeToSubject(_obs:Observable<ChartData[]>):void {
        _obs.subscribe((data:ChartData[]) => {
            if(this.isChartVisible()) {
                this.newValues(data); // if the chart is visible - update
            }
        });
    }

    // do not use this method until it's really necessary
    public fitToContainer():void {
        let canvas:any = document.getElementById(this.id);
        if (canvas != undefined) {
            canvas.style.width = '100%';
            canvas.style.height = '100%';
            canvas.width = canvas.offsetWidth;
            canvas.height = canvas.offsetHeight;
        }
    }
}

class GUID {
    static newGuid():string {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            let r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }
}