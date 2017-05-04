//
//  RCTFitKit+Methods_Fitness.m
//  RCTFitKit
//

#import "RCTFitKit+Methods_Fitness.h"
#import "RCTFitKit+Queries.h"
#import "RCTFitKit+Utils.h"

#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>

@import CoreMotion;

@implementation RCTFitKit (Methods_Fitness)

- (void)fitness_getDailySteps:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSLog(@"fitness_getDailySteps");
    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit countUnit]];
    NSUInteger limit = [RCTFitKit uintFromOptions:input key:@"limit" withDefault:HKObjectQueryNoLimit];
    BOOL ascending = [RCTFitKit boolFromOptions:input key:@"ascending" withDefault:false];
    NSDate* startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:[NSDate distantPast]];
    NSDate* endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];

    HKQuantityType* stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];

    NSLog(@"fitness_getDailySteps - setup done");

    [self fetchCumulativeSumStatisticsCollection:stepCountType
                                            unit:unit
                                       startDate:startDate
                                         endDate:endDate
                                       ascending:ascending
                                           limit:limit
                                      completion:^(NSArray* results, NSError* error) {
                                        if (!error && results) {
                                            NSLog(@"fitness_getDailySteps - got steps");
                                            NSDictionary* response = @{
                                                @"stepSamples" : [[results reverseObjectEnumerator] allObjects],
                                                @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                                            };
                                            resolve(response);
                                        } else {
                                            NSLog(@"error with fetchCumulativeSumStatisticsCollection: %@", error);
                                            reject(@"error with fetchCumulativeSumStatisticsCollection", nil, nil);
                                            return;
                                        }
                                      }];
}

- (void)fitness_getStepsDataPoints:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSLog(@"fitness_getStepsDataPoints");
    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit countUnit]];
    NSUInteger limit = [RCTFitKit uintFromOptions:input key:@"limit" withDefault:HKObjectQueryNoLimit];
    BOOL ascending = [RCTFitKit boolFromOptions:input key:@"ascending" withDefault:false];
    NSDate* startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:[NSDate distantPast]];
    NSDate* endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];

    HKQuantityType* stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    // HKQuantityType* stepCountType = [HKObjectType documentTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    NSPredicate* predicate = [RCTFitKit predicateForSamplesBetweenDates:startDate endDate:endDate];

    [self fetchQuantitySamplesOfType:stepCountType
                                   unit:unit
                              predicate:predicate
                              ascending:ascending
                                  limit:limit
                             completion:^(NSArray* results, NSError* error) {
                               if (!error && results) {
                                  
                                   resolve(results);
                               } else {
                                   NSLog(@"error with fetchCumulativeSumStatisticsCollection: %@", error);
                                   reject(@"error with fetchCumulativeSumStatisticsCollection", nil, nil);
                                   return;
                               }
                             }];
}

- (IBAction)fitness_initStepCountObserver:(NSDictionary*)input
                                 resolver:(RCTPromiseResolveBlock)resolve
                                 rejecter:(RCTPromiseRejectBlock)reject {
    NSLog(@"fitness_initStepCountObserver");

    __block NSInteger previousCount = 0;
    [self.pedometer startPedometerUpdatesFromDate:[NSDate date]
                                      withHandler:^(CMPedometerData* _Nullable pedometerData, NSError* _Nullable error) {

                                        NSNumberFormatter* formatter = [[NSNumberFormatter alloc] init];
                                        formatter.maximumFractionDigits = 2;

                                        // step counting
                                        if ([CMPedometer isStepCountingAvailable]) {
                                            NSInteger numberOfSteps = [pedometerData.numberOfSteps integerValue];
                                            [self.bridge.eventDispatcher sendAppEventWithName:@"FitKitStepEvent"
                                                                                         body:@{
                                                                                             @"steps" : @(numberOfSteps - previousCount)
                                                                                         }];
                                            previousCount = numberOfSteps;
                                            // self.stepsLabel.text = [NSString stringWithFormat:@"Steps walked: %@", [formatter
                                            // stringFromNumber:pedometerData.numberOfSteps]];
                                        }
                                      }];

    HKSampleType* sampleType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];

    HKObserverQuery* query = [[HKObserverQuery alloc]
        initWithSampleType:sampleType
                 predicate:nil
             updateHandler:^(HKObserverQuery* query, HKObserverQueryCompletionHandler completionHandler, NSError* error) {

               NSLog(@"stepevent!!!");

               if (error) {
                   NSLog(@"*** An error occured while setting up the stepCount observer. %@ ***", error.localizedDescription);
                   return;
               }

               completionHandler();
             }];

    [self.healthStore executeQuery:query];
    resolve(@YES);
}

- (void)fitness_removeStepObserver:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSLog(@"fitness_removeStepObserver");

    [self.pedometer stopPedometerUpdates];

    resolve(@YES);
}

