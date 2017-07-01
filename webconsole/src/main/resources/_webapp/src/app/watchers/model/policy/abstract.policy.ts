export abstract class AbstractPolicy {

    public static getReduceOperations():string[] {
        return ["MAX", "MIN", "MEAN", "MEDIAN", "PERCENTILE_90", "PERCENTILE_95", "PERCENTILE_97", "SUM"];
    }
    abstract toJSON():any;
}