package com.ntlimited.spinput.node.connection.tcp;
import java.net.InetAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntlimited.spinput.node.Node;
import com.ntlimited.spinput.node.command.NodeCommand;
import com.ntlimited.spinput.node.connection.NodeConnection;

/**
 * NodeConnection implemented over TCP/IP
 */
public class TcpConnection implements NodeConnection
{
    /**
     * Create a connection that will connect to the given
     * address and port using a specified event loop grouping.
     *
     * @param address
     * @param port
     * @param g
     */
    public TcpConnection(InetAddress address, int port, EventLoopGroup g)
    {
        fAddress = address;
        fPort = port;
        fGroup = g;
    }

    /** {@inheritDoc} */
    @Override
    public void connect() throws InterruptedException
    {
        if (isConnected())
        {
            throw new IllegalStateException(
                "Cannot connect if already connected");
        }

        log.info("connecting to {}:{}", fAddress, fPort);

        fBootstrap = new Bootstrap();
        fBootstrap.group(fGroup)
                  .channel(NioSocketChannel.class)
                  .option(ChannelOption.SO_KEEPALIVE, true)
                  .handler(new TcpSpinputHandler(this));

        fFuture = fBootstrap.connect(fAddress, fPort)
                            .sync();

        log.info("connected");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConnected()
    {
        return fConnected;
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect()
    {
        fConnected = false;
    }

    /** {@inheritDoc} */
    @Override
    public void setNode(Node n)
    {
        fNode = n;
    }

    /** {@inheritDoc} */
    @Override
    public Node getNode()
    {
        return fNode;
    }

    /** {@inheritDoc} */
    @Override
    public void sendCommand(NodeCommand command)
    {
        byte[] serialized = command.serialize(getNode().getEndianness());
        ByteBuf buffer = Unpooled.buffer(4 + serialized.length)
                                 .order(getNode().getEndianness().getByteOrder());

        buffer.writeInt(command.getCommandType().toInt());
        buffer.writeBytes(serialized);

        fFuture.channel().writeAndFlush(buffer);

        log.info("wrote command through channel");
    }

    private InetAddress fAddress = null;
    private int fPort = 0;
    private boolean fConnected = false;

    private Bootstrap fBootstrap;
    private ChannelFuture fFuture;

    private final EventLoopGroup fGroup;
    private Node fNode;

    private static final Logger log = LoggerFactory.getLogger(TcpConnection.class);
}
