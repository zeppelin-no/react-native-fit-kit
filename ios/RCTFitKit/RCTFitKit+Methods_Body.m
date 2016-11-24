

//
//  RCTFitKit+Methods_Body.m
//  RCTFitKit
//

#import "RCTFitKit+Methods_Body.h"
#import "RCTFitKit+Queries.h"
#import "RCTFitKit+Utils.h"

@implementation RCTFitKit (Methods_Body)

- (NSDictionary*)body_getLatestBodyStatsFromHK:(HKQuantityType*)quantityType unitType:(HKUnit*)unit {

    NSMutableDictionary* data = [NSMutableDictionary dictionaryWithCapacity:1];

    dispatch_group_t group = dispatch_group_create();
    dispatch_group_enter(group);

    dispatch_async(dispatch_get_main_queue(), ^{

      [self fetchMostRecentQuantitySampleOfType:quantityType
                                      predicate:nil
                                     completion:^(HKQuantity* mostRecentQuantity, NSDate* startDate, NSDate* endDate, NSError* error) {
                                       if (!mostRecentQuantity) {
                                           NSLog(@"error getting latest: %@", error);
                                           // reject(@"no_weight", @"error getting latest", error);
                                       } else {
                                           // Determine the weight in the required unit.
                                           double quantity = [mostRecentQuantity doubleValueForUnit:unit];

                                           if (unit == [HKUnit gramUnit]) {
                                               quantity = quantity / 1000;
                                           }
                                           
                                           [data addEntriesFromDictionary:@{
                                               @"value" : @(quantity),
                                               @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                               @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                                           }];

                                           // resolve(@[ [NSNull null], response ]);
                                       }

                                       dispatch_group_leave(group);
                                     }];
    });
    dispatch_group_wait(group, DISPATCH_TIME_FOREVER);

    return data;
}

- (void)body_getLatestWeight:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    // Query to get the user's latest weight, if it exists.
    HKQuantityType* weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];

    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input];
    if (unit == nil) {
        unit = [HKUnit gramUnit];
    }

    NSDictionary* weight = [self body_getLatestBodyStatsFromHK:weightType unitType:unit];
    resolve(weight);
}

- (void)body_getLatestHeight:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    HKQuantityType* heightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];

    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input];
    if (unit == nil) {
        unit = [HKUnit meterUnit];
    }
    
    NSDictionary* height = [self body_getLatestBodyStatsFromHK:heightType unitType:unit];
    resolve(height);
}

- (void)body_getLatestBodyStats:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    HKQuantityType* weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];
    HKQuantityType* heightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    
    HKUnit* weightUnit = [RCTFitKit hkUnitFromOptions:[input objectForKey:@"weightUnit"]];
    if (weightUnit == nil) {
        weightUnit = [HKUnit gramUnit];
    }
    HKUnit* lengthUnit = [RCTFitKit hkUnitFromOptions:[input objectForKey:@"heightUnit"]];
    if (lengthUnit == nil) {
        lengthUnit = [HKUnit meterUnit];
    }
    
    NSDictionary* weight = [self body_getLatestBodyStatsFromHK:weightType unitType:weightUnit];
    NSDictionary* height = [self body_getLatestBodyStatsFromHK:heightType unitType:lengthUnit];
    
    NSDictionary* latestBodyStats = @{
                                      @"weight": weight,
                                      @"height": height,
                                      };
    
    resolve(latestBodyStats);
}

- (void)body_saveHeight:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    double height = [RCTFitKit doubleValueFromOptions:input];
    NSDate* sampleDate = [RCTFitKit dateFromOptionsDefaultNow:input];
    
    HKUnit* heightUnit = [RCTFitKit hkUnitFromOptions:input];
    if (heightUnit == nil) {
        heightUnit = [HKUnit meterUnit];
    }
    
    HKQuantity* heightQuantity = [HKQuantity quantityWithUnit:heightUnit doubleValue:height];
    HKQuantityType* heightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    HKQuantitySample* heightSample = [HKQuantitySample quantitySampleWithType:heightType quantity:heightQuantity startDate:sampleDate endDate:sampleDate];
    
    [self.healthStore saveObject:heightSample
                  withCompletion:^(BOOL success, NSError* error) {
                      if (!success) {
                          NSLog(@"error saving height sample: %@", error);
                          reject(@"save_height_error", @"error saving height sample", nil);
                          return;
                      }
                      resolve(@[ [NSNull null], @(height) ]);
                  }];
}

