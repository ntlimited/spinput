package com.ntlimited.spinput.node.connection.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntlimited.spinput.node.event.ClickEvent;
import com.ntlimited.spinput.node.event.DeviceEvent;
import com.ntlimited.spinput.node.event.SpinEvent;
import com.ntlimited.spinput.node.connection.NodeConnection;

/**
 * The NodeProtocolHandler interprets messages from the spinput
 * daemon on (usually) remote hosts. Messages are in the following
 * format.
 *
 * -header: 4 bytes, the number of events following
 * -payload (N) 24 bytes: a series of N payloads equal to the number of
 *      events specified in the header. Each payload is 28 bytes long;
 *      -event type (4 bytes)
 *      -device ID (4 bytes)
 *      -data (4 bytes)
 *      -duration (4 bytes, milliseconds, usually 0 for discrete events)
 *      -time, 16 bytes (4 bytes seconds, 4 bytes microseconds)
 *
 * The endianness of the data is determined by the endianness of the node.
 */
public class NodeProtocolHandler extends ChannelInboundHandlerAdapter
{
    /**
     * Create a protocol handler belonging to the given connection.
     *
     * @param conn the connection the handler is attached to
     */
    public NodeProtocolHandler(NodeConnection conn)
    {
        fConnection = conn;
        fEventsIncoming = 0;
    }

    /**
     * Deserialize payloads from the node.
     *
     * @param ctx the context for the current channel
     * @param msg the message in the channel, a bytebuf instance
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        ByteBuf buf = ((ByteBuf)msg).order(fConnection.getNode()
                                                      .getEndianness()
                                                      .getByteOrder());

        readEventStream(buf);
    }

    /**
     * Called when the channel disconnects, updates the status of the
     * local node object's connection.
     *
     * @param ctx the channel's context.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx)
    {
        log.warn("lost connection to node {}", fConnection.getNode());
        fConnection.disconnect();
    }

    /**
     * Read events from the endian-corrected byte buffer.
     *
     * @param buf the buffer to read events from
     */
    private void readEventStream(ByteBuf buf)
    {
        if (fEventsIncoming == 0)
        {
            fEventsIncoming = buf.readInt();
        }

        while (buf.readableBytes() >= 24 && fEventsIncoming > 0)
        {
            int eventType = buf.readInt();
            int deviceId = buf.readInt();
            int data = buf.readInt();
            int duration = buf.readInt();
            long timeSeconds = buf.readInt();
            long timeMicros = buf.readInt();

            long timestamp = (timeSeconds * 1000) + (timeMicros/1000);
            DeviceEvent evt = null;

            fEventsIncoming--;

            log.info("reading event from device");

            switch (eventType)
            {
                case 0:
                    continue;
                case 1:
                    evt = new ClickEvent(fConnection.getNode(),
                                         deviceId,
                                         data,
                                         timestamp);
                    break;
                case 2:
                    evt = new SpinEvent(fConnection.getNode(),
                                        deviceId,
                                        data,
                                        duration,
                                        timestamp);
                    break;
                default:
                    continue;
            }

            fConnection.getNode().handleEvent(evt);
        }
    }

    /**
     * Number of events following the last header received.
     */
    private int fEventsIncoming;

    private final NodeConnection fConnection;

    private static final Logger log = LoggerFactory.getLogger(NodeProtocolHandler.class);
}
