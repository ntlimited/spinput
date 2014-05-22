#include <linux/input.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>

#include "spinput.h"
#include "bridge.h"
#include "node.h"
#include "device.h"

/**
 * Used to set LEDs to flashing state before bridge connection is made
 */
static const struct spi_led_command BRIDGE_WAITING = {
    .static_brightness = 32,
    .pulse_speed = 255,
    .pulse_table = 1,
    .pulse_on_sleep = 1,
    .pulse_on_wake = 1
};

/**
 * Used to turn off LEDs once bridge connects in
 */
static const struct spi_led_command BRIDGE_FOUND = {
    .static_brightness = 0,
    .pulse_speed = 0,
    .pulse_table = 0,
    .pulse_on_sleep = 0,
    .pulse_on_wake = 0
};

/**
 * Given a node and its connected bridge, send data to it indefinitely
 * and exit out when the bridge disconnects.
 */
int spi_event_loop(const struct spi_node *node, struct spi_bridge *bridge) {
    #define MAX_EVENTS 32
    #define TIMEOUT_MS 500
    int err = 0;
    struct spi_event events[MAX_EVENTS];
    int read;

    while (!err) {
        err = spi_read_node_events(node,
                                   MAX_EVENTS,
                                   events,
                                   &read,
                                   TIMEOUT_MS);
        if (err) {
            goto exit;
        }

        err = spi_send_event_stream(bridge, node, events, read);
        if (err) {
            goto exit;
        }

        err = spi_await_command(bridge, node, 0L);
    }

exit:
    return err;
    #undef MAX_EVENTS
}

int spi_find_bridge(struct spi_node *node, struct spi_bridge *bridge) {
    int success = 0;

    while (!success) {
        spi_broadcast(node);
        spi_await_bridge(node, bridge, &success, 10000);
    }

    return 0;
}

int main(int argc, char **argv) {
    struct spi_node node = {0};
    struct spi_bridge bridge = {0};
    uint8_t node_identifier[16];
    int err = 0;

    // get an identifier for the node
    err = spi_get_identifier("/opt/spinput", node_identifier);
    if (err) {
        fprintf(stderr, "could not read/create identifier for node\n");
        goto exit;
    }
    
    // initialize the node with its identifier
    err = spi_initialize_node(&node, node_identifier);
    if (err) {
        fprintf(stderr, "failed to initialize node\n");
        goto exit;
    }

    if (node.device_count == 0) {
        fprintf(stderr, "no devices located\n");
        err = -1;
        goto exit;
    }

    while (1) {
        spi_node_led_command(&node, &BRIDGE_WAITING);
        spi_find_bridge(&node, &bridge);
        spi_node_led_command(&node, &BRIDGE_FOUND);
        spi_event_loop(&node, &bridge);
    }

exit:
    return err;
}
