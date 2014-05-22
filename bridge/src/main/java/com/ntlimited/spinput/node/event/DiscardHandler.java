package com.ntlimited.spinput.node.event;

public final class DiscardHandler implements DeviceEventHandler
{
    private DiscardHandler() {}

    public void on(DeviceEvent e)
    {
    }

    public static DeviceEventHandler getInstance()
    {
        return kInstance;
    }

    public static class DiscardHandlerFactory implements DeviceEventHandlerFactory
    {
        @Override
        public DeviceEventHandler build()
        {
            return kInstance;
        }
    }

    private static final DiscardHandler kInstance = new DiscardHandler();
}
