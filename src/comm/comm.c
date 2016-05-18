#include <pebble.h>
#include "comm.h"
#include "ui/ui.h"
#include "i18n/i18n.h"

static char ticker[10];

/**
 * Initialize AppMessage handlers
 */
void init_appmessage_handlers() {
	app_message_register_inbox_received(in_received_handler);
	app_message_register_inbox_dropped(in_dropped_handler);
  app_message_register_outbox_sent(out_sent_handler);
	app_message_register_outbox_failed(out_failed_handler);

	app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
}

/**
 * Deinitialize AppMessage handlers
 */
void deinit_appmessage_handlers() {
	app_message_deregister_callbacks();
}

/**
 * Sends text to PebbleJS
 */
void watch_sends_text(const char *text) {
	DictionaryIterator *iter;

	app_message_outbox_begin(&iter);
	dict_write_cstring(iter, MESSAGE_KEY, text);
	dict_write_end(iter);
  app_message_outbox_send();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Sent MESSAGE_KEY to phone: %s", text);
}

/**
 * Send a status to PebbleJS
 */
void watch_sends_status(uint8_t status){
	DictionaryIterator *iter;

	app_message_outbox_begin(&iter);
	dict_write_uint8(iter, STATUS_KEY, status);
	dict_write_end(iter);
  app_message_outbox_send();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Sent STATUS_KEY to phone: %d", status);
}

/**
 * Send a command to the phone
 */
void watch_sends_command(uint8_t command){
	DictionaryIterator *iter;

	app_message_outbox_begin(&iter);
	dict_write_uint8(iter, COMMAND_KEY, command);
	dict_write_end(iter);
  app_message_outbox_send();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Sent COMMAND_KEY to phone: %d", command);
}

/**
 *
 */
char *translate_error(AppMessageResult result) {
  switch (result) {
    case APP_MSG_OK: return "APP_MSG_OK";
    case APP_MSG_SEND_TIMEOUT: return "APP_MSG_SEND_TIMEOUT";
    case APP_MSG_SEND_REJECTED: return "APP_MSG_SEND_REJECTED";
    case APP_MSG_NOT_CONNECTED: return "APP_MSG_NOT_CONNECTED";
    case APP_MSG_APP_NOT_RUNNING: return "APP_MSG_APP_NOT_RUNNING";
    case APP_MSG_INVALID_ARGS: return "APP_MSG_INVALID_ARGS";
    case APP_MSG_BUSY: return "APP_MSG_BUSY";
    case APP_MSG_BUFFER_OVERFLOW: return "APP_MSG_BUFFER_OVERFLOW";
    case APP_MSG_ALREADY_RELEASED: return "APP_MSG_ALREADY_RELEASED";
    case APP_MSG_CALLBACK_ALREADY_REGISTERED: return "APP_MSG_CALLBACK_ALREADY_REGISTERED";
    case APP_MSG_CALLBACK_NOT_REGISTERED: return "APP_MSG_CALLBACK_NOT_REGISTERED";
    case APP_MSG_OUT_OF_MEMORY: return "APP_MSG_OUT_OF_MEMORY";
    case APP_MSG_CLOSED: return "APP_MSG_CLOSED";
    case APP_MSG_INTERNAL_ERROR: return "APP_MSG_INTERNAL_ERROR";
    default: return "UNKNOWN ERROR";
  }
}

/**
 * Called when a message is received from the phone
 */
void in_received_handler(DictionaryIterator *received, void *context) {
	Tuple *tuple = NULL;
  tuple = malloc(sizeof(Tuple));

	tuple = dict_find(received, MESSAGE_KEY);
	if (tuple) {
		APP_LOG(APP_LOG_LEVEL_DEBUG, "Received MESSAGE_KEY from phone in %d bytes", tuple->length);
		APP_LOG(APP_LOG_LEVEL_DEBUG, "%s", tuple->value->cstring);
    // update ticker layer for the demo
    strncpy(ticker, tuple->value->cstring, 10);
    update_ticker(ticker);
    return;
	}

	tuple = dict_find(received, BROADCAST_KEY);
	if (tuple) {
		APP_LOG(APP_LOG_LEVEL_DEBUG, "Received BROADCAST_KEY from phone in %d bytes", tuple->length);
		APP_LOG(APP_LOG_LEVEL_DEBUG, "%s", tuple->value->cstring);
    // update ticker layer for the demo
    strncpy(ticker, tuple->value->cstring, 10);
    update_ticker(ticker);
    return;
	}

	tuple = dict_find(received, STATUS_KEY);
	if (tuple) {
		APP_LOG(APP_LOG_LEVEL_DEBUG, "Received STATUS_KEY from phone: %d", (int)tuple->value->uint8);
    switch((int)tuple->value->uint8) {
      case PHONE_WELCOMES:
        watch_sends_status(WATCH_GREETS);
        return;
      case DATA_SAVE_OK:
        update_status(i18n.data_save_ok);
        return;
      case DATA_SAVE_FAILED:
        update_status(i18n.data_save_failed);
        return;
      default:
        return;
    }
	}

	tuple = dict_find(received, COMMAND_KEY);
	if (tuple) {
		APP_LOG(APP_LOG_LEVEL_DEBUG, "Received COMMAND_KEY from phone: %d", (int)tuple->value->uint8);
    // possible commands: toggle measuring, short pulse
    if ((int)tuple->value->uint8 == TOGGLE_MEASURING_FROM_PHONE) {
      APP_LOG(APP_LOG_LEVEL_DEBUG, "Do TOGGLE_MEASURING_FROM_PHONE");
      toggle_measuring();
    }
    if ((int)tuple->value->uint8 == SHORT_PULSE) {
      APP_LOG(APP_LOG_LEVEL_DEBUG, "Do SHORT_PULSE");
      vibes_short_pulse();
    }
	}
}

/**
 *
 */
void in_dropped_handler(AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, "Failed to receive message");
}

/**
 *
 */
void out_sent_handler(DictionaryIterator *iterator, void *context) {
  //APP_LOG(APP_LOG_LEVEL_DEBUG, "Message sent");
}

/**
 *
 */
void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, "Failed sending message, reason: %s", translate_error(reason));
}

char *bool2str(bool var) {
  return var ? "true" : "false";
}
