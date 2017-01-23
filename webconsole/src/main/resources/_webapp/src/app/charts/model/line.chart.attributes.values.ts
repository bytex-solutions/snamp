import { ChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { ChronoAxis } from './chrono.axis';
import { AttributeValueAxis } from './attribute.value.axis';

export class LineChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public @type:string = "lineChartOfAttributeValues";

    public createDefaultAxisX() {
        return new ChronoAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }
}