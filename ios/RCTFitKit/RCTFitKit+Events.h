//
//  RCTFitKit+Events.h
//  RCTFitKit
//

#import "RCTFitKit.h"

@interface RCTFitKit (Events)

- (void)events_initStepCountObserver:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

@end
