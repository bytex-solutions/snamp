export class ParamDescriptor {
    public name:string = "";
    public required:boolean = false;
    public defaultValue:string = "";
    public pattern:string = "";
    public association:string[] = [];
    public exclucion:string[] = [];
    public extension:string[] = [];

    constructor(parameters:any) {
        this.name = parameters["name"];
        this.defaultValue = parameters["defaultValue"];
        this.pattern = parameters["pattern"];
        this.required = (parameters["required"] === "true");
        if (parameters["association"] != undefined) {
            this.association = parameters["association"].split(",");
        }
        if (parameters["exclucion"] != undefined) {
            this.exclucion = parameters["exclucion"].split(",");
        }
        if (parameters["extension"] != undefined) {
            this.extension = parameters["extension"].split(",");
        }
    }

    public static stubValue:string = "Input the value";

    public static getDescriptorByName(inputValue:ParamDescriptor[], inputKey:string):ParamDescriptor {
        let result:ParamDescriptor = undefined;
        if (inputValue != undefined) {
            for (let i = 0; i < inputValue.length; i++) {
                if (inputValue[i].name === inputKey) {
                    result = inputValue[i];
                    break;
                }
            }
        }
        return result;
    }
}