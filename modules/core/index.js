/*
  * @flow
 */

'use strict';

import { NativeModules, NativeAppEventEmitter, DeviceEventEmitter, Platform } from 'react-native';
const MarsCoreModule = NativeModules.MarsCoreModule;

const emitter = (Platform.OS === 'android' ? DeviceEventEmitter : NativeAppEventEmitter)
let cb_onpush = null;

DeviceEventEmitter.addListener('networkStatusDidChange', (resp) => {
    MarsCoreModule.notifyNetworkChange();
});

DeviceEventEmitter.addListener('onMarsPush', (resp) => {
    if (cb_onpush) {
      cb_onpush(resp)
    }
});

const init = (
  shortLinkPort,
  longLinkHost,
  longLinkPorts,
  clientVersion,
) => {
  const sport = shortLinkPort || 8080;
  const lhost = longLinkHost || 'marsopen.cn';
  const lports = longLinkPorts || [8081];
  const cversion = clientVersion || 1

  const profile = {
    'longLinkHost' : lhost, 
    'shortLinkPort' : sport,
    'longLinkPorts' : lports,
    'clientVersion' : cversion
  }
  MarsCoreModule.init(profile);
}

const post = (host, cgi, data, short_support, long_support, cmdid) => {

  const s_support = short_support == undefined ? true : short_support;
  const l_support = long_support == undefined ? false : long_support;
  const c_cmdid = cmdid == undefined ? -1 : cmdid;
  const properties = {
    'host' : host,
    'cgi_path': cgi,
    'cmd_id': c_cmdid,
    'short_support' : s_support, 
    'long_support' : l_support
  }
  return MarsCoreModule.send([... data], properties)
}

const setOnPushListener = (listener) => {
    cb_onpush = listener;
}

module.exports = {
  init,
  post,
  setOnPushListener,
}