- (void)fitness_HKObserver:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSLog(@"fitness_initStepCountObserver");

    HKSampleType* sampleType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];

    HKObserverQuery* query = [[HKObserverQuery alloc]
        initWithSampleType:sampleType
                 predicate:nil
             updateHandler:^(HKObserverQuery* query, HKObserverQueryCompletionHandler completionHandler, NSError* error) {

               NSLog(@"stepevent!!!");

               if (error) {
                   // Perform Proper Error Handling Here...
                   NSLog(@"*** An error occured while setting up the stepCount observer. %@ ***", error.localizedDescription);
                   reject(@"An error occured while setting up the stepCount observer", nil, nil);
                   return;
               }

               [self.bridge.eventDispatcher sendAppEventWithName:@"FitKitStepEvent" body:@{ @"steps" : @1 }];
               // completionHandler();
               // resolve(@"observer added");

               // [self.healthStore executeQuery:query];

               // [self sendEventWithName:@"FitKitStepEvent" body:@"yolo"];

               // RCTEvent fu = [[RCTEvent alloc] init];

               //[self.bridge.eventDispatcher sendEvent:(id<RCTEvent>)]

               // If you have subscribed for background updates you must call the completion handler here.
               // completionHandler();

             }];

    [self.healthStore executeQuery:query];
}

- (void)fitness_getStepCountOnDay:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    NSDate* date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    if (date == nil) {
        callback(@[ RCTMakeError(@"could not parse date from options.date", nil, nil) ]);
        return;
    }

    HKQuantityType* stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    HKUnit* stepsUnit = [HKUnit countUnit];

    [self fetchSumOfSamplesOnDayForType:stepCountType
                                   unit:stepsUnit
                                    day:date
                             completion:^(double value, NSDate* startDate, NSDate* endDate, NSError* error) {
                               if (!value) {
                                   NSLog(@"could not fetch step count for day: %@", error);
                                   callback(@[ RCTMakeError(@"could not fetch step count for day", error, nil) ]);
                                   return;
                               }

                               NSDictionary* response = @{
                                   @"value" : @(value),
                                   @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                   @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                               };

                               callback(@[ [NSNull null], response ]);
                             }];
}

- (void)fitness_saveSteps:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    double value = [RCTFitKit doubleFromOptions:input key:@"value" withDefault:(double)0];
    NSDate* startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:nil];
    NSDate* endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];

    if (startDate == nil || endDate == nil) {
        callback(@[ RCTMakeError(@"startDate and endDate are required in options", nil, nil) ]);
        return;
    }

    HKUnit* unit = [HKUnit countUnit];
    HKQuantity* quantity = [HKQuantity quantityWithUnit:unit doubleValue:value];
    HKQuantityType* type = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    HKQuantitySample* sample = [HKQuantitySample quantitySampleWithType:type quantity:quantity startDate:startDate endDate:endDate];

    [self.healthStore saveObject:sample
                  withCompletion:^(BOOL success, NSError* error) {
                    if (!success) {
                        NSLog(@"An error occured saving the step count sample %@. The error was: %@.", sample, error);
                        callback(@[ RCTMakeError(@"An error occured saving the step count sample", error, nil) ]);
                        return;
                    }
                    callback(@[ [NSNull null], @(value) ]);
                  }];
}

- (void)fitness_getDistanceWalkingRunningOnDay:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit meterUnit]];
    NSDate* date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    HKQuantityType* quantityType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierDistanceWalkingRunning];

    [self fetchSumOfSamplesOnDayForType:quantityType
                                   unit:unit
                                    day:date
                             completion:^(double distance, NSDate* startDate, NSDate* endDate, NSError* error) {
                               if (!distance) {
                                   NSLog(@"ERROR getting DistanceWalkingRunning: %@", error);
                                   callback(@[ RCTMakeError(@"ERROR getting DistanceWalkingRunning", error, nil) ]);
                                   return;
                               }

                               NSDictionary* response = @{
                                   @"value" : @(distance),
                                   @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                   @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                               };

                               callback(@[ [NSNull null], response ]);
                             }];
}

- (void)fitness_getDistanceCyclingOnDay:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    HKUnit* unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit meterUnit]];
    NSDate* date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    HKQuantityType* quantityType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierDistanceCycling];

    [self fetchSumOfSamplesOnDayForType:quantityType
                                   unit:unit
                                    day:date
                             completion:^(double distance, NSDate* startDate, NSDate* endDate, NSError* error) {
                               if (!distance) {
                                   NSLog(@"ERROR getting DistanceCycling: %@", error);
                                   callback(@[ RCTMakeError(@"ERROR getting DistanceCycling", error, nil) ]);
                                   return;
                               }

                               NSDictionary* response = @{
                                   @"value" : @(distance),
                                   @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                   @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                               };

                               callback(@[ [NSNull null], response ]);
                             }];
}

- (void)fitness_getFlightsClimbedOnDay:(NSDictionary*)input callback:(RCTResponseSenderBlock)callback {
    HKUnit* unit = [HKUnit countUnit];
    NSDate* date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    HKQuantityType* quantityType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierFlightsClimbed];

    [self fetchSumOfSamplesOnDayForType:quantityType
                                   unit:unit
                                    day:date
                             completion:^(double count, NSDate* startDate, NSDate* endDate, NSError* error) {
                               if (!count) {
                                   NSLog(@"ERROR getting FlightsClimbed: %@", error);
                                   callback(@[ RCTMakeError(@"ERROR getting FlightsClimbed", error, nil), @(count) ]);
                                   return;
                               }

                               NSDictionary* response = @{
                                   @"value" : @(count),
                                   @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                   @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                               };

                               callback(@[ [NSNull null], response ]);
                             }];
}

@end
