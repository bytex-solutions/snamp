export abstract class E2EView {

    public static CHILD_COMPONENT = "childComponents";
    public static COMPONENT_MODULES = "componentModules";
    public static COMPONENT_MODULES = "landscape";

    public name:string;
    public preferences:{ [key: string]: any } = { };
    public id:string = "e2eview" + GUID.newGuid();

    public abstract toJSON():any;
}

class GUID {
    static newGuid():string {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }
}