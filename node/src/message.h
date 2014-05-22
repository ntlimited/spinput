#ifndef _SPI_MESSAGE_H_
#define _SPI_MESSAGE_H_

#include <stdint.h>

#include "spinput.h"

enum spi_message_type {
    EOM = 0,
    LED_COMMAND = 1,
    EVENT_NOTIFICATION = 0
};

struct spi_message_header {
    enum spi_message_type message_type;

    char payload[0];
};

struct spi_message_led_command {
    struct spi_led_command command;
    uint64_t device_mask;
    uint64_t duration_millis;
    uint32_t cancellation_token;

    struct spi_message_header next;
};

struct spi_message_event {
    struct spi_event event;

    struct spi_message_header next;
};

struct spi_message_wifi {
    char network_ssid[128];
    char network_key[128];

    struct spi_message_header next;
};

#endif
