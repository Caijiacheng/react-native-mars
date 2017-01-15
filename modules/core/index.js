/*
  * @flow
 */

'use strict';

import { NativeModules, NativeAppEventEmitter, DeviceEventEmitter, Platform } from 'react-native';
const MarsCoreModule = NativeModules.MarsCoreModule;

const emitter = (Platform.OS === 'android' ? DeviceEventEmitter : NativeAppEventEmitter)

DeviceEventEmitter.addListener('networkStatusDidChange', (resp) => {
    MarsCoreModule.notifyNetworkChange();
});

const init = (
  shortLinkPort,
  longLinkHost,
  longLinkPorts,
  clientVersion,
) => {
  const sport = shortLinkPort && 80;
  const lhost = longLinkHost && 'localhost';
  const lports = longLinkPorts && [90];
  const cversion = clientVersion && '1.0.0'

  const profile = {
    'longLinkHost' : lhost, 
    'shortLinkPort' : sport,
    'longLinkPorts' : lports,
    'clientVersion' : cversion
  }
  MarsCoreModule.init(profile);
}

const post = (host, cgi, data, cmdid, short_support, long_support) => {

  const s_support = short_support && true;
  const l_support = long_support && false;
  const c_cmdid = cmdid && -1;

  const properties = {
    'host' : host,
    'cgi_path': cgi,
    'cmd_id': c_cmdid,
    'short_support' : s_support, 
    'long_support' : l_support
  }
  return MarsCoreModule.send(data, properties)
}

module.exports = {
  init,
  post,
}