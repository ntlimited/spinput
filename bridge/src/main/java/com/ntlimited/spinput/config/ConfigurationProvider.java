package com.ntlimited.spinput.config;

import com.ntlimited.spinput.persistence.PersistenceManager;

/**
 * A ConfigurationProvider knows how to provide several
 * important values that are necessray for running the
 * spinput bridge daemon.
 */
public interface ConfigurationProvider
{
    /**
     * Get the PersistenceManager to use for loading/storing
     * arbitrary data.
     */
    public PersistenceManager getPersistenceManager();

    /**
     * Get the port to set up the Discovery server on.
     */
    public int getDiscoveryPort();

    /**
     * Get the port to use for the webserver. If the
     * secure web port is non-zero, then the webserver
     * on this port will automatically redirect to the
     * secure port.
     */
    public int getWebPort();

    /**
     * Get the port to use for secure transport, or 0 if
     * secure transport should not be used.
     */
    public int getSecureWebPort();
}
