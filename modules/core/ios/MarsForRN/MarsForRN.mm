//
//  MarsForRN.m
//  MarsForRN
//
//  Created by shihui on 2017/1/19.
//  Copyright © 2017年 Caijiacheng. All rights reserved.
//

#import "MarsForRN.h"

#include <mars/xlog/xlogger.h>
#include <mars/xlog/appender.h>

@implementation MarsForRN

+ (void)test {
  
  xlogger_SetLevel(kLevelError);
  
  NSLog(@"abc");
}

@end
