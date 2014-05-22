package com.ntlimited.spinput.node.event.handlers;

import java.util.concurrent.TimeUnit;

import com.mastfrog.netty.http.client.HttpClient;
import com.mastfrog.netty.http.client.HttpClientBuilder;
import com.mastfrog.netty.http.client.ResponseHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ntlimited.netty.hue.HueClient;
import com.ntlimited.netty.hue.brightness.BrightnessCycle;
import com.ntlimited.netty.hue.brightness.BrightnessCycleImpl;
import com.ntlimited.netty.hue.color.ColorCycle;
import com.ntlimited.netty.hue.color.ColorHue;
import com.ntlimited.netty.hue.color.HueMonoCycle;
import com.ntlimited.spinput.node.Node;
import com.ntlimited.spinput.node.NodeManager;
import com.ntlimited.spinput.node.command.LightCommand;
import com.ntlimited.spinput.node.event.ClickEvent;
import com.ntlimited.spinput.node.event.DeviceEvent;
import com.ntlimited.spinput.node.event.DeviceEventHandler;
import com.ntlimited.spinput.node.event.DeviceEventHandlerFactory;
import com.ntlimited.spinput.node.event.SpinEvent;

/**
 * Handles inputs from a specific device 
 */
public class HueHandler implements DeviceEventHandler
{
    public HueHandler(HueClient client, int group)
    {
        fClient = client;
        fGroup = group;

        fHttpClient = new HttpClientBuilder().threadCount(1).build();
    }

    /** {@inheritDoc} */
    @Override
    public void on(DeviceEvent evt)
    {
        switch (evt.getEventType())
        {
            case CLICK:
                handleClickEvent(evt);
                break;
            case SPIN:
                handleSpinEvent(evt);
                break;
            default:
                log.warn("Unknown event: {}", evt);
        }
    }

    private void handleClick(DeviceEvent evt)
    {
        setColorMode(true);
        
        if (fColorModeTimeout != null)
        {
            fColorModeTimeout.cancel();
        }
        else
        {
            evt.getNode().getConnection().sendCommand(
                LightCommand.ON(evt.getDeviceId()));
        }
        RevertColorStateTask task = new RevertColorStateTask(this,
                                                             evt.getNode(),
                                                             evt.getDeviceId());
        fColorModeTimeout = NodeManager.getTimer()
                                        .newTimeout(task,
                                                    3,
                                                    TimeUnit.SECONDS);

    }

    private void handleClickEvent(DeviceEvent evt)
    {
        aggregateState(evt.getNode(), evt.getDeviceId());
        setPressed(evt.getValue() == 1);

        if (isPressed())
        {
            fClickTimestamp = evt.getTimestamp();
        } 
        else
        {
            long delta = evt.getTimestamp() - fClickTimestamp;
            if (!fIgnoreClick && delta < 1500)
            {
                handleClick(evt);
            }
            fIgnoreClick = false;
        }
    }

    private void handleSpinEvent(DeviceEvent evt)
    {
        // weird device crap filtering
        if (Math.abs(evt.getValue()) > 100)
        {
            return;
        }

        if (fSpinMagnitude == 0)
        {
            fSpinStart = evt.getTimestamp();
        }
        fSpinMagnitude += evt.getValue();
        fLastSpin = evt.getTimestamp();

        if (isPressed() && fSpinMagnitude > 1)
        {
            fIgnoreClick = true;
        }

        if (isColorMode())
        {
            RevertColorStateTask task = new RevertColorStateTask(this,
                                                                 evt.getNode(),
                                                                 evt.getDeviceId());
            fColorModeTimeout.cancel();
            fColorModeTimeout = NodeManager.getTimer()
                                           .newTimeout(task,
                                                       5,
                                                       TimeUnit.SECONDS);
        }

        if (fAggregationTimeout != null)
        {
            fAggregationTimeout.cancel();
            fAggregationTimeout = null;
        }
        if (evt.getTimestamp() - fSpinStart > 500)
        {
            aggregateState(evt.getNode(), evt.getDeviceId());
        }
        else
        {
            AggregateTask task = new AggregateTask(this,
                                                   evt.getNode(),
                                                   evt.getDeviceId());
            fAggregationTimeout = NodeManager.getTimer()
                                             .newTimeout(task,
                                                         500,
                                                         TimeUnit.MILLISECONDS);
        }
    }

