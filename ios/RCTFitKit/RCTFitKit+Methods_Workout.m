//
//  RCTFitKit+Methods_Workout.m
//  RCTFitKit
//

#import "RCTFitKit+Methods_Workout.h"
#import "RCTFitKit+Queries.h"
#import "RCTFitKit+Utils.h"

@implementation RCTFitKit (Methods_Workout)


- (void)workout_getLatestWorkout:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    // Query to get the user's latest weight, if it exists.
    HKQuantityType *weightType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBodyMass];

    HKUnit *unit = [RCTFitKit hkUnitFromOptions:input];
    if(unit == nil){
        unit = [HKUnit poundUnit];
    }

    [self fetchMostRecentQuantitySampleOfType:weightType
                                    predicate:nil
                                   completion:^(HKQuantity *mostRecentQuantity, NSDate *startDate, NSDate *endDate, NSError *error) {
                                       if (!mostRecentQuantity) {
                                           NSLog(@"error getting latest weight: %@", error);
                                           callback(@[RCTMakeError(@"error getting latest weight", error, nil)]);
                                       }
                                       else {
                                           // Determine the weight in the required unit.
                                           double usersWeight = [mostRecentQuantity doubleValueForUnit:unit];

                                           NSDictionary *response = @{
                                                                      @"value" : @(usersWeight),
                                                                      @"startDate" : [RCTFitKit buildISO8601StringFromDate:startDate],
                                                                      @"endDate" : [RCTFitKit buildISO8601StringFromDate:endDate],
                                                                      };

                                           callback(@[[NSNull null], response]);
                                       }
                                   }];
}

@end
