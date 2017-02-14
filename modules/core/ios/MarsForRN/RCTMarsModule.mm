
#import "RCTMarsModule.h"
#import "RCTEventDispatcher.h"
//#import "RCTLog.h"
#import "RCTConvert.h"
//#import "RCTBridge.h"
//#import "RCTEventDispatcher.h"

#import "PublicComponentV2/app_callback.h"
#import "PublicComponentV2/stn_callback.h"

#import <mars/app/app_logic.h>
#import <mars/baseevent/base_logic.h>


#import "stnproto_logic.h"

#import "NetworkService.h"
//#import "NetworkEvent.h"
#import "NetworkStatus.h"

#import "CGITask.h"


@interface RCTMarsModule () <NetworkDelegate, UINotifyDelegate>

@property (nonatomic, assign) BOOL mEnableStat;
@property (atomic, strong) NSMutableDictionary *taskCallbackMap;

@end

@implementation RCTMarsModule

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (instancetype)init
{
  self = [super init];
  if (self) {
    self.mEnableStat = NO;
    self.taskCallbackMap = [NSMutableDictionary dictionary];
  }
  return self;
}

RCT_EXPORT_METHOD(init:(NSDictionary *)profile)
{
    [NetworkService sharedInstance].delegate = self;
    [[NetworkService sharedInstance] setCallBack];
    [[NetworkService sharedInstance] createMars];
    [[NetworkService sharedInstance] setClientVersion:[RCTConvert NSInteger:profile[@"clientVersion"]]];
    [[NetworkService sharedInstance] setLongLinkAddress:[RCTConvert NSString:profile[@"longLinkHost"]]
                                                   port:[[RCTConvert NSArray:profile[@"longLinkPorts"]][0] unsignedShortValue]];
    [[NetworkService sharedInstance] setShortLinkPort:[RCTConvert NSInteger:profile[@"shortLinkPort"]]];
    [[NetworkService sharedInstance] reportEvent_OnForeground:YES];
    [[NetworkService sharedInstance] makesureLongLinkConnect];
    
    [[NetworkStatus sharedInstance] Start:[NetworkService sharedInstance]];
    
}

RCT_EXPORT_METHOD(notifyNetworkChange)
{
    [[NetworkService sharedInstance] reportEvent_OnNetworkChange];
}


RCT_EXPORT_METHOD(send:(NSArray *)dataArray
                  properties:(NSDictionary *)properties
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  NSData *data = [self toData:dataArray];
  
  BOOL short_support = [properties[@"short_support"] boolValue];
  BOOL long_support = [properties[@"long_support"] boolValue];
  NSInteger cmd_id = [properties[@"cmd_id"] integerValue];
  NSString *cgi_path = properties[@"cgi_path"];
  NSString *host = properties[@"host"];
  
  ChannelType conn = ChannelType_ShortConn;
  if (short_support==NO && long_support==NO) {
    NSLog(@"invalid channel strategy");
    @throw [NSException exceptionWithName:@"ERR_INVALID_CHANNEL -1001" reason:nil userInfo:nil];
  }
  if (short_support==YES && long_support==NO) {
    conn = ChannelType_ShortConn;
  }
  if (short_support==NO && long_support==YES) {
    conn = ChannelType_LongConn;
  }
  if (short_support==YES && long_support==YES) {
    conn = ChannelType_All;
  }
  
  CGITask *convlstCGI = [[CGITask alloc] initAll:conn
                                        AndCmdId:cmd_id
                                       AndCGIUri:cgi_path
                                         AndHost:host];
  int taskID = [[NetworkService sharedInstance] startTask:convlstCGI ForUI:self];
  
  [self.taskCallbackMap setValue:@{@"resolve": resolve, @"reject": reject} forKey:[NSString stringWithFormat:@"%d", taskID]];
  
  
  
  
//  [[NetworkService sharedInstance] startTask:<#(CGITask *)#> ForUI:<#(id<UINotifyDelegate>)#>]
    //CGITask * task = startTask(data, properties,
                                    //() => {onTaskResponse: (data) => {resolve(data)}, onTaskEnd:(errType, errcode) => {if (errType || errCode) reject(err)} });
    //
}

RCT_EXPORT_METHOD(setStatisticEnable:(BOOL)enable)
{
  self.mEnableStat = enable;
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

#pragma mark - NetworkDelegate

- (void)OnPushWithCmd:(NSInteger)cid data:(NSData *)data {
  [self.bridge.eventDispatcher sendAppEventWithName:@"onMarsPush"
                                               body:@{@"cmdid": @(cid),
                                                      @"buffer": [self toArray:data]}];
}

- (void)OnConnectionStatusChange:(int32_t)status longConnStatus:(int32_t)longConnStatus {
  if (self.mEnableStat) {
    [self.bridge.eventDispatcher sendAppEventWithName:@"CONNSTATUS_CMDID"
                                                 body:[NSString stringWithFormat:@"status: %d, longlinkstatus: %d", status, longConnStatus]];
  }
}

- (NSData *)toData:(NSArray *)array {
  NSMutableData *data = [NSMutableData dataWithCapacity:array.count];
  
  for (NSNumber *num in array) {
    int i = [num intValue];
    char c[1] = {1};
    c[0] = i;
    
    [data appendBytes:c length:1];
  }
  return data;
}

- (NSArray *)toArray:(NSData *)data {
  NSMutableArray *array = [NSMutableArray arrayWithCapacity:data.length];
  
  NSInteger index = 0;
  while (index >= data.length) {
    char *i;
    [data getBytes:&i range:NSMakeRange(index, 1)];
    
    [array addObject:@(i[0])];
    
    
    index ++;
  }
  
  return array;
}

#pragma mark - UINotifyDelegate

- (NSData *)requestSendData {
  // TODO
  return nil;
}

- (int)onPostDecode:(NSData*)responseData {
  // TODO
  return 0;
}

- (int)onTaskEnd:(uint32_t)tid errType:(uint32_t)errtype errCode:(uint32_t)errcode {
  NSDictionary *callbackMap = self.taskCallbackMap[[NSString stringWithFormat:@"%d", tid]];
  
  RCTPromiseResolveBlock resolve = callbackMap[@"resolve"];
  RCTPromiseRejectBlock reject = callbackMap[@"reject"];
  
  // TODO
  
  return 0;
}

@end
