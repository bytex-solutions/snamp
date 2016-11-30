export abstract class Entity {
    protected parameters : { [key:string]:string; } = {};
    constructor(parameters: { [key:string]:string; }) {
        this.parameters = parameters;
    }
}