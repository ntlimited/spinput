package com.ntlimited.spinput.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class StatusHandler implements HttpHandler
{
    @Override
    public void handleHttpRequest(HttpRequest request,
                                  HttpResponse response,
                                  HttpControl control)
    {
        log.info("Received request for {}", request.getUri());

        control.nextHandler();
    }

    private static final Logger log = LoggerFactory.getLogger(StatusHandler.class);
}
