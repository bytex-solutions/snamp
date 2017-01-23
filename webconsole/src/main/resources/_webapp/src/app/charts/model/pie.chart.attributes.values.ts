import { ChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';

export class PieChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public @type:string = "pieChartOfAttributeValues";

    public createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }
}