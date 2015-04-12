/**
 *
 */
package de.uni_leipzig.comprak.books.wcmbookserver.extract.utils;

import java.util.Properties;

/**
 * Extension of default Properties class of Java. Adds typed getters and
 * setters.
 *
 * @author Erik Körner
 */
public class Props extends Properties {

    private static final long serialVersionUID = -8218583856119489356L;

    // ------------------------------------------------------------------------

    /**
     * Default Constructor. Empty properties.
     */
    public Props() {
        super();
    }

    /**
     * Create new Props with values from given Properties object.
     *
     * @param properties Properties to copy
     */
    public Props(Properties properties) {
        super(properties);
    }

    /**
     * Create new Props with values from given Props object.
     *
     * @param props Properties to copy.
     */
    public Props(Props props) {
        super(props);
    }

    // ------------------------------------------------------------------------

    // Property/Configuration access

    /**
     * Gets String property/configuration value for name, uses defaultValue if
     * not set.
     *
     * @param name         Name of property/configuration
     * @param defaultValue Default value if not set
     * @return String value
     */
    public final String getStringProp(String name, String defaultValue) {
        if (super.containsKey(name)) {
            return (String) super.getProperty(name);
        } else {
            return defaultValue;
        } // if-else
    } // public String getStringProp

    /**
     * Gets String property/configuration value for name, returns empty String
     * if not set.
     *
     * @param name Name of property/configuration
     * @return String value
     */
    public final String getStringProp(String name) {
        return getStringProp(name, "");
    } // public String getStringProp

    /**
     * Gets int property/configuration value for name, uses defaultValue if not
     * set.
     *
     * @param name         Name of property/configuration
     * @param defaultValue Default value if not set
     * @return int value
     */
    public final int getIntProp(String name, int defaultValue) {
        if (super.containsKey(name)) {
            return Integer.valueOf(super.getProperty(name));
        } else {
            return defaultValue;
        } // if-else
    } // public int getIntProp

    /**
     * Gets long property/configuration value for name, uses defaultValue if not
     * set.
     *
     * @param name         Name of property/configuration
     * @param defaultValue Default value if not set
     * @return long value
     */
    public final long getLongProp(String name, long defaultValue) {
        if (super.containsKey(name)) {
            return Long.valueOf(super.getProperty(name));
        } else {
            return defaultValue;
        } // if-else
    } // public long getLongProp

    /**
     * Gets float property/configuration value for name, uses defaultValue if
     * not set.
     *
     * @param name         Name of property/configuration
     * @param defaultValue Default value if not set
     * @return int value
     */
    public final float getFloatProp(String name, float defaultValue) {
        if (super.containsKey(name)) {
            return Float.valueOf(super.getProperty(name));
        } else {
            return defaultValue;
        } // if-else
    } // public float getFloatProp

    /**
     * Gets boolean property/configuration value for name, uses defaultValue if
     * not set.
     *
     * @param name         Name of property/configuration
     * @param defaultValue Default value if not set
     * @return String value
     */
    public final boolean getBoolProp(String name, boolean defaultValue) {
        if (super.containsKey(name)) {
            return Boolean.valueOf(super.getProperty(name));
        } else {
            return defaultValue;
        } // if-else
    } // public boolean getBoolProp

    /**
     * Sets String property/configuration value for name.
     *
     * @param name  Name of property/configuration
     * @param value String value to set
     */
    public final void setStringProp(String name, String value) {
        super.setProperty(name, value);
    } // public void setStringProp

    /**
     * Sets boolean property/configuration value for name.
     *
     * @param name  Name of property/configuration
     * @param value boolean value to set
     */
    public final void setBoolProp(String name, boolean value) {
        super.setProperty(name, String.valueOf(value));
    } // public void setBoolProp

    /**
     * Sets int property/configuration value for name.
     *
     * @param name  Name of property/configuration
     * @param value int value to set
     */
    public final void setIntProp(String name, int value) {
        super.setProperty(name, String.valueOf(value));
    } // public void setIntProp

    /**
     * Sets long property/configuration value for name.
     *
     * @param name  Name of property/configuration
     * @param value long value to set
     */
    public final void setLongProp(String name, long value) {
        super.setProperty(name, String.valueOf(value));
    } // public void setLongProp

    /**
     * Sets int property/configuration value for name.
     *
     * @param name  Name of property/configuration
     * @param value int value to set
     */
    public final void setFloatProp(String name, float value) {
        super.setProperty(name, String.valueOf(value));
    } // public void setFloatProp

}
