import { AbstractNotification } from "./abstract.notification";

export class HealthStatusNotification extends AbstractNotification {

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