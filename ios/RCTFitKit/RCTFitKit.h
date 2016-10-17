//
//  RCTFitKit.h
//  RCTFitKit
//

#import <Foundation/Foundation.h>
#import <HealthKit/HealthKit.h>
#import "RCTBridgeModule.h"
#import "RCTUtils.h"
#import "RCTLog.h"

@interface RCTFitKit : NSObject <RCTBridgeModule>

@property (nonatomic) HKHealthStore *healthStore;

- (void)isHealthKitAvailable:(RCTResponseSenderBlock)callback;
- (void)initializeHealthKit:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)getModuleInfo:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;

@end
