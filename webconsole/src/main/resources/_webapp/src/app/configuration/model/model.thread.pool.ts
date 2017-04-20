import { Entity } from "./model.entity";

export class ThreadPool extends Entity {
    private static MAX_INT_NUMBER:number = 2147483647;

    private _threadPriority:number;
    private _minPoolSize:number;
    private _maxPoolSize:number;
    private _queueSize:number;
    private _keepAliveTime:number;

    private _useInfiniteQueue:boolean;

    get threadPriority(): number {
        return this._threadPriority;
    }

    set threadPriority(value: number) {
        this._threadPriority = value;
    }

    get minPoolSize(): number {
        return this._minPoolSize;
    }

    set minPoolSize(value: number) {
        this._minPoolSize = value;
    }

    get maxPoolSize(): number {
        return this._maxPoolSize;
    }

    set maxPoolSize(value: number) {
        this._maxPoolSize = value;
    }

    get queueSize(): number {
        return this._queueSize;
    }

    set queueSize(value: number) {
        this._queueSize = value;
    }

    get keepAliveTime(): number {
        return this._keepAliveTime;
    }

    set keepAliveTime(value: number) {
        this._keepAliveTime = value;
    }

    get useInfiniteQueue(): boolean {
        return this._useInfiniteQueue;
    }

    set useInfiniteQueue(value: boolean) {
        this._useInfiniteQueue = value;
    }

    /**
     * Constructor with default values.
     *
     * threadPriority = DEFAULT_THREAD_PRIORITY.
     * minPoolSize = DEFAULT_MIN_POOL_SIZE.
     * maxPoolSize = DEFAULT_MAX_POOL_SIZE.
     * queueSize = INFINITE_QUEUE_SIZE.
     * keepAliveTime = DEFAULT_KEEP_ALIVE_TIME.
     * @param name - name for the entity.
     * @param params - parameters key value map for the entity.
     */
    constructor(name:string, params:{[key: string]: string;}) {
        super(name, params);
        this.threadPriority = 5;
        this.minPoolSize = 1;
        this.maxPoolSize = 2;
        this.queueSize = 5;
        this.keepAliveTime = 1;
    }

    public clone():ThreadPool {
        return $.extend({}, this);
    }

    public toJSON():any {
        let _value:any = {};
        if (this.parameters.length > 0) {
            _value["parameters"] = this.stringifyParameters();
        }
        _value["threadPriority"] = this.threadPriority;
        _value["minPoolSize"] = this.minPoolSize;
        _value["maxPoolSize"] = this.maxPoolSize;
        _value["keepAliveTime"] = this.keepAliveTime;
        _value["queueSize"] = this.useInfiniteQueue ? ThreadPool.MAX_INT_NUMBER : this.queueSize;
        return _value;
    }

    public static makeFromJson(name:string, _json:any):ThreadPool {
        let _tp:ThreadPool = new ThreadPool(name, _json["parameters"]);
        if (_json["threadPriority"] != undefined) {
            _tp.threadPriority = _json["threadPriority"];
        }
        if (_json["minPoolSize"] != undefined) {
            _tp.minPoolSize = _json["minPoolSize"];
        }
        if (_json["maxPoolSize"] != undefined) {
            _tp.maxPoolSize = _json["maxPoolSize"];
        }
        if (_json["queueSize"] != undefined) {
            _tp.queueSize = _json["queueSize"];
        }
        if (_json["keepAliveTime"] != undefined) {
            _tp.keepAliveTime = _json["keepAliveTime"];
        }
        _tp.useInfiniteQueue = (_tp.queueSize == ThreadPool.MAX_INT_NUMBER);
        return _tp;
    }

    public static makeBunchFromJson(_json:any):ThreadPool[] {
        let _tps:ThreadPool[] = [];
        for (let key in _json) {
            _tps.push(ThreadPool.makeFromJson(key, _json[key]));
        }
        return _tps;
    }
}