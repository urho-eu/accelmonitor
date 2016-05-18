#include <pebble.h>
#include <inttypes.h>

#include "ui.h"
#include "../i18n/en.h"
#include "../comm/comm.h"
#include "../util/util.h"

static Window *window;

static Layer *page0_layer;
static TextLayer *header_layer;
static Layer *ticker_layer;
static TextLayer *timer_layer;

static GFont font_small, font_big, font_medium;

int seconds;
static char *current_ticker;
static char *timer_prefix;
static char *timer_text;
static bool countdown;
static bool measuring;
static AppTimer *countdown_timer;

static DataLoggingSessionRef accel_logger;
uint32_t logger_tag;

GRect header_frame, timer_frame, ticker_start_frame, ticker_end_frame;

/**
 * called when back btn pressed
 */
static void back_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, "back btn multiclicked");
  watch_sends_command(DMB_RECONNECT);
  watch_sends_text(i18n.pressed_back);
}

/**
 *
 */
static void update_timer() {
  if (countdown) {
    timer_prefix = i18n.prefix_countdown;
  }
  if (measuring) {
    timer_prefix = i18n.sampling_countdown;
  }
  snprintf(timer_text, 25, "%s %02d", timer_prefix, seconds);
  text_layer_set_text(timer_layer, timer_text);
  watch_sends_text(timer_text);
}

/**
 * Forward the data from the accelerometer
 */
static void accel_data_handler(AccelData *data, uint32_t num_samples) {
  DataLoggingResult res = data_logging_log(accel_logger, data, 1);
  if (res != DATA_LOGGING_SUCCESS) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "failed logging accel data: %d" + (int)res);
  }
}

/**
 * Countdown for pre-measuring and measuring phase
 */
static void countdown_handler(void *data) {
  static char log[] = "";

  update_timer();
  seconds--;

  if (seconds < 0 && countdown_timer) {
    if (countdown) {
      APP_LOG(APP_LOG_LEVEL_DEBUG, "measuring starts");
      watch_sends_text(i18n.measuring_starts);

      countdown = false;
      measuring = true;
      seconds = DEFAULT_SAMPLING;

      vibes_short_pulse();

      accel_service_set_sampling_rate(ACCEL_SAMPLING_25HZ);
      accel_data_service_subscribe(NUM_SAMPLES, accel_data_handler);

      if (countdown_timer) {
        app_timer_reschedule(countdown_timer, 1000);
      }

      accel_logger = data_logging_create(++logger_tag, DATA_LOGGING_BYTE_ARRAY, sizeof(AccelData) * NUM_SAMPLES, false);
    } else {
      if (measuring) {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "measuring ends");
        watch_sends_text(i18n.measuring_ends);

        measuring = false;

        data_logging_finish(accel_logger);
        accel_data_service_unsubscribe();

        vibes_double_pulse();

        if (countdown_timer) {
          app_timer_cancel(countdown_timer);
        }

        seconds = DEFAULT_COUNTDOWN;

        text_layer_set_text(timer_layer, i18n.finished);
      }
    }
  }

  if (countdown || (measuring && countdown_timer)) {
    countdown_timer = app_timer_register(1000, countdown_handler, NULL);
  }
}

/**
 * Starts or ends measuring
 */
void toggle_measuring() {
  if (measuring) {
    watch_sends_text(i18n.stopped_by_user);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "measuring stopped by user");

    measuring = false;

    data_logging_finish(accel_logger);

    accel_data_service_unsubscribe();
    app_timer_cancel(countdown_timer);

    window_set_background_color(window, GColorDarkGray);
    text_layer_set_text(timer_layer, i18n.stopped);

    vibes_double_pulse();

    seconds = DEFAULT_COUNTDOWN;
  } else {
    if (countdown) {
      APP_LOG(APP_LOG_LEVEL_DEBUG, "countdown stopped by user");
      watch_sends_text(i18n.measuring_ends);

      countdown = false;
      if (countdown_timer) {
        app_timer_cancel(countdown_timer);
      }
      text_layer_set_text(timer_layer, i18n.stopped);
    } else {
      APP_LOG(APP_LOG_LEVEL_DEBUG, "countdown starts by user");
      countdown = true;
      text_layer_set_text(timer_layer, i18n.started);
      seconds = DEFAULT_COUNTDOWN;
      countdown_handler(NULL);
    }
  }
}

/**
 * called when up btn pressed
 */
static void up_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, i18n.pressed_up);
  watch_sends_command(TOGGLE_MEASURING_FROM_WATCH);
  toggle_measuring();
}

