package com.ntlimited.spinput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntlimited.spinput.config.Configuration;
import com.ntlimited.spinput.discovery.DiscoveryServer;
import com.ntlimited.spinput.discovery.NodeRegisteringCallback;
import com.ntlimited.spinput.node.NodeManager;
import com.ntlimited.spinput.web.ControlServer;

/**
 * Driver for the Spinput Bridge daemon. Bootstraps the Netty
 * server components for the two primary listener functions of
 * the daemon:
 *  -discovery listener: UDP server that waits for spinput
 *      node broadcasts
 *  -web listener: webserver for managing/configuring the
 *      bridge
 *
 * In addition to Netty components, this sets up the interpreters
 * and handles various config options.
 */
public class Driver
{
    public static void main(String[] args) throws Exception
    {
        String configFilePath;

        if (args.length < 1)
        {
            configFilePath = DEFAULT_CONFIG_PATH;
        }
        else
        {
            configFilePath = args[0];
        }

        log.info("Using config path \"{}\"", configFilePath);
        Configuration.configure(configFilePath);

        NodeManager manager = new NodeManager(null);
        DiscoveryServer server = new DiscoveryServer(
            new NodeRegisteringCallback(manager));
        ControlServer controls = new ControlServer(8088);

        log.info("Installing discovery server");
        server.install();
        log.info("Installing control server");
        controls.install();

        log.info("waiting forever");
        server.waitForServer();
    }

    public static final String DEFAULT_CONFIG_PATH = "./config/spinput.xml";
    
    private static final Logger log = LoggerFactory.getLogger(Driver.class);
}
