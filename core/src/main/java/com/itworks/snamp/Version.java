package com.itworks.snamp;

/**
 * Provides subroutines for working with version string.
 * <p>
 *     This class supports the following version formats: X.Y, X.Y.Z, X.Y.Z.W
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class Version {
    /**
     * Represents version component splitter inside of the version string.
     */
    public static final char SPLITTER = '.';

    /**
     * Represents a version component that is used to indicate any value.
     */
    public static final char PLACEHOLDER = '*';

    private static final String SPLITTER_REGEX = "\\" + SPLITTER;

    private Version(){

    }

    /**
     * Splits the version string to the components.
     * @param version The version string to split.
     * @return An array of version components.
     */
    public static String[] split(final String version){
        return version!=null && version.length() > 0 ? version.split(SPLITTER_REGEX) : new String[]{version};
    }

    /**
     * Compares the two version strings.
     * @param version1 The first version string to compare.
     * @param version2 The second version string to compare.
     * @return The comparison result similar to {@link java.lang.Comparable#compareTo(Object)}.
     */
    public static int compare(final String version1, final String version2){
        final String[] vals1 = split(version1);
        final String[] vals2 = split(version2);
        int i=0;
        while(i<vals1.length && i<vals2.length){
            final String comp1 = vals1[i];
            final String comp2 = vals2[i];
            if(comp1.charAt(0) == PLACEHOLDER ||
                    comp2.charAt(0) == PLACEHOLDER ||
                    comp1.equals(comp2)) i += 1;
            else break;
        }
        return Integer.signum(i<vals1.length && i<vals2.length ?
                Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i])):
                vals1.length - vals2.length);
    }

    /**
     * Constructs a new version string from MAJOR and MINOR version components.
     * @param major MAJOR version.
     * @param minor MINOR version.
     * @return A string in MAJOR.MINOR format.
     */
    public static String toString(final long major, final long minor){
        return String.format("%s.%s", major, minor);
    }

    /**
     * Constructs a new version string from MAJOR, MINOR, BUILD version components.
     * @param major MAJOR version.
     * @param minor MINOR version.
     * @param build BUILD version.
     * @return A string in MAJOR.MINOR.BUILD format.
     */
    public static String toString(final long major, final long minor, final long build){
        return String.format("%s.%s.%s", major, minor, build);
    }

    /**
     * Constructs a new version string from MAJOR, MINOR, BUILD, REVISION version components.
     * @param major MAJOR version.
     * @param minor MINOR version.
     * @param build BUILD version.
     * @param revision REVISION version.
     * @return A string in MAJOR.MINOR.BUILD.REVISION format.
     */
    public static String toString(final long major, final long minor, final long build, final long revision){
        return String.format("%s.%s.%s.%s", major, minor, build, revision);
    }
}
