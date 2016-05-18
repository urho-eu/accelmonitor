#pragma once
/**
 * Dictionary for watch <-> phone (PebbleJS) communication
 */
enum {
  STATUS_KEY = 0,
  COMMAND_KEY = 1,
  MESSAGE_KEY = 2,
  BROADCAST_KEY = 3,
};

// statuses / commands / messages from phone to watch
// see also js/config.json
typedef enum {
  PHONE_WELCOMES = 1,
  API_WELCOMES = 51,
  TOGGLE_MEASURING_FROM_PHONE = 52,
  SHORT_PULSE = 61,
  DATA_SAVE_OK = 71,
  DATA_SAVE_FAILED = 72,
} WatchCommand;

// statues / commands / messages from watch to phone
// see also js/config.json
typedef enum {
  WATCH_GREETS = 11,
  WATCH_SENDS_TEXT = 31,
  DMB_CONNECT = 32,
  DMB_DISCONNECT = 33,
  DMB_RECONNECT = 34,
  TOGGLE_MEASURING_FROM_WATCH = 41,
  SAVE_TO_FILE = 42,
} PhoneCommand;

/**
 * For our persistent storage on the watch
 */
typedef enum {
  DUMMY = 0,
} PersistentStorage;

// init, deinit
void init_appmessage_handlers();
void deinit_appmessage_handlers();

// Watch -> Phone (PebbleJS)
void watch_sends_text(const char *text);
void watch_sends_status(uint8_t status);
void watch_sends_command(uint8_t command);

char *translate_error(AppMessageResult result);
// Phone -> Watch
void in_received_handler(DictionaryIterator *received, void *context);
void in_dropped_handler(AppMessageResult reason, void *context);
// Watch -> Phone
void out_sent_handler(DictionaryIterator *iterator, void *context);
void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context);

char *bool2str(bool var);
