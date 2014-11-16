module bunstatus
{
    export class bundleStatus
    {
        currentInstanceCount: number;
        maxInstanceCount: number;
        managmentTargets : target.managmentTarget[];

        constructor(public curCount:number = 0, public maxCount:number=0, targets:target.managmentTarget[]=null)
        {
            this.currentInstanceCount = curCount;
            this.maxInstanceCount = maxCount;
            this.managmentTargets = targets;
        }
    }
}