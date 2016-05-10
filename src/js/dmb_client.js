/**
 *
 * Simple integration with DMB
 *
 */
var config = require("config");
var io = require("socket.io");
var watch = require("watch");

var dmb = config.dmb;
dmb.socket = null;

dmb.init = function(token) {
  console.log('dmb init called with: ' + token);

  if (typeof(token) !== 'undefined')
  {
    if (! dmb.socket) {
      dmb.socket = io(config.dmb.server);

      if (typeof dmb.params.bkid == 'undefined' || dmb.params.bkid == '')
      {
        // the default channel
        dmb.params.bkid = 'accelmonitor';
      }

      dmb.params.token = token;

      // say hello to DMB
      dmb.socket.emit('dmb:connect', dmb.params);

      // want to hook into this one?
      dmb.socket.on('disconnect', function(msg)
      {
        console.log('dmb.socket disconnect');
      });

      // want to hook into this one?
      dmb.socket.on('dmb:connected', function(msg)
      {
        console.log('dmb.socket dmb:connected');
      });

      // received a broadcast from DMB
      dmb.socket.on('dmb:broadcast', function(msg)
      {
        console.log('dmb.socket dmb:broadcast: ' + msg);
        watch.send({'BROADCAST_KEY': msg});
      });

      // received a direct message from DMB
      dmb.socket.on('dmb:message', function(msg)
      {
        console.log('dmb.socket dmb:message: ' + msg);
        watch.send({'MESSAGE_KEY': msg});
      });

      // received data from DMB
      dmb.socket.on('dmb:data', function(msg) {
        console.log('dmb.socket dmb:data');
      });
    } else {
      console.log('dmb.socket already active');
    }
  }
};

dmb.deinit = function(token) {
  console.log('dmb deinit called with: ' + token);
  if (typeof dmb.socket !== 'undefined') {
    // say bye to DMB
    dmb.socket.emit('disconnect');
    dmb.socket.disconnect();
    dmb.socket = null;
  }
}

dmb.message = function(token, text) {
  console.log('dmb message called with: ' + text);
  dmb.params.token = token;
  dmb.params.payload = text;
  dmb.socket.emit('dmb:message', dmb.params);
  console.log(JSON.stringify(dmb.params));
}

module.exports = dmb;
