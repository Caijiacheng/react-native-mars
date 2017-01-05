/*
  * @flow
 */

'use strict';

import { NativeModules, NativeAppEventEmitter, DeviceEventEmitter, Platform } from 'react-native';
const MarsCoreModule = NativeModules.MarsCoreModule;

const emitter = (Platform.OS === 'android' ? DeviceEventEmitter : NativeAppEventEmitter)

const uploadFile = (
  token: string,
  filePath: string,
) => {
  if (!QiNiuBridge) {
    console.error('QiNiuBridge 原生部分未加载');
    throw new Error('上传组件加载失败')
  }

  let funcObj = {};
  let promise = _upload(token, filePath, funcObj);

  promise.progress = (fn) => {
    funcObj.onProgress = fn;
    return promise;
  }

  return promise;
}

const _upload = async(token: string, filePath: string, funcObj: Object): any => {
  // on progress event listener
  let key = 'UploadProgress-'+filePath;
  let subscription = emitter.addListener(key, (body) => {
    if(funcObj.onProgress) {
      funcObj.onProgress(body.percent);
    }
  })
  try{
    let response = await QiNiuBridge.uploadFile(token, filePath);
    subscription.remove();
    return response;
  }catch(err){
      subscription.remove();
      throw err;
  }
}

module.exports = {
  uploadFile,
}