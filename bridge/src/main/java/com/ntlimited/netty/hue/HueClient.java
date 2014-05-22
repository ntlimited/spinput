package com.ntlimited.netty.hue;

import java.net.InetAddress;

import com.mastfrog.netty.http.client.HttpClient;
import com.mastfrog.netty.http.client.HttpClientBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.ntlimited.netty.hue.command.HueCommand;

/**
  *The HueClient is a single point for controlling the devices on
 * a Hue bridge.
 */
public final class HueClient
{
    public HueClient(InetAddress address, String username)
    {
        fAddress = address;
        fUsername = username;

        fClient = new HttpClientBuilder()
                    .threadCount(1)
                    .build();
    }

    public void command(HueCommand command)
    {
        command.execute(fClient);
    }

    public void shutdown()
    {
        fClient.shutdown();
    }

    public InetAddress getAddress()
    {
        return fAddress;
    }

    public String getUsername()
    {
        return fUsername;
    }

    private final HttpClient fClient;
    private final InetAddress fAddress;
    private final String fUsername;
    
    public static final int PORT = 80;
}
