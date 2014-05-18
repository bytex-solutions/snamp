/*
    This class contains specification for describing Bundle model
 */
module com.snamp.models
{
    export class bundle
    {
        active: boolean; // is bundle on or off
        type: string; // type of bundle
        name: string; // bundle name
        description: string; // short bundle description
        status: bundleStatus; // full info about bundle status
        licenseInfo: license; // licensing info for current bundle

        constructor(active:boolean=false, type:string="",name:string="",
                    description:string="",status:bundleStatus=null, licenseInfo:license=null)
        {
            this.active = active;
            this.type = type;
            this.name = name;
            this. description = description;
            this.status = bundleStatus;
            this.licenseInfo = licenseInfo;

        }
    }
}