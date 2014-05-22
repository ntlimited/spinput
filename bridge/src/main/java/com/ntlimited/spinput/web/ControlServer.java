package com.ntlimited.spinput.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;

import com.ntlimited.spinput.web.handler.StatusHandler;

//import com.ntlimited.spinput.web.handlers.DataHandler;
//import com.ntlimited.spinput.web.handlers.StatusHandler;

public class ControlServer
{
    public ControlServer(int port)
    {
        fPort = port;
    }

    public void install()
    {
        fServer = WebServers.createWebServer(fPort) 
//            .add("/data", new DataHandler(this))
            .add("/status", new StatusHandler())
            .add(new StaticFileHandler("/web"));
        fServer.start();

        log.info("installed");
    }

    private WebServer fServer;
    private final int fPort;

    private static final Logger log = LoggerFactory.getLogger(ControlServer.class);

    public static final String STATIC_PATH = "/static";
}
