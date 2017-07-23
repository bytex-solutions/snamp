import { Component, ChangeDetectorRef, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';
import { Overlay } from 'angular2-modal';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';
import { ThreadPool } from "./model/model.thread.pool";
import { Entity } from "./model/model.entity";

@Component({
    moduleId: module.id,
    templateUrl: './templates/threadpools.html',
    styleUrls: ['./templates/css/threadpools.css']
})
export class ThreadPoolsComponent implements OnInit {

    public selectedThreadPool:ThreadPool;
    public threadPools:ThreadPool[] = [];

    private threadPool:ThreadPool;

    private static modalDialogId:string = "#editThreadPoolModal";

    constructor(private http: ApiClient,
                overlay: Overlay,
                vcRef: ViewContainerRef,
                private cd: ChangeDetectorRef,
                private modal: Modal) {
        overlay.defaultViewContainer = vcRef;
    }

    ngOnInit():void {
        this.http.get(REST.THREAD_POOL_CONFIG)
            .map((res:Response) => res.json())
            .subscribe(data => {
                this.threadPools = ThreadPool.makeBunchFromJson(data);
                if (this.threadPools.length > 0) {
                    this.setActiveThreadPool(this.threadPools[0]);
                }
            });
    }

    private setActiveThreadPool(pool:ThreadPool):void {
        this.selectedThreadPool = pool;
        this.threadPool = this.selectedThreadPool.clone();
        this.cd.detectChanges();
    }

    private removeThreadPoolFromArray(pool:ThreadPool):void {
        for (let i = 0; i < this.threadPools.length; i++) {
            if (this.threadPools[i].guid == pool.guid) {
                this.threadPools.splice(i, 1);
                if (this.threadPools.length > 0) {
                    this.setActiveThreadPool(this.threadPools[0]);
                }
                console.debug("Thread pool has been removed");
                break;
            }
        }
    }

    private saveThreadPoolToServer(pool:ThreadPool):void {
        this.http.put(REST.THREAD_POOL_BY_NAME(pool.name), pool.toJSON())
            .map((response:Response) => response.text())
            .subscribe(() => {
                // if this is the new pool - push it to the array for displaying it in the template
                if (!Entity.containsInArray(pool, this.threadPools)) {
                    this.threadPools.push(pool);
                }
                this.setActiveThreadPool(pool);
            });
    }

    isActivePool(pool:ThreadPool):boolean {
        return pool.guid == this.selectedThreadPool.guid;
    }

    cloneThreadPool(pool:ThreadPool):void {
        this.modal.prompt()
            .className(<VEXBuiltInThemes>'default')
            .message('Clone thread pool')
            .placeholder('Please set the name for a new thread pool')
            .open()
            .then(dialog => dialog.result)
            .then(result => {
                let _newThreadPool:ThreadPool = pool.clone();
                _newThreadPool.name = result;
                this.http.put(REST.THREAD_POOL_BY_NAME(result), _newThreadPool.toJSON())
                    .map((response:Response) => response.text())
                    .subscribe(() => {
                        this.threadPools.push(_newThreadPool);
                        this.setActiveThreadPool(_newThreadPool);
                    });
            })
            .catch(() => {});
    }

    editThreadPool(pool:ThreadPool):void {
        this.setActiveThreadPool(pool);
        $(ThreadPoolsComponent.modalDialogId).modal("show");
    }

    removeThreadPool(pool:ThreadPool):void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .message('Thread pool is going to be removed. Proceed?')
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this.http.delete(REST.THREAD_POOL_BY_NAME(pool.name))
                            .map((res: Response) => res.text())
                            .subscribe(() => this.removeThreadPoolFromArray(pool));
                        return response;
                    })
                    .catch(() => {
                        console.debug("User preferred to decline thread pool removing");
                    });
            });
    }

    addNewThreadPool():void {
        this.modal.prompt()
            .className(<VEXBuiltInThemes>'default')
            .message('New thread pool')
            .placeholder('Please set the name for a new thread pool')
            .open()
            .then(dialog => dialog.result)
            .then(result => {
                let _newThreadPool:ThreadPool = new ThreadPool(result, {});
                this.setActiveThreadPool(_newThreadPool);
                $(ThreadPoolsComponent.modalDialogId).modal("show");

            })
            .catch(() => {});
    }

    cancelThreadDialog():void {
        if (this.threadPools.length > 0) {
            this.setActiveThreadPool(this.threadPools[0]);
        }
        $(ThreadPoolsComponent.modalDialogId).modal("hide");
    }

    saveThreadDialog():void {
        // if we already have this pool in the array and its name has been changed - confirm user about consequences
        if (this.threadPool.name != this.selectedThreadPool.name
                && Entity.containsInArray(this.threadPool, this.threadPools)) {
            this.modal.confirm()
                .className(<VEXBuiltInThemes>'default')
                .message('You have renamed thread pool. It will affect all linked components. Proceed?')
                .open()
                .then((resultPromise) => {
                    return (<Promise<boolean>>resultPromise.result)
                        .then((response) => {
                            this.saveThreadPoolToServer(this.threadPool);
                            $(ThreadPoolsComponent.modalDialogId).modal("hide");
                            return response;
                        })
                        .catch(() => {
                            console.debug("User preferred to decline thread pool saving");
                        });
                });
        } else {
            this.saveThreadPoolToServer(this.threadPool);
            $(ThreadPoolsComponent.modalDialogId).modal("hide");
        }
    }
}