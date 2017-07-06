import * as moment from 'moment/moment';

export class SnampUtils {

    /**
     * Returns the number of milliseconds for source.
     * @param source - number (is being returned untouched) or string (duration notation).
     */
    public static parseDuration(source:any):number {
        return (!isNaN(parseFloat(source)) && isFinite(source))
            ? source :  moment.duration(source).asMilliseconds();
    }

    /**
     * Returns duration string notation for milliseconds.
     * @param source - number of milliseconds.
     * @returns {string} - duration string notation.
     */
    public static toDurationString(source:number):string {
        return moment.duration({ milliseconds: source}).toISOString();
    }

    /**
     * Humanize the duration.
     * @param source - milliseconds number.
     * @returns {string} - humanized duration string notation.
     */
    public static toHumanizedDuration(source:number):string {
        return moment.duration({ milliseconds: source}).humanize();
    }
}