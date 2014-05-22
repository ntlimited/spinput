package com.ntlimited.spinput.node.connection.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Channel initializer to add the protoocl handler
 * to channel's pipelines.
 */
public final class TcpSpinputHandler extends ChannelInitializer<SocketChannel>
{
    /**
     * Create a handler that will use the given parent.
     *
     * @param parent the parent to use for handlers created
     */
    public TcpSpinputHandler(TcpConnection parent)
    {
        fParent = parent;
    }

    /**
     * Initialize a channel by adding a protocol handler to it.
     * 
     * @param ch the channel to initialize
     */
    @Override
    public void initChannel(SocketChannel ch)
    {
        ch.pipeline().addLast(new NodeProtocolHandler(fParent));
    }

    private final TcpConnection fParent;
}
