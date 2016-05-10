#pragma once

/**
 *
 */
typedef struct default_i18n {
  char *title;
  char *pressed_back;
  char *pressed_up;
  char *pressed_select;
  char *pressed_down;
  char *idle;
  char *started;
  char *stopped;
  char *finished;
  char *prefix_countdown;
  char *prefix_measuring;
} __attribute__((__packed__)) default_i18n;
