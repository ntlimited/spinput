package com.ntlimited.netty.hue.command;

import com.mastfrog.netty.http.client.HttpClient;

public interface HueCommand
{
    public void execute(HttpClient client);
}
