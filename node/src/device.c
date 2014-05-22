/**
 * device.c: spinput utilities for finding/interacting with
 *	PowerMate devices
 */
#include <dirent.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdint.h>
#include <sys/time.h>
#include <stdio.h>

#include "device.h"

/**
 * Location to look for devices in
 */
static const char *DEVICE_DIRECTORY = "/dev/input/by-id/";

/**
 * /dev/input/by-id/ entries should contain the substring
 * below if they are PowerMate devices.
 */
static const char *POWERMATE_DEVICE_ID_SSTR = "Griffin_PowerMate";


/**
 * module-private method to return the difference in milliseconds
 * between two struct timevals
 */
static long spi_diff_ms(const struct timeval *start, const struct timeval *end) {
    return ((end->tv_sec - start->tv_sec) * 1000) +
           ((end->tv_usec - start->tv_usec)/1000);
}

/**
 * Find all PowerMates on the current system and return their
 * paths, up to max_devices. The number of devices found is
 * returned via the `found` parameter and as many fit are
 * stored in paths.
 *
 * Entries in the paths array must be 512+ bytes long for
 * memory safety.
 */
int spi_find_devices(int max_devices, int *found, struct spi_device *devices) {
	DIR *device_listing = NULL;
    struct dirent *ent;
	int retval = 0;
	int idx = 0;

    if (access(DEVICE_DIRECTORY, F_OK) == -1) {
        //retval = -1;
        goto exit;
    }
	
    device_listing = opendir(DEVICE_DIRECTORY);

	if (devices == NULL) {
		retval = EINVAL;
		goto exit;
	}

	while (NULL != (ent = readdir(device_listing))) {
		if (strstr(ent->d_name, POWERMATE_DEVICE_ID_SSTR) == NULL) {
			continue;
		}

		if (idx >= max_devices) {
			idx++;
			continue;
		}

		devices[idx].fd = -1;
		devices[idx].path[0] = '\0';
        devices[idx].id = idx;

		strcat(devices[idx].path, DEVICE_DIRECTORY);
		strcat(devices[idx].path, ent->d_name);

		idx++;
	}

	*found = idx;

exit:
	if (device_listing != NULL) {
		closedir(device_listing);
	}
	return retval;
}

/**
 * If the spi_device is not already opened, open it. An
 * error value is returned if the device could not be opened.
 */
int spi_device_open(struct spi_device *device) {
	int err = 0;
	int fd;

	if (device->fd >= 0) {
		goto exit;
	}

	fd = open(device->path, O_RDWR);

	if (fd < 0) {
		err = 1;
		goto exit;
	}

	device->fd = fd;

exit:
	return err;
}

/**
 * Close the fd for the spi_device if it is not already closed.
 * An error code is returned if the device was open and failed to
 * be closed.
 */
int spi_device_close(struct spi_device *device) {
	int err = 0;

	if (device->fd < 0) {
		goto exit;
	}

	err = close(device->fd);

	if (!err) {
		device->fd = -1;
	}

exit:
	return err;
}

/**
 * Return the next event to be read from the device, blocking until
 * an event is read.
 */
int spi_device_read_sync(const struct spi_device *device, struct spi_event *event) {
	struct input_event in_event;
	ssize_t bytes = read(device->fd, &in_event, sizeof(struct input_event));
	int err = 0;

	if (bytes != sizeof(struct input_event)) {
		err = 1;
		goto exit;
	}

	err = spi_convert_event(&in_event, event);
    if (!err) {
        event->device_id = device->id;
    }

exit:
	return err;
}

/**
 * Return the next event to be read from the device within
 * `wait_millis` milliseconds. If `wait_millis` is less than
 * 0, the API will block indefinitely. The return code is 0
 * on success, regardless of whether an event was read. However
 * if an even tis read then the *found value will be set to 1
 */
int spi_device_read(const struct spi_device *device, struct spi_event *event, int *found, long wait_millis) {
    fd_set fdset;
    FD_ZERO(&fdset);
    FD_SET(device->fd, &fdset);
	struct timeval tv = { wait_millis / 1000,
						  (wait_millis % 1000) * 1000 };
	
    int rval = select(device->fd + 1, &fdset, NULL, NULL, &tv);
	int err = 0;

	if (rval == -1) {
		err = -1;
		goto exit;
	}

	if (rval == 0) {
		*found = 0;
		goto exit;
	}

	rval = spi_device_read_sync(device, event);
	if (rval) {
		err = rval;
		goto exit;
	}
	*found = 1;

exit:
	return err;
}

