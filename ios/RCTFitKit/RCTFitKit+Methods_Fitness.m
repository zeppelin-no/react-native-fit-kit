//
//  RCTFitKit+Methods_Fitness.m
//  RCTFitKit
//

#import "RCTFitKit+Methods_Fitness.h"
#import "RCTFitKit+Queries.h"
#import "RCTFitKit+Utils.h"

#import "RCTBridge.h"
#import "RCTEventDispatcher.h"

@implementation RCTFitKit (Methods_Fitness)


//- (void)fitness_getStepCountForToday:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
//{
//    HKQuantityType *stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
//    HKUnit *stepsUnit = [HKUnit countUnit];
//
//    [self fetchSumOfSamplesTodayForType:stepCountType
//                                   unit:stepsUnit
//                             completion:^(double totalSteps, NSError *error) {
//        if (!totalSteps) {
//            NSLog(@"Either an error occured fetching the user's step count information or none has been stored yet. In your app, try to handle this gracefully.");
//            callback(@[RCTMakeError(@"Either an error occured fetching the user's step count information or none has been stored yet. In your app, try to handle this gracefully.", nil, nil)]);
//            return;
//        }
//
//        callback(@[[NSNull null], @(totalSteps)]);
//    }];
//}


- (void)fitness_getStepCountOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    NSDate *date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    if(date == nil) {
        callback(@[RCTMakeError(@"could not parse date from options.date", nil, nil)]);
        return;
    }

    HKQuantityType *stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    HKUnit *stepsUnit = [HKUnit countUnit];

    [self fetchSumOfSamplesOnDayForType:stepCountType
                                   unit:stepsUnit
                                    day:date
                             completion:^(double value, NSDate *startDate, NSDate *endDate, NSError *error) {
        if (!value) {
            NSLog(@"could not fetch step count for day: %@", error);
            callback(@[RCTMakeError(@"could not fetch step count for day", error, nil)]);
            return;
        }

         NSDictionary *response = @{
                 @"value" : @(value),
                 @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                 @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
         };

        callback(@[[NSNull null], response]);
    }];
}



//
//- (void)fitness_getDailyStepCounts:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
//{
//    NSDate *startDate = [RCTFitKit startDateFromOptions:input];
//    NSDate *endDate = [RCTFitKit endDateFromOptionsDefaultNow:input];
//
//    if(startDate == nil) {
//        callback(@[RCTMakeError(@"could not parse required startDate from options.startDate", nil, nil)]);
//        return;
//    }
//
//    HKQuantityType *stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
//    HKUnit *stepsUnit = [HKUnit countUnit];
//
//    [self fetchCumulativeSumStatisticsCollection:stepCountType
//                                            unit:stepsUnit
//                                       startDate:startDate
//                                         endDate:endDate
//                                      completion:^(NSArray *arr, NSError *err){
//        if (err != nil) {
//            NSLog(@"error with fetchCumulativeSumStatisticsCollection: %@", err);
//            callback(@[RCTMakeError(@"error with fetchCumulativeSumStatisticsCollection", err, nil)]);
//            return;
//        }
//        callback(@[[NSNull null], arr]);
//    }];
//}




- (void)fitness_getDailyStepSamples:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    HKUnit *unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit countUnit]];
    NSUInteger limit = [RCTFitKit uintFromOptions:input key:@"limit" withDefault:HKObjectQueryNoLimit];
    BOOL ascending = [RCTFitKit boolFromOptions:input key:@"ascending" withDefault:false];
    NSDate *startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:nil];
    NSDate *endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];
    if(startDate == nil){
        callback(@[RCTMakeError(@"startDate is required in options", nil, nil)]);
        return;
    }

    HKQuantityType *stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];

    [self fetchCumulativeSumStatisticsCollection:stepCountType
                                            unit:unit
                                       startDate:startDate
                                         endDate:endDate
                                       ascending:ascending
                                           limit:limit
                                      completion:^(NSArray *arr, NSError *err){
        if (err != nil) {
            NSLog(@"error with fetchCumulativeSumStatisticsCollection: %@", err);
            callback(@[RCTMakeError(@"error with fetchCumulativeSumStatisticsCollection", err, nil)]);
            return;
        }
        callback(@[[NSNull null], arr]);
    }];
}



