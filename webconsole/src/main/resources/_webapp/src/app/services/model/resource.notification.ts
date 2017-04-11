import { AbstractNotification } from "./abstract.notification";

export class ResourceNotification extends AbstractNotification {

    htmlDetails(): string {
        return undefined;
    }

    shortDescription(): string {
        return undefined;
    }

    fillFromJson(json: any): void {
        return undefined;
    }

}