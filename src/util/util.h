#pragma once

#include <pebble.h>

typedef enum {
  FontSizeSmall = 0,
  FontSizeMedium,
  FontSizeLarge
} FontSize;

struct tm* get_tm();

GFont get_font(FontSize size);