- (void)fitness_saveSteps:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    double value = [RCTFitKit doubleFromOptions:input key:@"value" withDefault:(double)0];
    NSDate *startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:nil];
    NSDate *endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];

    if(startDate == nil || endDate == nil){
        callback(@[RCTMakeError(@"startDate and endDate are required in options", nil, nil)]);
        return;
    }

    HKUnit *unit = [HKUnit countUnit];
    HKQuantity *quantity = [HKQuantity quantityWithUnit:unit doubleValue:value];
    HKQuantityType *type = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    HKQuantitySample *sample = [HKQuantitySample quantitySampleWithType:type quantity:quantity startDate:startDate endDate:endDate];

    [self.healthStore saveObject:sample withCompletion:^(BOOL success, NSError *error) {
        if (!success) {
            NSLog(@"An error occured saving the step count sample %@. The error was: %@.", sample, error);
            callback(@[RCTMakeError(@"An error occured saving the step count sample", error, nil)]);
            return;
        }
        callback(@[[NSNull null], @(value)]);
    }];
}



- (void)fitness_initializeStepEventObserver:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    HKSampleType *sampleType =
    [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];

    HKObserverQuery *query =
    [[HKObserverQuery alloc]
     initWithSampleType:sampleType
     predicate:nil
     updateHandler:^(HKObserverQuery *query,
                     HKObserverQueryCompletionHandler completionHandler,
                     NSError *error) {

         if (error) {
             // Perform Proper Error Handling Here...
             NSLog(@"*** An error occured while setting up the stepCount observer. %@ ***", error.localizedDescription);
             callback(@[RCTMakeError(@"An error occured while setting up the stepCount observer", error, nil)]);
             return;
         }

          [self.bridge.eventDispatcher sendAppEventWithName:@"change:steps"
                                                       body:@{@"name": @"change:steps"}];

         // If you have subscribed for background updates you must call the completion handler here.
         // completionHandler();

     }];

    [self.healthStore executeQuery:query];
}



- (void)fitness_getDistanceWalkingRunningOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    HKUnit *unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit meterUnit]];
    NSDate *date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    HKQuantityType *quantityType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierDistanceWalkingRunning];

    [self fetchSumOfSamplesOnDayForType:quantityType unit:unit day:date completion:^(double distance, NSDate *startDate, NSDate *endDate, NSError *error) {
        if (!distance) {
            NSLog(@"ERROR getting DistanceWalkingRunning: %@", error);
            callback(@[RCTMakeError(@"ERROR getting DistanceWalkingRunning", error, nil)]);
            return;
        }

        NSDictionary *response = @{
                @"value" : @(distance),
                @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
        };


        callback(@[[NSNull null], response]);
    }];
}


- (void)fitness_getDistanceCyclingOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    HKUnit *unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:[HKUnit meterUnit]];
    NSDate *date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    HKQuantityType *quantityType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierDistanceCycling];

    [self fetchSumOfSamplesOnDayForType:quantityType unit:unit day:date completion:^(double distance, NSDate *startDate, NSDate *endDate, NSError *error) {
        if (!distance) {
            NSLog(@"ERROR getting DistanceCycling: %@", error);
            callback(@[RCTMakeError(@"ERROR getting DistanceCycling", error, nil)]);
            return;
        }

        NSDictionary *response = @{
                @"value" : @(distance),
                @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
        };

        callback(@[[NSNull null], response]);
    }];
}


- (void)fitness_getFlightsClimbedOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    HKUnit *unit = [HKUnit countUnit];
    NSDate *date = [RCTFitKit dateFromOptions:input key:@"date" withDefault:[NSDate date]];

    HKQuantityType *quantityType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierFlightsClimbed];

    [self fetchSumOfSamplesOnDayForType:quantityType unit:unit day:date completion:^(double count, NSDate *startDate, NSDate *endDate, NSError *error) {
        if (!count) {
            NSLog(@"ERROR getting FlightsClimbed: %@", error);
            callback(@[RCTMakeError(@"ERROR getting FlightsClimbed", error, nil), @(count)]);
            return;
        }

        NSDictionary *response = @{
                @"value" : @(count),
                @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
        };

        callback(@[[NSNull null], response]);
    }];
}





@end