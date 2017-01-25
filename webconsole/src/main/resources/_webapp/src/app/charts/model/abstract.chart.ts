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

    public abstract toJSON():any;
}