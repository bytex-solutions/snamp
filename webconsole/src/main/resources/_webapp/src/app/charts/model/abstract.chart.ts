export abstract class AbstractChart {
    public name:string;
    public preferences:{ [key: string]: any } = { };

    public abstract toJSON():any;
}