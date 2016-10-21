//
//  RCTFitKit+Methods_Fitness.h
//  RCTFitKit
//

#import "RCTFitKit.h"

@interface RCTFitKit (Methods_Fitness)

//- (void)fitness_getStepCountForToday:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;
- (void)fitness_getStepCountOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;

//- (void)fitness_getDailyStepCounts:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;
- (void)fitness_getDailyStepSamples:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;

- (void)fitness_saveSteps:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;

- (void)fitness_initializeStepEventObserver:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;

- (void)fitness_getDistanceWalkingRunningOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;
- (void)fitness_getDistanceCyclingOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;
- (void)fitness_getFlightsClimbedOnDay:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback;


@end