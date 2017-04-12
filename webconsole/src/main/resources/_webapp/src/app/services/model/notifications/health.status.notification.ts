import { AbstractNotification } from "./abstract.notification";

export class HealthStatusNotification extends AbstractNotification {
    htmlDetails(): string {
        return undefined;
    }

    shortDescription(): string {
        return undefined;
    }

    fillFromJson(json: any): void {
    }
}

/**
 1) example:
        {"@messageType":"healthStatusChanged","previousStatus":{"@type":"InvalidAttributeValue","resourceName":"node#1","critical":true,"attributeName":"CPU","attributeValue":89.4655},"newStatus":{"@type":"OK"}}
 2) example:
        {"@messageType":"healthStatusChanged","previousStatus":{"@type":"OK"},"newStatus":{"@type":"InvalidAttributeValue","resourceName":"node#1","critical":true,"attributeName":"CPU","attributeValue":89.4655}}
 */