/// <reference path="../ts/types/jquery.d.ts" />
declare module bundles {
    class bundle {
        public active: boolean;
        public type: string;
        public name: string;
        public description: string;
        public status: bunstatus.bundleStatus;
        public licenseInfo: license.licenseInfo;
        constructor(active?: boolean, type?: string, name?: string, description?: string, statusCurrent?: bunstatus.bundleStatus, licenseInfo?: license.licenseInfo);
    }
}
declare module bunstatus {
    class bundleStatus {
        public curCount: number;
        public maxCount: number;
        public currentInstanceCount: number;
        public maxInstanceCount: number;
        public managmentTargets: target.managmentTarget[];
        constructor(curCount?: number, maxCount?: number, targets?: target.managmentTarget[]);
    }
}
declare module license {
    class licenseInfo {
        public descr: string;
        public description: string;
        constructor(descr?: string);
    }
}
interface JQuery {
    getLicenseInfo(opts: options): JQuery;
}
interface JQuery {
    createLoaderTable(opts: options): JQuery;
    addOperations(UUID: any, status: any): void;
}
declare module target {
    class managmentTarget {
        public connectionString: string;
        public events: number;
        public attributes: number;
        constructor(connectionString?: string, events?: number, attributes?: number);
    }
}
declare class options {
    private _useStub;
    public useStub : boolean;
    constructor(usestub?: boolean);
}
declare module stubs {
    function getSummary(): bundles.bundle[];
}
interface JQuery {
    createSummaryTable(opts: options): JQuery;
}