/**
 * Sends the given LED status command to the specified device.
 * If the command is invalid or fails to be sent, a non-zero
 * return value is issued.
 */
int spi_send_led_command(const struct spi_device *device,
                         const struct spi_led_command *command) {
	struct input_event event = {
        .time = {0},
        .type = 4,
        .code = 1,
        .value = 0
    };
	int err = 0;

	if (command->pulse_speed < 0 || command->pulse_speed > 510) {
		err = -1;
		goto exit;
	}
	if (command->pulse_table < 0 || command->pulse_table > 2) {
		err = -2;
		goto exit;
	}

	event.value = ((command->static_brightness & 0xFF) |
				   (command->pulse_speed << 8) |
				   (command->pulse_table) << 17 |
				   ((!!command->pulse_on_sleep) << 19) |
				   ((!!command->pulse_on_wake) << 20));
	

	ssize_t written = write(device->fd, &event, sizeof(struct input_event));
	if (written != sizeof(struct input_event)) {
		err = -3;
		goto exit;
	}

exit:
	return err;
}

/**
 * Reads events from the device until eiher max_events or
 * max_millis is reached. If successful, the number of
 * events read is stored into the events_read pointer.
 */
int spi_collect_events(struct spi_device *device,
                       struct spi_event *events,
                       int *events_read,
                       int max_events,
                       long max_millis) {
    int err = 0;
    int read_count = 0;
    struct timeval start;
    struct timeval now;
    int found;

    if (max_events < 1) {
        *events_read = 0;
        goto exit;
    }

    err = gettimeofday(&start, NULL);
    if (err) {
        goto exit;
    }

    do {
        found = 0;
        err = spi_device_read(device, &events[read_count], &found, max_millis);

        if (err) {
            goto exit;
        }

        if (found && events[read_count].event_type != EMPTY) {
            read_count++;
            if (read_count == max_events) {
                break;
            }
        }

        err = gettimeofday(&now, NULL);
        if (err) {
            goto exit;
        }

        max_millis -= spi_diff_ms(&start, &now);
        start = now;

    } while (max_millis > 0 && read_count < max_events);
    *events_read = read_count;

exit:
    return err;
}

/**
 * Take a list of events and aggregate & filter it into
 * a smaller list.  This primarily involves removing empty
 * events, and collecting consecutive spin events of the
 * same direction into singular events of higher magnitude
 * with a duration set.
 */
int spi_aggregate_events(const struct spi_event *events,
                         struct spi_event *aggregate,
                         int event_count,
                         int *aggregate_count) {
    int err = 0;
    int aggregated = 0;
    int i;
    int coalescing_spin = 0;
    struct timeval coalesce_start;

    for (i = 0 ; i < event_count ; i++) {
        switch(events[i].event_type) {
            // ignore empty events, which are effectively just markers
            // between real events
            case EMPTY:
                continue;
            // click events are not post-processed but do terminate
            // coalescing of spin events.
            case CLICK:
                if (coalescing_spin) {
                    aggregate[aggregated - 1].duration = spi_diff_ms(&coalesce_start,
                                                                    &events[i-1].time);
                    coalescing_spin = 0;
                }
                aggregate[aggregated] = events[i];
                aggregated++;
                continue;
            // the primary purpose of aggregation is to take consecutive
            // spin events and convert them into a single event with a
            // higher magnitude (event.data) and a real duration
            case SPIN:
                if (coalescing_spin) {
                    if (aggregate[aggregated - 1].data < 0 &&
                        events[i].data < 0) {
                        aggregate[aggregated - 1].data += events[i].data;
                    }
                    else if (aggregate[aggregated - 1].data > 0 && 
                             events[i].data > 0) {
                        aggregate[aggregated - 1].data += events[i].data;
                    }
                    else {
                        aggregate[aggregated - 1].duration = spi_diff_ms(&coalesce_start,
                                                                         &events[i-1].time);
                        aggregate[aggregated] = events[i];
                        coalesce_start = events[i].time;
                        aggregated++;
                    }
                }
                else {
                    aggregate[aggregated] = events[i];
                    aggregate[aggregated - 1].duration = spi_diff_ms(&coalesce_start,
                                                                     &events[i-1].time);
                    coalesce_start = events[i].time;
                    aggregated++;
                }
                coalescing_spin = 1;
                continue;
            // unknown event types are problematic, so error out.
            default:
                err = -1;
                goto exit;
        }
    }

    if (coalescing_spin) {
        aggregate[aggregated - 1].duration = spi_diff_ms(&coalesce_start,
                                                         &events[i-1].time);
    }

    *aggregate_count = aggregated;

exit:
    return err;
}
