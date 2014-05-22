package com.ntlimited.spinput.discovery;

import java.net.InetAddress;

import com.ntlimited.spinput.node.Node;
import com.ntlimited.spinput.node.NodeManager;

public class NodeRegisteringCallback implements DiscoveryCallback
{
    public NodeRegisteringCallback(NodeManager manager)
    {
        fManager = manager;
    }

    @Override
    public void nodeDetected(Node n, InetAddress host, int port)
    {
        fManager.addNode(n, host, port);
    }

    private final NodeManager fManager;
}
