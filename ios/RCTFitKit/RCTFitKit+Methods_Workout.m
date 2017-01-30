//
//  RCTFitKit+Methods_Workout.m
//  RCTFitKit
//

#import "RCTFitKit+Methods_Workout.h"
#import "RCTFitKit+Queries.h"
#import "RCTFitKit+Utils.h"
#import "OMHSerializer.h"

@implementation RCTFitKit (Methods_Workout)

- (NSMutableDictionary*)convertHKActivityType:(NSMutableDictionary*)jsonResponse {
    @try {
        NSLog(@"jsonResponse %@", jsonResponse);

        // Remove HKWorkoutActivityType
        NSString* standarizedActivityType =
            [jsonResponse[@"body"][@"activity_name"] stringByReplacingOccurrencesOfString:@"HKWorkoutActivityType" withString:@""];

        // Split and undescore on capital letter
        standarizedActivityType = [standarizedActivityType stringByReplacingOccurrencesOfString:@"([a-z])([A-Z])"
                                                                                     withString:@"$1_$2"
                                                                                        options:NSRegularExpressionSearch
                                                                                          range:NSMakeRange(0, standarizedActivityType.length)];
        // Uppercase string
        standarizedActivityType = [standarizedActivityType uppercaseString];

        jsonResponse[@"body"][@"activity_name"] = standarizedActivityType;
    } @catch (NSException* exception) {
        NSLog(@"Error converting HKActivity to standarized activity %@", exception);
    }

    return jsonResponse;
}

- (NSMutableDictionary*)convertSchemaID:(NSMutableDictionary*)jsonResponse {
    @try {
        // Remove HKWorkoutActivityType
        NSDictionary* standarizedSchemaID = @{
            @"namespace" : @"omh",
            @"name" : @"physical-activity",
            @"version" : @"1.2",
        };

        jsonResponse[@"header"][@"schema_id"] = standarizedSchemaID;
    } @catch (NSException* exception) {
        NSLog(@"Error converting schema_id to standard schema_id %@", exception);
    }

    return jsonResponse;
}

- (NSMutableDictionary*)addSource:(NSMutableDictionary*)jsonResponse sourceRev:(HKSourceRevision*)sourceRev {
    @try {
        NSDictionary* source = @{
                                 @"source": sourceRev.source.name,
                                 @"version": sourceRev.version,
                                 };
        
        NSLog(@"source: %@", source);
        
        jsonResponse[@"header"][@"source"] = source;
        
    } @catch (NSException *exception) {
        NSLog(@"Error adding source %@", exception);
    }
    
    return jsonResponse;
}

- (void)workout_getActivities:(NSDictionary*)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSLog(@"workout_getActivities");
    
    HKUnit* distanceUnit = [RCTFitKit hkUnitFromOptions:input];
    if (distanceUnit == nil) {
        distanceUnit = [HKUnit meterUnit];
    }

    HKUnit* enegryUnit = [RCTFitKit hkUnitFromOptions:input];
    if (enegryUnit == nil) {
        enegryUnit = [HKUnit calorieUnit];
    }

    NSDate* startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:[NSDate distantPast]];
    NSDate* endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];

    NSLog(@"startdate: %@", startDate);
    
    NSPredicate* predicate = [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:false];

    NSSortDescriptor* sortDescriptor = [[NSSortDescriptor alloc] initWithKey:HKSampleSortIdentifierStartDate ascending:false];

    HKSampleQuery* sampleQuery = [[HKSampleQuery alloc]
        initWithSampleType:[HKWorkoutType workoutType]
                 predicate:predicate
                     limit:HKObjectQueryNoLimit
           sortDescriptors:@[ sortDescriptor ]
            resultsHandler:^(HKSampleQuery* query, NSArray* results, NSError* error) {

              if (!error && results) {
                  NSMutableArray* data = [NSMutableArray arrayWithCapacity:1];

                  dispatch_async(dispatch_get_main_queue(), ^{

                    for (HKQuantitySample* sample in results) {
                        OMHSerializer* serializer = [OMHSerializer new];
                        NSString* jsonString = [serializer jsonForSample:sample error:nil];

                        NSData* jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
                        NSMutableDictionary* jsonResponse = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:nil];

                        jsonResponse = [self convertHKActivityType:jsonResponse];
                        jsonResponse = [self convertSchemaID:jsonResponse];
                        jsonResponse = [self addSource:jsonResponse sourceRev:sample.sourceRevision];
                        
                        [data addObject:jsonResponse];
                    }

                    resolve(data);
                  });
              } else {
                  NSLog(@"Error retrieving workouts %@", error);
                  reject(@"Error retrieving workouts %@", nil, error);
              }
            }];

    // Execute the query
    [self.healthStore executeQuery:sampleQuery];
}

@end