- (void)body_saveWeight:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    double weight = [RCTFitKit doubleValueFromOptions:input] * 1000;
    NSDate* sampleDate = [RCTFitKit dateFromOptionsDefaultNow:input];
    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit gramUnit]];
    
    HKQuantity* weightQuantity = [HKQuantity quantityWithUnit:unit doubleValue:weight];
    HKQuantityType* weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];
    HKQuantitySample* weightSample = [HKQuantitySample quantitySampleWithType:weightType quantity:weightQuantity startDate:sampleDate endDate:sampleDate];
    
    [self.healthStore saveObject:weightSample
                  withCompletion:^(BOOL success, NSError* error) {
                      if (!success) {
                          NSLog(@"error saving the weight sample: %@", error);
                          reject(@"save_weight_error", @"error saving weight sample", nil);
                          return;
                      }
                      resolve(@[ [NSNull null], @(weight) ]);
                  }];
}

// ====================================================================================================================



- (void)body_getWeightSamples:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    HKQuantityType* weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];

    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit poundUnit]];
    NSUInteger limit = [RCTFitKit uintFromOptions:input key:@"limit" withDefault:HKObjectQueryNoLimit];
    BOOL ascending = [RCTFitKit boolFromOptions:input key:@"ascending" withDefault:false];
    NSDate* startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:nil];
    NSDate* endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];
    if (startDate == nil) {
        callback(@[ RCTMakeError(@"startDate is required in options", nil, nil) ]);
        return;
    }
    NSPredicate* predicate = [RCTFitKit predicateForSamplesBetweenDates:startDate endDate:endDate];

    [self fetchQuantitySamplesOfType:weightType
                                unit:unit
                           predicate:predicate
                           ascending:ascending
                               limit:limit
                          completion:^(NSArray* results, NSError* error) {
                            if (results) {
                                callback(@[ [NSNull null], results ]);
                                return;
                            } else {
                                NSLog(@"error getting weight samples: %@", error);
                                callback(@[ RCTMakeError(@"error getting weight samples", nil, nil) ]);
                                return;
                            }
                          }];
}



- (void)body_getLatestBodyMassIndex:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    // Query to get the user's latest BMI, if it exists.
    HKQuantityType* bmiType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMassIndex];

    [self fetchMostRecentQuantitySampleOfType:bmiType
                                    predicate:nil
                                   completion:^(HKQuantity* mostRecentQuantity, NSDate* startDate, NSDate* endDate, NSError* error) {
                                     if (!mostRecentQuantity) {
                                         NSLog(@"error getting latest BMI: %@", error);
                                         callback(@[ RCTMakeError(@"error getting latest BMI", error, nil) ]);
                                     } else {
                                         // Determine the bmi in the required unit.
                                         HKUnit* countUnit = [HKUnit countUnit];
                                         double bmi = [mostRecentQuantity doubleValueForUnit:countUnit];

                                         NSDictionary* response = @{
                                             @"value" : @(bmi),
                                             @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                             @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                                         };

                                         callback(@[ [NSNull null], response ]);
                                     }
                                   }];
}

- (void)body_saveBodyMassIndex:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    double bmi = [RCTFitKit doubleValueFromOptions:input];
    NSDate* sampleDate = [RCTFitKit dateFromOptionsDefaultNow:input];
    HKUnit* unit = [HKUnit countUnit];

    HKQuantity* bmiQuantity = [HKQuantity quantityWithUnit:unit doubleValue:bmi];
    HKQuantityType* bmiType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMassIndex];
    HKQuantitySample* bmiSample = [HKQuantitySample quantitySampleWithType:bmiType quantity:bmiQuantity startDate:sampleDate endDate:sampleDate];

    [self.healthStore saveObject:bmiSample
                  withCompletion:^(BOOL success, NSError* error) {
                    if (!success) {
                        NSLog(@"error saving BMI sample: %@.", error);
                        callback(@[ RCTMakeError(@"error saving BMI sample", error, nil) ]);
                        return;
                    }
                    callback(@[ [NSNull null], @(bmi) ]);
                  }];
}