/**
 * called when select btn pressed
 */
static void select_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, i18n.pressed_select);
  watch_sends_text(i18n.pressed_select);
}

/**
 * called when select btn pressed
 */
static void down_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, i18n.pressed_down);
  watch_sends_text(i18n.pressed_down);
}

/**
 * click handler subscriptions
 */
void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_BACK, back_single_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_single_click_handler);
  window_single_click_subscribe(BUTTON_ID_SELECT, select_single_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_single_click_handler);
}

/**
 *
 */
static void page0_update_proc(Layer *layer, GContext *ctx) {
  //APP_LOG(APP_LOG_LEVEL_DEBUG, "page0_update_proc called");
}

/**
 *
 */
static void ticker_update_proc(Layer *layer, GContext *ctx) {
  //APP_LOG(APP_LOG_LEVEL_DEBUG, "ticker_update_proc called");

  const GRect layer_bounds = layer_get_bounds(layer);

  int total_width = 0;

  GSize ticker_size = graphics_text_layout_get_content_size(
    current_ticker, font_medium, layer_bounds, GTextOverflowModeWordWrap, GTextAlignmentLeft);

  total_width += ticker_size.w;

  const int x_margin = (layer_bounds.size.w - total_width) / 2;
  const int y_margin = PBL_IF_RECT_ELSE(8, 2);
  const GRect ticker_rect = grect_inset(layer_bounds, GEdgeInsets(-y_margin, 0, 0, x_margin));

  graphics_context_set_text_color(ctx, DEFAULT_TICKER_COLOR);

  graphics_draw_text(ctx, current_ticker, font_medium, ticker_rect,
                     GTextOverflowModeWordWrap, GTextAlignmentLeft, NULL);
}

/**
 *
 */
void update_ticker(const char *text) {
  //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_ticker called");
  snprintf(current_ticker, 100, "%s", text);
  //APP_LOG(APP_LOG_LEVEL_DEBUG, sprintf("update_ticker called: %d", res));
  layer_mark_dirty(ticker_layer);
}

/**
 *
 */
void window_load(Window *window) {
  font_small = fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD);
  font_medium = fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD);
  font_big = fonts_get_system_font(FONT_KEY_BITHAM_30_BLACK);

  int size = 100;
  current_ticker = (char *) malloc(size);
  timer_prefix = (char *) malloc(20);
  timer_text = (char *) malloc(25);
  logger_tag = 0;

  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  page0_layer = layer_create(bounds);

  header_frame = GRect(0, 0, bounds.size.w, 30);
  header_layer = text_layer_create(header_frame);
  text_layer_set_font(header_layer, font_medium);
  text_layer_set_text_alignment(header_layer, GTextAlignmentCenter);
  text_layer_set_text(header_layer, i18n.title);

  ticker_start_frame = GRect(0, 40, bounds.size.w, 25);
  ticker_layer = layer_create(ticker_start_frame);

  timer_frame = GRect(0, 138, bounds.size.w, 30);
  timer_layer = text_layer_create(timer_frame);
  text_layer_set_font(timer_layer, font_medium);
  text_layer_set_text_alignment(timer_layer, GTextAlignmentCenter);
  text_layer_set_text(timer_layer, i18n.idle);

  // Set all update (redraw) proc for the layers
  layer_set_update_proc(page0_layer, page0_update_proc);
  layer_set_update_proc(ticker_layer, ticker_update_proc);

  layer_add_child(page0_layer, (Layer *)header_layer);
  layer_add_child(page0_layer, ticker_layer);
  layer_add_child(page0_layer, (Layer *)timer_layer);

  // Add all layers to parent window
  layer_add_child(window_layer, page0_layer);
}

/**
 *
 */
void window_unload(Window *window) {
  destroy();
}

/**
 *
 */
void window_push(void) {
  countdown = false;
  measuring = false;

  window = window_create();

  window_set_click_config_provider(window, click_config_provider);

  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });

  window_set_background_color(window, DEFAULT_BG_COLOR);
  window_stack_push(window, true);
}

/**
 *
 */
void window_redraw() {
  APP_LOG(APP_LOG_LEVEL_DEBUG, "window_redraw called");
  layer_mark_dirty(page0_layer);
}

/**
 *
 */
void destroy(void) {
  free(current_ticker);
  free(timer_prefix);
  free(timer_text);

  text_layer_destroy(header_layer);
  layer_destroy(ticker_layer);
  text_layer_destroy(timer_layer);
  layer_destroy(page0_layer);

  window_destroy(window);
}
