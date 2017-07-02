import { ChartOfAttributeValues } from './abstract.chart.attributes.values';
import { AttributeInformation } from './attribute';
import { AttributeValueAxis } from './axis/attribute.value.axis';
import { ChartUtils } from "./chart.utils";

export abstract class TwoDimensionalChartOfAttributeValues extends ChartOfAttributeValues {

    public setSourceAttribute(sourceAttribute:AttributeInformation):void {
        if (this.getAxisX() instanceof AttributeValueAxis) {
            (<AttributeValueAxis>this.getAxisX()).sourceAttribute = sourceAttribute;
        }
        if (this.getAxisY() instanceof AttributeValueAxis) {
            (<AttributeValueAxis>this.getAxisY()).sourceAttribute = sourceAttribute;
        }
    }

    public getSourceAttribute():AttributeInformation {
        if (this.getAxisX() instanceof AttributeValueAxis) {
            return (<AttributeValueAxis>this.getAxisX()).sourceAttribute;
        } else if (this.getAxisY() instanceof AttributeValueAxis) {
            return (<AttributeValueAxis>this.getAxisY()).sourceAttribute;
        } else {
            return undefined;
        }
    }
}