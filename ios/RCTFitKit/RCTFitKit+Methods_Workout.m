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

-(void)workout_retrieveWorkouts:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    
    HKUnit *distanceUnit = [RCTFitKit hkUnitFromOptions:input];
    if(distanceUnit == nil){
        distanceUnit = [HKUnit meterUnit];
    }
    
    HKUnit *enegryUnit = [RCTFitKit hkUnitFromOptions:input];
    if(enegryUnit == nil){
        enegryUnit = [HKUnit calorieUnit];
    }
    
    
    NSDate *start = [NSDate distantPast];
    NSDate *end = [NSDate distantFuture];
    // 1. Predicate to read only running workouts
    
    NSPredicate *predicate = [HKQuery predicateForSamplesWithStartDate:start endDate:end options:false];
    
    // 2. Order the workouts by date
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc]initWithKey:HKSampleSortIdentifierStartDate ascending:false];
    
    // 3. Create the query
    HKSampleQuery *sampleQuery = [[HKSampleQuery alloc] initWithSampleType:[HKWorkoutType workoutType]
                                                                 predicate:predicate
                                                                     limit:HKObjectQueryNoLimit
                                                           sortDescriptors:@[sortDescriptor]
                                                            resultsHandler:^(HKSampleQuery *query, NSArray *results, NSError *error)
                                  {
                                      
                                      if (!error && results) {
                                          NSMutableArray *data = [NSMutableArray arrayWithCapacity:1];
                                          
                                          dispatch_async(dispatch_get_main_queue(), ^{
                                              
                                              for (HKQuantitySample *sample in results) {
                                                  HKWorkout *workout = (HKWorkout *)sample;
                                                  
                                                  double distance = [workout.totalDistance doubleValueForUnit:distanceUnit];
                                                  double energy = [workout.totalEnergyBurned doubleValueForUnit:enegryUnit];
                                                                                                    
                                                  NSString *startDateString = [RCTFitKit buildISO8601StringFromDate:sample.startDate];
                                                  NSString *endDateString = [RCTFitKit buildISO8601StringFromDate:sample.endDate];
                                                  
                                                  NSDictionary *elem = @{
                                                                         @"distance" : @(distance),
                                                                         @"energy" : @(energy),
                                                                         @"duration" : @(workout.duration),
                                                                         @"type" : @(workout.workoutActivityType),
                                                                         @"startDate" : startDateString,
                                                                         @"endDate" : endDateString,
                                                                         };
                                                  
                                                  [data addObject:elem];
                                              }
                                              
                                              callback(@[[NSNull null], data]);
                                          });
                                      } else {
                                          NSLog(@"Error retrieving workouts %@",error);
                                      }
                                  }];
    
    // Execute the query
    [self.healthStore executeQuery:sampleQuery];
}
    
@end
