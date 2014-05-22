package com.ntlimited.spinput.config;

/**
 * A Spinput configuration holder
 */
public class Configuration
{
    /**
     * Return the current Configuration object. Note that
     */
    public static ConfigurationProvider getConfiguration()
    {
        return kConfig;
    }

    public static synchronized ConfigurationProvider configure(String filePath)
    {
        return kConfig = null;
    }

    private static ConfigurationProvider kConfig = null;
}
