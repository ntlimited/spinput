package com.ntlimited.spinput.node.connection;

import com.ntlimited.spinput.node.Node;
import com.ntlimited.spinput.node.command.NodeCommand;

/**
 * A dummy connection used to avoid null pointer exceptions.
 */
public final class DummyConnection implements NodeConnection
{
    /**
     * Dummy connections can never connect.
     */
    @Override
    public void connect()
    {
        throw new IllegalStateException(
            "DummyConnections cannot connect");
    }

    /**
     * Dummy connections will never be connected.
     */
    @Override
    public boolean isConnected()
    {
        return false;
    }

    /**
     * Dummy connections cannot send commands.
     */
    @Override
    public void sendCommand(NodeCommand command)
    {
        throw new IllegalStateException(
            "DummyConnections cannot send commands");
    }

    /**
     * Dummy connections are never connected in the first place.
     */
    @Override
    public void disconnect() {}

    /**
     * Dummy connections have no node.
     */
    @Override
    public Node getNode() { return null; }

    /**
     * Dummy connections have no node.
     */
    @Override
    public void setNode(Node n) {}
}
