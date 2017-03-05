export public class ScriptletDataObject {
    public language:string;
    public script:string;
    public isURL:boolean;

    constructor(){
        this.language = "n/a";
        this.script = "empty";
        this.isURL = false;
    }

    public static fromJSON(json:string):ScriptletDataObject {
        let instance:ScriptletDataObject = new ScriptletDataObject();
        if (json["language"] != undefined) {
            instance.language = json["language"];
        }
        if (json["script"] != undefined) {
            instance.script = json["script"];
        }
        if (json["isURL"] != undefined) {
            instance.isURL = (json["isURL"] == 'true');
        }
        return instance;
    }

    public static toJSON():any {
        let _value:any = {};
        _value["language"] = this.language;
        _value["script"] = this.script;
        _value["isURL"] = this.isURL;
        return _value;
    }
}