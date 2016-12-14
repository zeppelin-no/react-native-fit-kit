//
//  RCTFitKit+Methods_Characteristic.h
//  RCTFitKit
//

#import "RCTFitKit.h"

@interface RCTFitKit (Methods_Characteristic)

- (void)characteristic_getBiologicalSex:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)characteristic_getDateOfBirth:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

@end
