import { AbstractChart } from "./abstract.chart";

export class ChartUtils {


    public static hslFromValue(i:number, count:number, opacity:any):string {
        let clr:any = 360 * i / count;
        return 'hsla(' + clr + ', 100%, 50%, ' + opacity + ')';
    }
}