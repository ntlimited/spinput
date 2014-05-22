#ifndef _SPI_NODE_H_
#define _SPI_NODE_H_

#include <stdint.h>

#include "spinput.h"
#include "device.h"

/**
 * A node has a unique identifier that is consistent across
 * executions of the daemon. Its port is the port that its
 * TCP server socket is listening on and fd is a handle to
 * the server socket itself.
 *
 * Device count is the number of spi_devices that are part
 * of the node, and devices is the actual handle to the
 * instances.
 */
struct spi_node {
    uint8_t identifier[16];
    uint16_t port;

    int fd;

    int device_count;
    struct spi_device *devices;
};

int spi_initialize_node(struct spi_node *node, uint8_t identifier[16]);
int spi_read_node_events(const struct spi_node *node, int max_events, struct spi_event *events, int *events_read, long millis);
int spi_node_led_command(const struct spi_node *node, const struct spi_led_command *command);
#endif
