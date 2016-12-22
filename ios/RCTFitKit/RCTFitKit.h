//
//  RCTFitKit.h
//  RCTFitKit
//

#import <Foundation/Foundation.h>
#import <HealthKit/HealthKit.h>
#import "RCTBridgeModule.h"
#import "RCTUtils.h"
#import "RCTLog.h"
#import <RCTEventEmitter.h>
@import CoreMotion;

@interface RCTFitKit : RCTEventEmitter <RCTBridgeModule>

@property (nonatomic) HKHealthStore *healthStore;
@property (nonatomic) CMPedometer *pedometer;

- (void)isHealthKitAvailable:(RCTResponseSenderBlock)callback;
- (void)initializeHealthKit:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)getModuleInfo:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;

@end
