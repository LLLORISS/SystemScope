package nm.sc.systemscope.modules;

/**
 * Enum representing the theme options for the system.
 */
public enum Theme {
    /**
     * Represents the dark theme.
     */
    DARK,

    /**
     * Represents the light theme.
     */
    LIGHT;

    /**
     * Parses a string to the corresponding Theme enum.
     * Defaults to DARK if input is invalid or not recognized.
     *
     * @param value The string value representing the theme.
     * @return The corresponding Theme enum.
     */
    public static Theme fromString(String value) {
        return value.equalsIgnoreCase("light") ? LIGHT : DARK;
    }

    /**
     * Converts the current theme to its string representation.
     * <p>
     * The method returns "dark" for the {@link Theme#DARK} theme and "light" for the {@link Theme#LIGHT} theme.
     * </p>
     *
     * @return The string representation of the current theme ("dark" or "light").
     */
    @Override
    public String toString() {
        return this == DARK ? "dark" : "light";
    }
}
