/*
    This class contains specification for describing Bundle model
 */
module bundles
{

    function getStringStatusRepresentation(status:number):string
    {
        switch (status)
        {
            case 1:
                return "UNINSTALLED";
                break;
            case 2:
                return "INSTALLED";
                break;
            case 4:
                return "RESOLVED";
                break;
            case 8:
                return "STARTING";
                break;
            case 16:
                return "STOPPING";
                break;
            case 32:
                return "ACTIVE";
                break;
            default:
                return "UNKNOWN STATUS";
        }
    }
    export class bundle
    {
        version: string; // version of the bundle
        state: string; // is bundle on or off
        name: string; // bundle name
        description: string; // short bundle description
        licensing:  { [s: string]: string; } = {};

/*
        constructor(version:string="", state:string="", name:string="",
                    description:string="", licensing:any=null)
        {
            this.version = version;
            this.state = state;
            this.name = name;
            this.description = description;
            if (licensing instanceof Object)
                this.licensing = licensing;
            else
                this.licensing = null;

        }
*/

        constructor(data: Object=null)
        {
           if (data == null) new bundle();
           else
           {
               this.version = data.hasOwnProperty("Version")?data['Version']:"";
               this.state = data.hasOwnProperty("State")?getStringStatusRepresentation(data['State']):"";
               this.name = data.hasOwnProperty("DisplayName")?data['DisplayName']:"";
               this.description = data.hasOwnProperty("Description")?data['Description']:"";
               if (data.hasOwnProperty("Licensing"))
               {
                   var licensing_ = data['Licensing'];
                   if (licensing_ instanceof Object)
                   {
                       this.licensing = licensing_;
                   }
               }
           }
        }

        getLicenseAsAString():string
        {
           var res_string:string = "";
           if (this.licensing != null)
           {
               for (var key in this.licensing)
               {
                  res_string += key + " : " + this.licensing[key] + ";\n";
               }
               return res_string;
           }
           else
            return "";
        }
    }
}