- (void)body_getHeightSamples:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    HKQuantityType* heightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];

    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit inchUnit]];
    NSUInteger limit = [RCTFitKit uintFromOptions:input key:@"limit" withDefault:HKObjectQueryNoLimit];
    BOOL ascending = [RCTFitKit boolFromOptions:input key:@"ascending" withDefault:false];
    NSDate* startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:nil];
    NSDate* endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];
    if (startDate == nil) {
        callback(@[ RCTMakeError(@"startDate is required in options", nil, nil) ]);
        return;
    }
    NSPredicate* predicate = [RCTFitKit predicateForSamplesBetweenDates:startDate endDate:endDate];

    [self fetchQuantitySamplesOfType:heightType
                                unit:unit
                           predicate:predicate
                           ascending:ascending
                               limit:limit
                          completion:^(NSArray* results, NSError* error) {
                            if (results) {
                                callback(@[ [NSNull null], results ]);
                                return;
                            } else {
                                NSLog(@"error getting height samples: %@", error);
                                callback(@[ RCTMakeError(@"error getting height samples", error, nil) ]);
                                return;
                            }
                          }];
}


- (void)body_getLatestBodyFatPercentage:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    HKQuantityType* bodyFatPercentType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyFatPercentage];

    [self fetchMostRecentQuantitySampleOfType:bodyFatPercentType
                                    predicate:nil
                                   completion:^(HKQuantity* mostRecentQuantity, NSDate* startDate, NSDate* endDate, NSError* error) {
                                     if (!mostRecentQuantity) {
                                         NSLog(@"error getting latest body fat "
                                               @"percentage: %@",
                                               error);
                                         callback(@[ RCTMakeError(@"error getting latest "
                                                                  @"body fat percentage",
                                                                  error, nil) ]);
                                     } else {
                                         // Determine the weight in the required unit.
                                         HKUnit* percentUnit = [HKUnit percentUnit];
                                         double percentage = [mostRecentQuantity doubleValueForUnit:percentUnit];

                                         percentage = percentage * 100;

                                         NSDictionary* response = @{
                                             @"value" : @(percentage),
                                             @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                             @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                                         };

                                         callback(@[ [NSNull null], response ]);
                                     }
                                   }];
}

- (void)body_getLatestLeanBodyMass:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    HKQuantityType* leanBodyMassType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierLeanBodyMass];

    [self fetchMostRecentQuantitySampleOfType:leanBodyMassType
                                    predicate:nil
                                   completion:^(HKQuantity* mostRecentQuantity, NSDate* startDate, NSDate* endDate, NSError* error) {
                                     if (!mostRecentQuantity) {
                                         NSLog(@"error getting latest lean body "
                                               @"mass: %@",
                                               error);
                                         callback(@[ RCTMakeError(@"error getting latest lean body mass", error, nil) ]);
                                     } else {
                                         HKUnit* weightUnit = [HKUnit poundUnit];
                                         double leanBodyMass = [mostRecentQuantity doubleValueForUnit:weightUnit];

                                         NSDictionary* response = @{
                                             @"value" : @(leanBodyMass),
                                             @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                             @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                                         };

                                         callback(@[ [NSNull null], response ]);
                                     }
                                   }];
}

- (void)body_fetchQuantitySamplesOfType:(HKQuantityType*)quantityType
                                   unit:(HKUnit*)unit
                              predicate:(NSPredicate*)predicate
                              ascending:(BOOL)asc
                                  limit:(NSUInteger)lim
                             completion:(void (^)(NSArray*, NSError*))completion {

    NSSortDescriptor* timeSortDescriptor = [[NSSortDescriptor alloc] initWithKey:HKSampleSortIdentifierEndDate ascending:asc];

    // declare the block
    void (^handlerBlock)(HKSampleQuery* query, NSArray* results, NSError* error);
    // create and assign the block
    handlerBlock = ^(HKSampleQuery* query, NSArray* results, NSError* error) {
      if (!results) {
          if (completion) {
              completion(nil, error);
          }
          return;
      }

      if (completion) {
          NSMutableArray* data = [NSMutableArray arrayWithCapacity:1];

          dispatch_async(dispatch_get_main_queue(), ^{

            for (HKQuantitySample* sample in results) {
                HKQuantity* quantity = sample.quantity;
                double value = [quantity doubleValueForUnit:unit];

                NSString* startDateString = [RCTFitKit buildISO8601StringFromDate:sample.startDate];

                NSDictionary* d = @{
                    @"HKQuantityTypeIdentifierHeight" : @"height",
                    @"HKQuantityTypeIdentifierBodyMass" : @"bodyMass",
                    @"HKQuantityTypeIdentifierBodyMassIndex" : @"bodyMassIndex"
                };

                NSString* type = d[sample.sampleType.identifier];
                if (!type) {
                    type = @"other";
                }

                if (unit == [HKUnit gramUnit]) {
                    value = value / 1000;
                }

                NSDictionary* elem = @{
                    type : @(value),
                    @"dateTime" : startDateString,
                    //@"id": sample.UUID.UUIDString,
                };

                [data addObject:elem];
            }

            completion(data, error);
          });
      }
    };

    HKSampleQuery* query = [[HKSampleQuery alloc] initWithSampleType:quantityType
                                                           predicate:predicate
                                                               limit:lim
                                                     sortDescriptors:@[ timeSortDescriptor ]
                                                      resultsHandler:handlerBlock];

    [self.healthStore executeQuery:query];
}

