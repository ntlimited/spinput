#include <arpa/inet.h>
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <stdio.h>

#include "device.h"
#include "node.h"

#define MAX_DEVICES 4

#define TV_DIFF_MS(start,end) (((end.tv_sec - start.tv_sec)*1000) + ((end.tv_usec-start.tv_usec)/1000))

/**
 * Given a node struct, set up the client to prepare
 * for connecting to a bridge.
 */
int spi_initialize_node(struct spi_node *node, uint8_t identifier[16]) {
    memcpy(&node->identifier, identifier, 16);

    int err = 0;
    int found;
    socklen_t len = sizeof(struct sockaddr_in);
    int fd = -1;
    struct spi_device *devices = malloc(sizeof(struct spi_device) * MAX_DEVICES);
    struct sockaddr_in sin = {
        .sin_port = 0,
        .sin_addr = {
            .s_addr = INADDR_ANY
        },
        .sin_family = AF_INET
    };

    if (devices == NULL) {
        err = ENOMEM;
        goto exit;
    }
    
    err = spi_find_devices(MAX_DEVICES, &found, devices);
    if (err) {
        goto exit;
    }

    node->device_count = found > MAX_DEVICES ? MAX_DEVICES : found;
    node->devices = devices;

    fd = socket(AF_INET, SOCK_STREAM, 0);
    if (fd < 0) {
        err = -1;
        goto exit;
    }

    err = bind(fd, (struct sockaddr*)&sin, sizeof(struct sockaddr_in));
    if (err) {
        goto exit;
    }

    listen(fd, 1);
    getsockname(fd, (struct sockaddr*)&sin, &len);

    node->fd = fd;
    node->port = htons(sin.sin_port);

exit:
    if (err) {
        if (devices != NULL) {
            free(devices);
        }
        if (fd > -1) {
            close(fd);
        }
    }

    return err;
}

/**
 * Read events from all devices connected to the node, up to
 * `max_events` or until `millis` milliseconds have passed.
 */
int spi_read_node_events(const struct spi_node *node,
                         int max_events,
                         struct spi_event *events,
                         int *events_read,
                         long millis) {
    fd_set fdset;
    struct timeval start;
    struct timeval now;
    struct timeval tv;
    int max_fd = -1;
    int i;
    int evt_count = 0;
    int err = 0;

    FD_ZERO(&fdset);
    
    for (i = 0 ; i < node->device_count ; i++) {
        FD_SET(node->devices[i].fd, &fdset);
        if (node->devices[i].fd > max_fd) {
            max_fd = node->devices[i].fd;
        }
    }

    if (max_fd < 0) {
        err = -1;
        goto exit;
    }

    err = gettimeofday(&start, NULL);
    if (err) {
        goto exit;
    }

    while (millis > 0) {
        tv.tv_sec = (millis / 1000);
        tv.tv_usec = (millis % 1000) * 1000;

        i = select(max_fd + 1, &fdset, NULL, NULL, &tv);
        if (i < 0) {
            err = i;
            goto exit;
        }

        if (i > 0) {
            for (i = 0 ; i < node->device_count ; i++) {
                if (FD_ISSET(node->devices[i].fd, &fdset)) {
                    err = spi_device_read_sync(&node->devices[i], &events[evt_count++]);
                    if (err) {
                        goto exit;
                    }

                    // ignore empty events
                    if (events[evt_count - 1].event_type == EMPTY) {
                        evt_count--;
                    }
                    else {
                        events[evt_count - 1].device_id = i;
                    }

                    if (max_events == evt_count) {
                        goto exit;
                    }
                }
            }
        }
        FD_ZERO(&fdset);
        for (i = 0 ; i < node->device_count ; i++) {
            FD_SET(node->devices[i].fd, &fdset);
        }

        err = gettimeofday(&now, NULL);
        if (err) {
            goto exit;
        }
        millis -= TV_DIFF_MS(start, now);
        start = now;
    }

exit:
    *events_read = evt_count;
    return err;
}

/**
 * Send an LED command to all devices connected to the node.
 */
int spi_node_led_command(const struct spi_node *node, const struct spi_led_command *command) {
    int err;
    int i;

    for (i = 0 ; i < node->device_count ; i++) {
        spi_device_open(&node->devices[i]);
        err |= spi_send_led_command(&node->devices[i], command);
    }
    return err;
}
