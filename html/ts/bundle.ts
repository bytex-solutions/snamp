/*
    This class contains specification for describing Bundle model
 */
module bundles
{
    export class bundle
    {
        active: boolean; // is bundle on or off
        type: string; // type of bundle
        name: string; // bundle name
        description: string; // short bundle description
        status: bunstatus.bundleStatus; // full info about bundle status
        licenseInfo: license.licenseInfo; // licensing info for current bundle

        constructor(active:boolean=false, type:string="",name:string="",
                    description:string="",statusCurrent:bunstatus.bundleStatus=null, licenseInfo:license.licenseInfo=null)
        {
            this.active = active;
            this.type = type;
            this.name = name;
            this.description = description;
            this.status = statusCurrent;
            this.licenseInfo = licenseInfo;

        }
    }
}