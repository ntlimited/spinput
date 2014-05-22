package com.ntlimited.spinput.discovery;

import java.net.InetAddress;

import com.ntlimited.spinput.node.Node;

public interface DiscoveryCallback
{
    /**
     * Called when a new Node is found.
     */
    public void nodeDetected(Node n,
                             InetAddress host,
                             int port);
}
