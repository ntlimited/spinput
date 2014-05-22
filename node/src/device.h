#ifndef _SPI_DEVICE_H_
#define _SPI_DEVICE_H_

#include "spinput.h"

/**
 * An spi_device has the path that identifies the device
 * uniquely, an fd corresponding used to read/write to the
 * char device (if opened) and an fdset used for time-limited
 * or event based reads. If the device is closed, its fd is
 * set to a value less than 0.
 */
struct spi_device {
	char path[512];
	int fd;
    int id;
};

int spi_find_devices(int max_devices, int *found, struct spi_device *devices);

int spi_device_open(struct spi_device *device);
int spi_device_close(struct spi_device *device);

int spi_device_read_sync(const struct spi_device *device, struct spi_event *event);
int spi_device_read(const struct spi_device *device, struct spi_event *event, int *read, long wait_millis);

int spi_send_led_command(const struct spi_device *device, const struct spi_led_command *command);

int spi_collect_events(struct spi_device *device, struct spi_event *events, int *events_reads, int max_events, long max_millis);

int spi_aggregate_events(const struct spi_event *events, struct spi_event *aggregate, int event_count, int *aggregate_count);

#endif
