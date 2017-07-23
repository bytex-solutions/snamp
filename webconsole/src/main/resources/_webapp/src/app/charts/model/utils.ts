import { AbstractChart } from "./abstract.chart";

export class ChartTypeDescription {
    public mappedTypeName:string;
    public consoleSpecificName:string;
    public instancesSupport:boolean;

    constructor(csm:string, mtn:string, is:boolean) {
        this.consoleSpecificName = csm;
        this.mappedTypeName = mtn;
        this.instancesSupport = is;
    }

    public static generateType():ChartTypeDescription[] {
        return [
            new ChartTypeDescription('doughnut', AbstractChart.PIE, true),
            new ChartTypeDescription('horizontalBar', AbstractChart.HBAR, true),
            new ChartTypeDescription('bar', AbstractChart.VBAR, true),
            new ChartTypeDescription('line', AbstractChart.LINE, true),
            new ChartTypeDescription('panel', AbstractChart.PANEL, true),
            new ChartTypeDescription('statuses', AbstractChart.HEALTH_STATUS, false),
            new ChartTypeDescription('resources', AbstractChart.RESOURCE_COUNT, false),
            new ChartTypeDescription('scaleIn', AbstractChart.SCALE_IN, false),
            new ChartTypeDescription('scaleOut', AbstractChart.SCALE_OUT, false),
            new ChartTypeDescription('voting', AbstractChart.VOTING, false)
        ];
    }

}