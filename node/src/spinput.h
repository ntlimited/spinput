#ifndef _SPI_SPINPUT_H_
#define _SPI_SPINPUT_H_

#include <stdint.h>
#include <linux/input.h>
#include <sys/time.h>

enum spi_event_type {
	EMPTY = 0,
	CLICK = 1,
	SPIN = 2
};

// same structure as struct input_event
struct spi_event {
	enum spi_event_type event_type;
    int device_id;
	int data;
    int duration;
    struct timeval time;
};

struct spi_led_command {
	int static_brightness;
	int pulse_speed;
	int pulse_table;
	int pulse_on_sleep;
	int pulse_on_wake;
};

struct spi_config {
    long aggregation_window_millis;
};

int spi_convert_event(struct input_event *std_evt, struct spi_event *spi_evt);
int spi_get_identifier(const char *location, uint8_t *identifier);

#endif
