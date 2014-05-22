package com.ntlimited.spinput.discovery;

import java.util.Objects;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDP server that listens for packets from spinput Nodes
 * and decides what to do based on configuration.
 */
public final class DiscoveryServer extends SimpleChannelInboundHandler<DatagramPacket>
{
    public DiscoveryServer(DiscoveryCallback callback)
    {
        this(callback, PORT);
    }

    public DiscoveryServer(DiscoveryCallback callback, int port)
    {
        fCallback = Objects.requireNonNull(callback, "callback may not be null");
        fPort = port;
        
        fInstalled = false;
    }

    /**
     * Install the server onto the 
     */
    public void install() throws Exception
    {
        if (fInstalled)
        {
            throw new IllegalStateException(
                "Cannot install server that has been already installed");
        }
        fLoop = new NioEventLoopGroup();
        fBootstrap = new Bootstrap();
        fBootstrap.group(fLoop)
                  .channel(NioDatagramChannel.class)
                  .option(ChannelOption.SO_BROADCAST, true)
                  .handler(this);

        fFuture = fBootstrap.bind(fPort).sync();
        fInstalled = true;
    }

    public void waitForServer() throws InterruptedException
    {
        if (!fInstalled)
        {
            throw new IllegalStateException(
                "Server has not yet been installed");
        }

        try
        {
            fFuture.channel().closeFuture().await();
        }
        finally
        {
            fLoop.shutdownGracefully();
        }

        fInstalled = false;
    }

    /** {@inheritDoc} */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
    {
        log.info("UDP broadcast received from {}", packet.sender());
        
        try
        {
            DiscoveryMessage msg = DiscoveryMessage.parse(packet);

            log.info("discovery packet {} recognized, handing off", msg);
        
            fCallback.nodeDetected(msg.getNode(),
                                   msg.getHost(),
                                   msg.getPort());
        }
        catch (Exception e)
        {
            log.warn("caught bad packet: {}", e.getMessage(), e);
        }
        finally
        {
        }
    }

    /** {@inheritDoc} */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
    {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable t)
    {
        t.printStackTrace();
    }

    private final int fPort;
    private final DiscoveryCallback fCallback;

    private EventLoopGroup fLoop;
    private Bootstrap fBootstrap;
    private ChannelFuture fFuture;
    private boolean fInstalled;

    public static final int PORT = 7787;

    private static final Logger log = LoggerFactory.getLogger(DiscoveryServer.class);
}
