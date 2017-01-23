import { Axis } from './abstract.axis';
import { AttributeInformation } from './attribute';

export class AttributeValueAxis extends Axis {
    public @type:string = "attributeValue";
    public sourceAttribute:AttributeInformation;
}