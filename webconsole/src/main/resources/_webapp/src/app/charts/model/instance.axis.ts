import { Axis } from './abstract.axis';

export class InstanceNameAxis extends Axis {
    public @type:string = "instance";
    constructor() {
        this.name = "instances";
    }
}