package com.ntlimited.spinput.discovery;

import java.net.InetAddress;

import io.netty.channel.socket.DatagramPacket;

import com.ntlimited.spinput.node.Endianness;
import com.ntlimited.spinput.node.Node;
import com.ntlimited.spinput.node.NodeIdentifier;

/**
 * A <code>DiscoveryMessage</code> is sent by the node daemon
 * to announce its presence to any listening bridge. The message
 * has a strict format as follows:
 * -bytes 0-15: node identifier
 * -bytes 16-17: node server port
 * -bytes 18-19: 16 bit checksum over bytes 0-17
 * -bytes 20-24: 32 bit checksum over bytes 0-19
 * -bytes 25-26: an endianness marker representing a short int
 *      equal to 0x00FF
 */
public final class DiscoveryMessage
{

    /**
     * @param address the address of the remote node
     * @param port the port the remote node is listening on
     * @param identifier the identifier for the remote node
     * @param e the endianness of the remote node
     */
    public DiscoveryMessage(InetAddress address,
                            int port,
                            NodeIdentifier identifier,
                            Endianness e)
    {
        fAddress = address;
        fPort = port;
        fIdentifier = identifier;
        fEndianness = e;
    }

    public NodeIdentifier getNodeIdentifier()
    {
        return fIdentifier;
    }

    public Node getNode()
    {
        return new Node(getNodeIdentifier(), getEndianness());
    }

    public InetAddress getHost()
    {
        return fAddress;
    }

    public int getPort()
    {
        return fPort;
    }

    public Endianness getEndianness()
    {
        return fEndianness;
    }

    public String toString()
    {
        return "Node<" + fIdentifier + "@" + fAddress + ":" + fPort + ">";
    }

    public static DiscoveryMessage parse(DatagramPacket packet)
    {
        byte[] contents = new byte[26];
        Endianness endian;
        
        packet.content().getBytes(0, contents);
        
        if (contents[25] == -1)
        {
            endian = Endianness.BIG;
        }
        else if (contents[24] == -1)
        {
            endian = Endianness.LITTLE;
        }
        else
        {
            throw new IllegalStateException("invalid marker bytes");
        }
        
        return parseAware(endian,
                          packet.sender().getAddress(),
                          contents);
    }

    /**
     *
     * @param e
     * @param addr
     * @param bytes
     */
    private static DiscoveryMessage parseAware(Endianness e,
                                               InetAddress addr,
                                               byte[] bytes)
    {
        int port = (int)e.parseBytes(bytes, 16, 2);
        long check16 = e.parseBytes(bytes, 18, 2);
        long check32 = e.parseBytes(bytes, 20, 4);

        long check = 0;
        for (int i = 0 ; i < 18 ; i += 2)
        {
            check ^= ((bytes[i]&0xFF)<<8) + (bytes[i+1]&0xFF);
        }
        if (e == Endianness.LITTLE)
        {
            check = Endianness.reverseEndianness(check, 2);
        }

        if (check != check16)
        {
            throw new IllegalArgumentException("invalid 16 bit checksum");
        }

        check = 0;
        for (int i = 0 ; i < 20 ; i += 4)
        {
            check ^= ((bytes[i]&0xFF)<<24) + ((bytes[i+1]&0xFF)<<16) +
                     ((bytes[i+2]&0xFF)<<8) + ((bytes[i+3]&0xFF));
        }
        if (e == Endianness.LITTLE)
        {
            check = Endianness.reverseEndianness(check, 4);
        }

        if (check != check32)
        {
            throw new IllegalArgumentException("invalid 32 bit checksum");
        }
        
        return new DiscoveryMessage(addr, port, readIdentifier(bytes), e);
    }

    /**
     * Converts bytes 0-15 of the array into a NodeIdentifier
     *
     * @param bytes
     * @return
     */
    private static NodeIdentifier readIdentifier(byte[] bytes)
    {
        byte[] identifier = new byte[16];
        for (int i = 0 ; i < 16 ; i++)
        {
            identifier[i] = bytes[i];
        }

        return new NodeIdentifier(identifier);
    }

    private final int fPort;
    private final Endianness fEndianness;
    private final InetAddress fAddress;
    private final NodeIdentifier fIdentifier;
}