    public boolean isColorMode()
    {
        return fColorMode;
    }

    public void setColorMode(boolean colors)
    {
        fColorMode = colors;
    }

    public boolean isAllMode()
    {
        return fAllMode;
    }

    public void setAllMode(boolean all)
    {
        fAllMode = all;
    }

    public boolean isPressed()
    {
        return fPressed;
    }

    public void setPressed(boolean pressed)
    {
        fPressed = pressed;
    }

    /**
     * Called by the timer to allow interaction with lights.
     */
    public synchronized void aggregateState(Node n, int deviceId)
    {
        if (fAggregationTimeout != null)
        {
            fAggregationTimeout.cancel();
            clearAggregationTimeout();
        }

        if (fSpinMagnitude == 0)
        {
            return;
        }

        int group = isPressed() ? 0 : fGroup;

        if (isColorMode())
        {
            spinColors(fColor.cycle(fSpinMagnitude), group);
        }
        else
        {
            spinBrightness(fBrightness.cycle(fSpinMagnitude), group);
        }

        fSpinMagnitude = 0;
    }

    public void clearColorTimeout()
    {
        fColorModeTimeout = null;
    }

    public void clearAggregationTimeout()
    {
        fAggregationTimeout = null;
    }

    private void spinColors(ColorHue hue, int group)
    {
        String body = "{\"hue\":" + hue.getHue() + ", \"sat\":" + hue.getSaturation() + "}";
        log.info("color to {}", hue);
        try
        {
            fHttpClient.put()
                       .setURL("http://10.0.1.18/api/newdeveloper/groups/" + group + "/action")
                       .setBody(body, com.google.common.net.MediaType.PLAIN_TEXT_UTF_8)
                       .execute();
        }
        catch (Exception e)
        {
            log.warn("temperature set failed", e);
        }
    }

    private void spinBrightness(int brightness, int group)
    {
        String body;

        if (brightness < 0)
        {
            body = "{\"on\":false}";
        }
        else
        {
            body = "{\"on\":true,\"bri\":" + brightness + "}";
        }
        
        log.info("brightness to {}", brightness);

        try
        {
            fHttpClient.put()
                       .setURL("http://10.0.1.18/api/newdeveloper/groups/" + group + "/action")
                       .setBody(body, com.google.common.net.MediaType.PLAIN_TEXT_UTF_8)
                       .execute();
        }
        catch (Exception e)
        {
            log.warn("temperature set failed", e);
        }
    }

    public static class Factory implements DeviceEventHandlerFactory
    {
        public Factory(HueClient client, int group)
        {
            fInstance = new HueHandler(client, group);
        }
        public DeviceEventHandler build()
        {
            return fInstance;
        }

        private final HueHandler fInstance;
    }

    private final BrightnessCycle fBrightness = new BrightnessCycleImpl(0, 64, false);
    private final ColorCycle<ColorHue> fColor = new HueMonoCycle(new ColorHue(46920, 0), 64);
    private final HttpClient fHttpClient;

    private final HueClient fClient;
    private final int fGroup;
    private int fEventsSincePressed = 0;
    private long fClickTimestamp = 0L;
    private long fSpinStart = 0L;
    private long fLastSpin = 0L;
    private int fSpinMagnitude = 0;

    private boolean fColorMode = false;
    private boolean fAllMode = false;
    private boolean fPressed = false;
    private boolean fIgnoreClick = false;

    private Timeout fColorModeTimeout = null;
    private Timeout fAggregationTimeout = null;

    private static final Logger log = LoggerFactory.getLogger(HueHandler.class);
}
