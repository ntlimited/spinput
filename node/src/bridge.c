#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>

#include "spinput.h"
#include "bridge.h"
#include "device.h"

#define TV_FROM_MILLIS(millis) { .tv_sec = (millis/1000), .tv_usec = (millis%1000)*1000 }

static const uint16_t ENDIANNESS = 0x00FF;

/**
 * Broadcast node to any listening bridges. Payload is 26
 * bytes long, consisting of the node identifier (16 bytes),
 * the port the node is listening on (2 bytes), a 2 byte XOR
 * checksum over the first 18 bytes, a 4 byte XOR
 * payload over the first 20 bytes, and finally a two byte
 * endianness marker in the last two bytes.
 */
int spi_broadcast(struct spi_node *node) {
    int err = 0;
    uint8_t message[26] = {0};
    uint16_t checksum16 = 0;
    uint32_t checksum32 = 0;
    int fd = -1;
    int i;
    struct sockaddr_in addr = {
        .sin_family = AF_INET,
        .sin_port = htons(BROADCAST_PORT),
        .sin_addr = {
            .s_addr = htonl(INADDR_BROADCAST)
        }
    };

    memcpy(&message[0], node->identifier, 16);
    memcpy(&message[16], &node->port, 2);

    for (i = 0 ; i < 9 ; i++) {
        checksum16 ^= ((uint16_t*)message)[i];
    }
    
    memcpy(&message[18], &checksum16, 2);

    for (i = 0 ; i < 5 ; i++) {
        checksum32 ^= ((uint32_t*)message)[i];
    }

    memcpy(&message[20], &checksum32, 4);
    memcpy(&message[24], &ENDIANNESS, 2);

    fd = socket(AF_INET, SOCK_DGRAM, 0);

    if (fd < 0) {
        err = -1;
        goto exit;
    }

    i = 1;
    err = setsockopt(fd, SOL_SOCKET, SO_BROADCAST, &i, sizeof(i));
    if (err) {
        goto exit;
    }

    i = sendto(fd, message, 26, 0, (struct sockaddr*)&addr, sizeof(addr));
    if (i < 0) {
        err = i;
        goto exit;
    }

exit:
    if (fd > -1) {
        close(fd);
    }
    return err;
}

/**
 * Accept a connection on the node's socket, initializing the
 * bridge with handshake data. If no connection is made, the
 * call will return successfully but with the success flag set
 * to 0.
 */
int spi_await_bridge(const struct spi_node *node,
                     struct spi_bridge *bridge,
                     int *success,
                     long wait_millis) {
    int err = 0;
    int result;
    struct timeval tv = {
        .tv_sec = (wait_millis / 1000),
        .tv_usec = (wait_millis % 1000) * 1000
    };
    fd_set fdset;

    FD_ZERO(&fdset);
    FD_SET(node->fd, &fdset);

    result = select(node->fd + 1, &fdset, NULL, NULL, &tv);
    if (result < 0) {
        err = -1;
        goto exit;
    }
    if (result == 0) {
        *success = 0;
        goto exit;
    }

    bridge->fd = accept(node->fd, NULL, NULL);
    bridge->command_buffer_offset = 0;
    bridge->has_command = 0;

    FD_CLR(node->fd, &fdset);
    *success = 1;

exit:
    return err;
}

/**
 * Waits for the bridge to send a command to the node
 */
int spi_await_command(struct spi_bridge *bridge,
                      const struct spi_node *node,
                      long wait_millis) {
    struct timeval tv = TV_FROM_MILLIS(wait_millis);
    int err = 0;
    int i;
    ssize_t bytes_read;
    fd_set fdset;

    FD_ZERO(&fdset);
    FD_SET(bridge->fd, &fdset);

    i = select(bridge->fd + 1, &fdset, NULL, NULL, &tv);

    if (i < 0) {
        err = -1;
        goto exit;
    }

    if (i) {
        bytes_read = read(bridge->fd,
                          bridge->command_buffer + bridge->command_buffer_offset,
                          COMMAND_BUFFER_SIZE - bridge->command_buffer_offset);

        if (bytes_read <= 0) {
            err = -1;
            goto exit;
        }

        bridge->command_buffer_offset += bytes_read;

        if (!bridge->has_command) {
            if (bridge->command_buffer_offset >=
                sizeof(struct spi_bridge_command_header)) {

                memcpy(&bridge->command,
                       bridge->command_buffer,
                       sizeof(struct spi_bridge_command_header));
                for (i = sizeof(struct spi_bridge_command_header) ;
                     i < bridge->command_buffer_offset ;
                     i++) {

                    bridge->command_buffer[i-sizeof(struct spi_bridge_command_header)] =
                        bridge->command_buffer[i];
                }

                bridge->has_command = 1;
                bridge->command_buffer_offset -= sizeof(struct spi_bridge_command_header);
            }
        }

        if (bridge->has_command) {
            switch (bridge->command.command_type) {
                case LIGHT:
                    err = spi_parse_led_command(bridge, node);
                    break;
                case WIFI:
                    err = spi_parse_wifi_command(bridge);
                    break;
            }
        }
    }

exit:
    return err;
}

int spi_parse_wifi_command(struct spi_bridge *bridge) {
    return 0;
}

int spi_parse_led_command(struct spi_bridge *bridge, const struct spi_node *node) {
    struct spi_bridge_command_led cmd;
    int err = 0;
    int i;

    if (bridge->command_buffer_offset <
        sizeof(struct spi_bridge_command_led)) {

        goto exit;
    }
    
    memcpy(&cmd, bridge->command_buffer, sizeof(struct spi_bridge_command_led));

    for (i = sizeof(struct spi_bridge_command_led) ;
         i < bridge->command_buffer_offset ;
         i++) {

        bridge->command_buffer[i-sizeof(struct spi_bridge_command_led)] =
            bridge->command_buffer[i];
    }

    bridge->command_buffer_offset -= sizeof(struct spi_bridge_command_led);
    bridge->has_command = 0;

    for (i = 0 ; i < node->device_count ; i++) {
        if (node->devices[i].id == cmd.device_id) {
            err = spi_send_led_command(&node->devices[cmd.device_id],
                                       &cmd.led_command);
            break;
        }
    }

exit:
    return err;
}

/**
 * Serializes and sends events to the bridge. Failure means that the bridge
 * received only a partial message and the node needs to revert to broadcast
 * state.
 */
int spi_send_event_stream(const struct spi_bridge *bridge,
                          const struct spi_node *node,
                          const struct spi_event *events,
                          int event_count) {
    struct spi_event_stream_header header_msg;
    struct spi_event_stream_event event_msg;
    int err = 0;
    int i;
    
    if (!event_count) {
        goto exit;
    }

    //memcpy(&header_msg.node_identifier, &node->identifier, 16);
    header_msg.event_count = event_count;

    if (sizeof(struct spi_event_stream_header) !=
        write(bridge->fd, &header_msg, sizeof(struct spi_event_stream_header))) {

        err = -1;
        goto exit;
    }

    for (i = 0 ; i < event_count ; i++) {
        memcpy(&event_msg.event, &events[i], sizeof(struct spi_event));

        if (sizeof(struct spi_event_stream_event) !=
            write(bridge->fd, &event_msg, sizeof(struct spi_event_stream_event))) {
            err = -1;
            goto exit;
        }
    }

exit:
    return err;
}
