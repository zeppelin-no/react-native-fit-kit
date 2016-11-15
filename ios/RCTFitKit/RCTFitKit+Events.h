//
//  RCTFitKit+Events.h
//  RCTFitKit
//

#import "RCTFitKit.h"
#import <HealthKit/HealthKit.h>

@interface RCTFitKit (Events)

//- (void)fitness_initializeStepEventObserver:(NSDictionary *)input healthStore:(HKHealthStore *)healthStore resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)fitness_initializeStepEventObserver:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

@end
