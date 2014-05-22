package com.ntlimited.spinput.node;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntlimited.spinput.node.connection.DummyConnection;
import com.ntlimited.spinput.node.connection.NodeConnection;
import com.ntlimited.spinput.node.event.DeviceEvent;
import com.ntlimited.spinput.node.event.DeviceEventHandler;
import com.ntlimited.spinput.node.event.DeviceEventHandlerFactory;
import com.ntlimited.spinput.node.event.DiscardHandler;

/**
 * A Node has an identifier, and Endianness, a name, and a set
 * of attached devices. Two of these are intrinsic to the node;
 * the identifier and the endianness of the device should be
 * permanently linked. The name is a construct on the bridge
 * side for user convenience.
 */
public final class Node
{
    /**
     * Create a node with the given identifier and endianness.
     *
     * @param identifier the identifier of the node
     * @param endian the endianness of the node's hardware
     */
    public Node(NodeIdentifier identifier, Endianness endian)
    {
        this(identifier, endian, null);
    }

    /**
     * Create a node with the given identifier, endiannes, and name.
     *
     * @param identifier
     * @param endian
     * @param name
     */
    public Node(NodeIdentifier identifier, Endianness endian, String name)
    {
        fIdentifier = identifier;
        fEndianness = endian;
        fName = name;
    }

    /**
     * @return
     */
    public NodeIdentifier getIdentifier()
    {
        return fIdentifier;
    }

    /**
     * @return
     */
    public String getName()
    {
        return fName == null ? "(Unnamed)" : fName;
    }

    /** 
     * @return
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * @return
     */
    public Endianness getEndianness()
    {
        return fEndianness;
    }

    /**
     * @param handler
     */
    public void setDefaultEventHandlerFactory(DeviceEventHandlerFactory handler)
    {
        fDefaultHandlerFactory = handler;
    }

    /**
     *
     */
    public NodeConnection getConnection()
    {
        return fConnection;
    }

    /**
     * @param conn
     * @return
     */
    public NodeConnection connect(NodeConnection conn)
    {
        if (fConnection.isConnected())
        {
            throw new IllegalStateException(
                "Cannot connect while a current connection is active");
        }

        fConnection = conn;
        fConnection.setNode(this);

        try
        {
            conn.connect();
        }
        catch (InterruptedException e)
        {
            log.warn("Node {} failed to connect", this, e);
        }

        return fConnection;
    }

    /**
     * @param e
     */
    public void handleEvent(DeviceEvent e)
    {
        DeviceEventHandler handler = fHandlers.get(e.getDeviceId());
        if (handler == null)
        {
            handler = fDefaultHandlerFactory.build();
            fHandlers.put(e.getDeviceId(), handler);
        }

        handler.on(e);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Node<" + getIdentifier() + "=" + getName() + ">";
    }

    private final NodeIdentifier fIdentifier;
    private final Endianness fEndianness;

    private DeviceEventHandlerFactory fDefaultHandlerFactory = new DiscardHandler.DiscardHandlerFactory();
    private String fName;
    private Map<Integer, DeviceEventHandler> fHandlers = new HashMap<>();

    private NodeConnection fConnection = new DummyConnection();

    private static final Logger log = LoggerFactory.getLogger(Node.class);
}
