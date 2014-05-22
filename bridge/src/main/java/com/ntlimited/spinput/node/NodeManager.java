package com.ntlimited.spinput.node;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntlimited.spinput.node.connection.tcp.TcpConnection;
import com.ntlimited.spinput.node.event.DeviceEventHandlerFactory;
import com.ntlimited.spinput.node.event.handlers.HueHandler;
import com.ntlimited.spinput.persistence.PersistenceManager;

public final class NodeManager
{
    public NodeManager(PersistenceManager persistence)
    {
        fPersistence = persistence;
        fMapping = new HashMap<>();
    }

    /** 
     * Retrieve a Node by its identifier, or <code>null</code>
     * if the identifier is not known.
     */
    public Node getNode(NodeIdentifier identifier)
    {
        return fMapping.get(identifier);
    }

    /**
     * Add a new node to the manager at the given address and port.
     * The node is not necessarily 'new' in the sense of being
     * unknown.
     */
    public void addNode(Node n, InetAddress address, int port)
    {
        if (getNode(n.getIdentifier()) != null)
        {
            log.info("node {} detected, but already known", getNode(n.getIdentifier()));
        }
        else
        {
            log.info("new node detected: {}", n);
            n.setName("Living Room");
            n.setDefaultEventHandlerFactory(fFactory);

            fMapping.put(n.getIdentifier(), n);
        }

        if (!fMapping.get(n.getIdentifier()).getConnection().isConnected())
        {
            getNode(n.getIdentifier()).connect(
                new TcpConnection(address, port, fGroup));
        }
    }

    /**
     * Get the shared timer used to handle delayed node-related
     * events.
     */
    public static Timer getTimer()
    {
        return kTimer;
    }

    private static Timer kTimer = new HashedWheelTimer();

    private final PersistenceManager fPersistence;
    private final Map<NodeIdentifier, Node> fMapping;
    private final EventLoopGroup fGroup = new NioEventLoopGroup(2);

    private final DeviceEventHandlerFactory fFactory =
        new HueHandler.Factory(null, 2);

    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);
}
