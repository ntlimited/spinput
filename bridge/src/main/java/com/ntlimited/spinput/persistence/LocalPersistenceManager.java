package com.ntlimited.spinput.persistence;

/**
 * A LocalPersistenceManager is a {@link PersistenceManager} that
 * stores and reads persisted data to and from the local filesystem.
 */
public class LocalPersistenceManager implements PersistenceManager
{
    /**
     * Create a manager that will store data in files under the
     * specified root directory.
     *
     * @param rootDirectory
     */
    public LocalPersistenceManager(String rootDirectory)
    {
        fRoot = rootDirectory;
    }

    /** {@inheritDoc} */
    @Override
    public <T> void store(String key, T value)
    {
    }
    
    /** {@inheritDoc} */
    @Override
    public <T> T read(String key) throws PersistenceException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T read(String key, T defaultValue)
    {
        return null;
    }

    private final String fRoot;
}