- (NSArray*)body_getSamples:(NSDictionary*)input
               quantityType:(HKQuantityType*)quantityType
                   unitType:(HKUnit*)unitType
                  startDate:(NSDate*)startDate
                    endDate:(NSDate*)endDate {
    // HKQuantityType *bmiType = [HKQuantityType
    // quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMassIndex];

    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:unitType];
    NSUInteger limit = [RCTFitKit uintFromOptions:input key:@"limit" withDefault:HKObjectQueryNoLimit];
    BOOL ascending = [RCTFitKit boolFromOptions:input key:@"ascending" withDefault:false];

    NSPredicate* predicate = [RCTFitKit predicateForSamplesBetweenDates:startDate endDate:endDate];

    NSMutableArray* data = [NSMutableArray arrayWithCapacity:1];

    dispatch_group_t group = dispatch_group_create();
    dispatch_group_enter(group);

    dispatch_async(dispatch_get_main_queue(), ^{
      [self body_fetchQuantitySamplesOfType:quantityType
                                       unit:unit
                                  predicate:predicate
                                  ascending:ascending
                                      limit:limit
                                 completion:^(NSArray* results, NSError* error) {
                                   if (results) {
                                       NSLog(@"got wight sample");
                                       [data addObjectsFromArray:results];
                                   } else {
                                       NSLog(@"error getting weight samples: %@", error);
                                   }

                                   dispatch_group_leave(group);
                                 }];
    });

    dispatch_group_wait(group, DISPATCH_TIME_FOREVER);
    return data;
}

- (void)body_getBodyMetrics:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSDate* startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:[NSDate distantPast]];
    NSDate* endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];

    HKUnit* distanceUnit = [RCTFitKit hkUnitFromOptions:input];
    if (distanceUnit == nil) {
        distanceUnit = [HKUnit meterUnit];
    }

    HKUnit* enegryUnit = [RCTFitKit hkUnitFromOptions:input];
    if (enegryUnit == nil) {
        enegryUnit = [HKUnit calorieUnit];
    }

    HKQuantityType* heightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    HKQuantityType* weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];
    HKQuantityType* bmiType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMassIndex];
    HKUnit* lengthUnit = [HKUnit meterUnit];
    HKUnit* weightUnit = [HKUnit gramUnit];
    HKUnit* countUnit = [HKUnit countUnit];

    NSArray* heightSamples = [self body_getSamples:nil quantityType:heightType unitType:lengthUnit startDate:startDate endDate:endDate];
    NSArray* weightSamples = [self body_getSamples:nil quantityType:weightType unitType:weightUnit startDate:startDate endDate:endDate];
    NSArray* BMISamples = [self body_getSamples:nil quantityType:bmiType unitType:countUnit startDate:startDate endDate:endDate];

    NSMutableArray* aggregatedBodySamples = [NSMutableArray arrayWithCapacity:1];

    [aggregatedBodySamples addObjectsFromArray:heightSamples];
    [aggregatedBodySamples addObjectsFromArray:weightSamples];
    [aggregatedBodySamples addObjectsFromArray:BMISamples];

    NSDictionary* resolveValue = @{
        @"bodySamples" : aggregatedBodySamples,
        @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
    };

    if (heightSamples) {
        resolve(resolveValue);
    } else {
        reject(@"Error %@", nil, nil);
    }
}

@end
