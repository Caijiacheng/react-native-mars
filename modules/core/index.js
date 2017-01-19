/*
  * @flow
 */

'use strict';

import { NativeModules, NativeAppEventEmitter, DeviceEventEmitter, Platform } from 'react-native';
const MarsCoreModule = NativeModules.MarsCoreModule;

const emitter = (Platform.OS === 'android' ? DeviceEventEmitter : NativeAppEventEmitter)
let cb_onpush = null;
let cb_onstat = null;

emitter.addListener('networkStatusDidChange', (resp) => {
    MarsCoreModule.notifyNetworkChange();
});

emitter.addListener('onMarsPush', (resp) => {
    if (cb_onpush) {
      cb_onpush(resp)
    }
});

emitter.addListener('onMarsStat', (resp) => {
    if (cb_onstat) {
      cb_onstat(resp)
    }
})


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

const setOnStatListener = (listener) => {
    cb_onstat = listener;
    if (cb_onstat) {
      MarsCoreModule.setStatisticEnable(true)
    }else{
      MarsCoreModule.setStatisticEnable(false)
    }
}

const constant = {
    PUSHMSG_CMDID : MarsCoreModule.PUSHMSG_CMDID,
    FLOW_CMDID : MarsCoreModule.FLOW_CMDID,
    CONNSTATUS_CMDID : MarsCoreModule.CONNSTATUS_CMDID,
    CGIHISTORY_CMDID : MarsCoreModule.CGIHISTORY_CMDID,
    SDTRESULT_CMDID : MarsCoreModule.SDTRESULT_CMDID
} 


module.exports = {
  init,
  post,
  setOnPushListener,
  setOnStatListener,
  constant
}