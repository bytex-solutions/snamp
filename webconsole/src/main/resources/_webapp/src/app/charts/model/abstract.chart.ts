import { ChartData } from './chart.data';
import { Observable } from 'rxjs/Observable';

export abstract class AbstractChart {
    public static VBAR:string = "verticalBarChartOfAttributeValues";
    public static PIE:string = "pieChartOfAttributeValues";
    public static HBAR:string = "horizontalBarChartOfAttributeValues";
    public static PANEL:string = "panelOfAttributeValues";
    public static LINE:string = "lineChartOfAttributeValues";

    // map chartjs types to current hierarchy types
    public static TYPE_MAPPING:{[key:string]:string} = {
        'pie':              AbstractChart.PIE,
        'doughnut':         AbstractChart.PIE,
        'horizontalBar':    AbstractChart.HBAR,
        'bar':              AbstractChart.VBAR,
        'line':             AbstractChart.LINE,
        'panel':            AbstractChart.PANEL
    };

    // reverse mapping
    public static CHART_TYPE_OF(name:string):string {
        let _value:string = "";
        for (let key in AbstractChart.TYPE_MAPPING) {
            if (AbstractChart.TYPE_MAPPING[key] == name) {
                _value = AbstractChart.TYPE_MAPPING[key];
                break;
            }
        }
        if (_value == "") {
            throw new Error("Cannot find any corresponding type for " + name);
        } else {
            return _value;
        }
    }

    public name:string;
    public preferences:{ [key: string]: any } = { };
    public id:string = Guid.newGuid();
    public chartData: ChartData[] = [];
    public abstract toJSON():any;
    public abstract draw():void;
    public abstract updateChart(_data:ChartData):void;

    protected simplifyData():any[] = {
        let _value:any[] = [];
        for (let i = 0; i < this.chartData.length; i++) {
            _value.push(this.chartData[i].attributeValue);
        }
        return _value;
    }

    public subscribeToSubject(_obs:Observable<ChartData>):void {
        _obs.subscribe((data:ChartData) => {
            this.chartData.push(data);
            this.updateChart(data);
        });
    }
}

class Guid {
    static newGuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return "chart" + v.toString(16);
        });
    }
}