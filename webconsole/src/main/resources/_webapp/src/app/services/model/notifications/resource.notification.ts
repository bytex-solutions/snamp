import { AbstractNotification } from "./abstract.notification";

export class ResourceNotification extends AbstractNotification {

    htmlDetails(): string {
        return "";
    }

    shortDescription(): string {
        return "";
    }

    fillFromJson(json: any): void {}

}