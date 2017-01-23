import { ChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';

export class PanelOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = "panelOfAttributeValues";

    public createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }
}