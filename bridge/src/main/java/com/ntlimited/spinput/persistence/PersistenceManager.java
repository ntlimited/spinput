package com.ntlimited.spinput.persistence;

/**
 * A PersistenceManager handles storing and resurrecting data
 * to survive across restarts of the daemon and server.
 */
public interface PersistenceManager
{
    /**
     * Store the given value to the specified key.
     *
     * @param key
     * @param obj
     * @throws PersistenceException
     */
    public <T> void store(String key, T obj) throws PersistenceException;

    /**
     * Read the value stored at the given key.
     *
     * @param key
     * @throws PersistenceException
     * @return
     */
    public <T> T read(String key) throws PersistenceException;

    /**
     * Read the value stored at the given key, returning a
     * default value if the value fails to be retrieved.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public <T> T read(String key, T defaultValue);
}
