#pragma once

#include <pebble.h>

#define DEFAULT_BG_COLOR GColorOrange //GColorBlack
#define DEFAULT_TICKER_COLOR GColorSpringBud
#define DEFAULT_FILL_COLOR GColorPastelYellow
#define DEFAULT_INVERSE_FILL_COLOR GColorWhite

#define DEFAULT_COUNTDOWN 3
#define DEFAULT_SAMPLING 5
#define NUM_SAMPLES 5

void window_push();
void window_redraw();
void destroy();

void update_ticker(const char *text);
void update_status(const char *text);
//void update_timer(uint8_t seconds);
void toggle_measuring();
