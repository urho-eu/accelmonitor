/**
 * AccelMonitor - Pebble phone app
 *
 * This code will run on the phone within the MyPebble app's sandbox
 *
 */
var util2 = require("util2");
var myutil = require("myutil");
var watch = require("watch");
var dmb = require("dmb_client");
var config = require("config");
var mytoken = Pebble.getWatchToken();

// Called when JS is ready
Pebble.addEventListener("ready",
  function(e) {
    // connect to web socket
    dmb.init(mytoken);
    //
    console.log('Pebble app ready');
});

// Called when incoming message from the watch app is received
Pebble.addEventListener("appmessage",
  function(e) {
    console.log("Received from watch: " + util2.toString(e.payload));

    if (typeof e.payload.COMMAND_KEY !== 'undefined')
    {
      switch(e.payload.COMMAND_KEY)
      {
        case config.apiCommands.DMB_CONNECT:
          // TODO: check if we have active connection or not
          console.log('Watch requested DMB_CONNECT');
          dmb.init(mytoken);
          break;
        case config.apiCommands.DMB_DISCONNECT:
          // TODO: check if we have active connection or not
          console.log('Watch requested DMB_DISCONNECT');
          dmb.deinit(mytoken);
          break;
        case config.apiCommands.DMB_RECONNECT:
          // TODO: check if we have active connection or not
          console.log('Watch requested DMB_RECONNECT');
          dmb.deinit(mytoken);
          dmb.init(mytoken);
          break;
        default:
          console.log("Unknown API command: " + util2.toString(e.payload.COMMAND_KEY));
      }
    }


    if (typeof e.payload.MESSAGE_KEY !== 'undefined')
    {
      console.log('Watch sent MESSAGE_KEY: ' + e.payload.MESSAGE_KEY);
      dmb.message(mytoken, e.payload.MESSAGE_KEY);
    }
});

/**
 * load config page
 */
Pebble.addEventListener("showConfiguration", function(e)
{
  var apiSettings = config.api.settings;
  var fromStorage = localStorage.getItem("apiSettings") || apiSettings;

  try
  {
    apiSettings = JSON.parse(fromStorage);
  }
  catch(e)
  {
    console.log('Parsing local storage data failed: ' + util2.toString(e.message));
  }

  console.log('fromStorage: ' + fromStorage);
  console.log('apiSettings: ' + util2.toString(apiSettings));

  var configPage = apiSettings.server;
  console.log('Load config page from: ' + configPage);

  Pebble.openURL(configPage);
});

/**
 * check and save api settings to local storage
 */
Pebble.addEventListener("webviewclosed", function(e)
{
  var value = '';
  var apiSettings = config.api.settings;

  /**
   * the reply should look like something like this:
   * server=xxxx&apikey=xxxx&apiuid=xxxx
   */
  console.log('Raw response from settings page: ' + e.response);

  var reply = e.response.split('&');

  // check, sanitize, always :)
  if (reply.length > 0)
  {
    if (reply[0].indexOf('=') !== -1)
    {
      value = reply[0].split('=')[1];
      if (value != '') apiSettings.server = value;
    }
    console.log('Save parsed settings: ' + util2.toString(apiSettings));

    // save to local storage
    localStorage.setItem('apiSettings', util2.toString(apiSettings));

    // send watch a command so that it can recheck the API
    watch.send({'COMMAND_KEY': config.watchCommands.WATCH_CHECK_API_ACCESS});
  }
  else
  {
    console.log('Invalid settings received: ' + e.response);
  }
});
