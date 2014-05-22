#include <errno.h>
#include <stdint.h>
#include <time.h>
#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>

#include "spinput.h"

/**
 * Convert a standard linux input_event struct into a
 * spinput spi_event struct. An error code will be
 * returned if the input event does not match the
 * expected format of known Griffin PowerMate
 * outputs under the standard PowerMate kernel
 * driver.
 */
int spi_convert_event(struct input_event *std_evt,
					  struct spi_event *spi_evt) {
	int retval = 0;

	switch (std_evt->type) {
        // events of type 0 are empty but events of type 4 are
        // technically not. However, for the purposes of the
        // spinput daemon they can be ignored as well.
		case 0:
        case 4:
			spi_evt->event_type = EMPTY;
			spi_evt->data = 0;
			break;
		case 1:
			spi_evt->event_type = CLICK;
			spi_evt->data = std_evt->value;
			break;
		case 2:
			spi_evt->event_type = SPIN;
			spi_evt->data = std_evt->value;
			break;
		default:
			retval = EINVAL;
	}
    
    spi_evt->time = std_evt->time;
    // an un-aggregated event has a duration of 0
    spi_evt->duration = 0;

	return retval;
}

/**
 * Gets the unique identifier for the system. If one does not already
 * exist, a new value is created.
 */
int spi_get_identifier(const char *location, uint8_t *identifier) {
    FILE *handle = NULL;
    int err = 0;
    int i;
    
    if (-1 == access(location, F_OK)) {
        // create new identifier & file
        srand(time(NULL));

        for (i = 0 ; i < 4 ; i++) {
            ((int*)identifier)[i] = rand();
        }

        handle = fopen(location, "w");
        if (16 != fwrite(identifier, 1, 16, handle)) {
            err = -1;
            goto exit;
        }
    }
    else {
        if (-1 == access(location, R_OK)) {
            err = -1;
            goto exit;
        }
        handle = fopen(location, "r");
        if (16 != fread(identifier, 1, 16, handle)) {
            err = -1;
            goto exit;
        }
    }

exit:
    if (handle != NULL) {
        fclose(handle);
    }
    return err;
}
