//
//  RCTFitKit+Methods_Characteristic.h
//  RCTFitKit
//

#import "RCTFitKit.h"

@interface RCTFitKit (Methods_Characteristic)

- (void)characteristic_getBiologicalSex:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;
- (void)characteristic_getDateOfBirth:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;

@end
