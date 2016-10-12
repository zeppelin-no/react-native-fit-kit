#import "RCTFitKit+Methods_Results.h"
#import "RCTFitKit+Queries.h"
#import "RCTFitKit+Utils.h"

@implementation RCTFitKit (Methods_Results)

- (void)results_getBloodGlucoseSamples:(NSDictionary *)input callback:(RCTResponseSenderBlock)callback
{
    HKQuantityType *bloodGlucoseType = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierBloodGlucose];

    HKUnit *mmoLPerL = [[HKUnit moleUnitWithMetricPrefix:HKMetricPrefixMilli molarMass:HKUnitMolarMassBloodGlucose] unitDividedByUnit:[HKUnit literUnit]];

    HKUnit *unit = [RCTFitKit hkUnitFromOptions:input key:@"unit" withDefault:mmoLPerL];
    NSUInteger limit = [RCTFitKit uintFromOptions:input key:@"limit" withDefault:HKObjectQueryNoLimit];
    BOOL ascending = [RCTFitKit boolFromOptions:input key:@"ascending" withDefault:false];
    NSDate *startDate = [RCTFitKit dateFromOptions:input key:@"startDate" withDefault:nil];
    NSDate *endDate = [RCTFitKit dateFromOptions:input key:@"endDate" withDefault:[NSDate date]];
    if(startDate == nil){
        callback(@[RCTMakeError(@"startDate is required in options", nil, nil)]);
        return;
    }
    NSPredicate * predicate = [RCTFitKit predicateForSamplesBetweenDates:startDate endDate:endDate];

    [self fetchQuantitySamplesOfType:bloodGlucoseType
                                unit:unit
                           predicate:predicate
                           ascending:ascending
                               limit:limit
                          completion:^(NSArray *results, NSError *error) {
        if(results){
            callback(@[[NSNull null], results]);
            return;
        } else {
            NSLog(@"error getting blood glucose samples: %@", error);
            callback(@[RCTMakeError(@"error getting blood glucose samples", nil, nil)]);
            return;
        }
    }];
}


@end
