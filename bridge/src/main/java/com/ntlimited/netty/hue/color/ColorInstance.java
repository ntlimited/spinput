package com.ntlimited.netty.hue.color;

import java.util.Map;

/**
 * Parent of the three types of color specifications
 * supported by Hue
 */
public interface ColorInstance
{
    Map<String,String> getSetter();
}
