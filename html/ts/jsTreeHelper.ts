module jsTreeHelper
{
    export class jsonFormat
    {
        // Alternative format of the node (id & parent are required)
        id          :  string = "#"; // required
        parent      :  string = "#"; // required
        text        :  string = ""; // node text
        icon        :  string = ""; // string for custom
        state       : stateJson = new stateJson;
        li_attr     : {};  // attributes for the generated LI node
        a_attr      : {};  // attributes for the generated A node

        constructor(id:string = "#", parent:string="#", text:string="", icon:string="",state:stateJson = new stateJson,
                    li_attr:any={}, a_attr:any={})
        {
            this.id = id;
            this.parent = parent;
            this.text = text;
            this.icon = icon;
            this.state = state;
            this.li_attr = li_attr;
            this.a_attr = a_attr;
        }
    }

    export class stateJson
    {   opened    : boolean = true; // is the node open
        disabled  : boolean = false; // is the node disabled
        selected  : boolean = false; // is the node selected
    }
}
