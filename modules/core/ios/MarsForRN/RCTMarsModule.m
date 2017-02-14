
#import "RCTMarsModule.h"
#import "RCTEventDispatcher.h"
#import "RCTLog.h"
#import "RCTConvert.h"

#import "PublicComponentV2/app_callback.h"
#import "PublicComponentV2/stn_callback.h"

#import <mars/app/app_logic.h>
#import <mars/baseevent/base_logic.h>


#import "stnproto_logic.h"

#import "Bussiness/NetworkService.h"
#import "Bussiness/NetworkEvent.h"
#import "Bussiness/NetworkStatus.h"



@implementation RCTMarsModule

RCT_EXPORT_MODULE();


RCT_EXPORT_METHOD(init:(NSDictionary *)profile)
{
    
//    [NetworkService sharedInstance].delegate = [[NetworkEvent alloc] init];
    [[NetworkService sharedInstance] setCallBack];
    [[NetworkService sharedInstance] createMars];
    [[NetworkService sharedInstance] setClientVersion:[RCTConvert NSInteger:profile[@"clientVersion"]]];
    [[NetworkService sharedInstance] setLongLinkAddress:[RCTConvert NSString:profile[@"longLinkHost"]] port:[RCTConvert NSArray:profile[@"longLinkPorts"]][0]];
    [[NetworkService sharedInstance] setShortLinkPort:[RCTConvert NSInteger:profile[@"shortLinkPort"]]];
    [[NetworkService sharedInstance] reportEvent_OnForeground:YES];
    [[NetworkService sharedInstance] makesureLongLinkConnect];
    
    [[NetworkStatus sharedInstance] Start:[NetworkService sharedInstance]];
    
}

RCT_EXPORT_METHOD(notifyNetworkChange)
{
    [[NetworkService sharedInstance] reportEvent_OnNetworkChange];
}


RCT_EXPORT_METHOD(send:(NSArray *)data properties:(NSDictionary *)properties resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    //CGITask * task = startTask(data, properties,
                                    //() => {onTaskResponse: (data) => {resolve(data)}, onTaskEnd:(errType, errcode) => {if (errType || errCode) reject(err)} });
    //
}
//property mEnableStat = false;
RCT_EXPORT_METHOD(setStatisticEnable:(BOOL)enable)
{
    //mEnableStat = enable;
}

- (NSDictionary *)constantsToExport
{
    return @{ @"PUSHMSG_CMDID": @10001,
              @"FLOW_CMDID": @10002,
              @"CONNSTATUS_CMDID": @10003,
              @"CGIHISTORY_CMDID": @10004,
              @"SDTRESULT_CMDID": @10005,
              };
}

- (void)dealloc{
     [[NetworkService sharedInstance] destroyMars];
}



+ (void) onPush{
    
    //triggerRNEvent("onMarsPush", NSDict{ @"cmdid" : cmdid, @"buffer" : data });
    
}

+ (void) onMarsStat
{
    //if (mEnableStat)
     //triggerRNEvent("onMarsStat", NSDict{ @"cmdid" : cmdid, @"stat" : data });
}



@end
