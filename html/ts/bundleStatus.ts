export module com.snamp.models
{

    export class bundleStatus
    {
        currentInstanceCount: number;
        maxInstanceCount: number;
        managmentTargets : managmentTarget[];

        constructor(public curCount:number = 0, public maxCount:number=0, targets:managmentTarget[]=null)
        {
            this.currentInstanceCount = curCount;
            this.maxInstanceCount = maxCount;
            this.managmentTargets = targets;
        }
    }
}