import { Axis } from './abstract.axis';

export class ChronoAxis extends Axis {
    public @type:string = "chrono";
    public unitOfMeasurement:string = "seconds";
}