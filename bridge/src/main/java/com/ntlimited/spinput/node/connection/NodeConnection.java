package com.ntlimited.spinput.node.connection;

import com.ntlimited.spinput.node.Node;
import com.ntlimited.spinput.node.command.NodeCommand;

/**
 * Interface for abstracting the connection between the
 * server anda node.
 */
public interface NodeConnection
{
    /**
     * Connect to the remote node.
     */
    void connect() throws InterruptedException;

    /**
     * Return the connection status.
     *
     * @return whether the connection is connected
     */
    boolean isConnected();

    /**
     * Disconnect from the node.
     */
    void disconnect();

    /**
     * Set the node that is on the other end of the connection.
     *
     * @param n the node to set
     */
    void setNode(Node n);

    /**
     * Get the node on the other end of the connection.
     *
     * @return the node
     */
    Node getNode();

    /**
     * Send a command to the remote node.
     *
     * @param command the command to send
     */
    void sendCommand(NodeCommand command);
}
