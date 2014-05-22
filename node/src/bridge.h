#ifndef _SPI_BRIDGE_H_
#define _SPI_BRIDGE_H_

#include <stdint.h>

#include "node.h"

#define BROADCAST_PORT 7787
#define COMMAND_BUFFER_SIZE 256

struct spi_event_stream_header {
    int event_count;
};

struct spi_event_stream_event {
    struct spi_event event;
};

enum spi_command_type {
    LIGHT = 0,
    WIFI = 1,
};

struct spi_bridge_command_header {
    enum spi_command_type command_type;
    char payload[0];
};

struct spi_bridge_command_led {
    int device_id;
    struct spi_led_command led_command;
};

struct spi_bridge_command_wifi {
    char ssid[64];
    char key[64];
};

/**
 * a bridge is the master that the spi node sends updates
 * on input events to. While nodes broadcast using multicast,
 * the bridge's fd is a TCP connection accepted by the node's
 * server socket.
 */
struct spi_bridge {
    char name[256];
    struct spi_bridge_command_header command;
    uint8_t command_buffer[COMMAND_BUFFER_SIZE];
    int command_buffer_offset;
    int has_command;
    int fd;
};


int spi_broadcast(struct spi_node *node);
int spi_await_bridge(const struct spi_node *node, struct spi_bridge *bridge, int *success, long wait_millis);
int spi_await_command(struct spi_bridge *bridge, const struct spi_node *node, long wait_millis);
int spi_parse_led_command(struct spi_bridge *bridge, const struct spi_node *node);
int spi_parse_wifi_command(struct spi_bridge *bridge);
int spi_send_event_stream(const struct spi_bridge *bridge, const struct spi_node *node, const struct spi_event *events, int event_count);

#endif
