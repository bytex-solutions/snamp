class options
{
    private _useStub:boolean;

    public get useStub():boolean {
     return this._useStub;
    }

    public set useStub(value:boolean) {
     this._useStub = value;
    }

    public constructor(usestub:boolean=true)
    {
        this._useStub = usestub;
    }
}

