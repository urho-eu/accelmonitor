#include <pebble.h>

#include "ui/ui.h"
#include "util/util.h"
#include "comm/comm.h"

void init() {
  init_appmessage_handlers();
  window_push();
}

void deinit() {
  deinit_appmessage_handlers();
  destroy();
}

int main() {
  init();
  app_event_loop();
}
