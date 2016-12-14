//
//  RCTFitKit+Methods_Characteristic.m
//  RCTFitKit
//

#import "RCTFitKit+Methods_Characteristic.h"
#import "RCTFitKit+Utils.h"

@implementation RCTFitKit (Methods_Characteristic)

- (void)characteristic_getBiologicalSex:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
    NSError *error;
    HKBiologicalSexObject *bioSex = [self.healthStore biologicalSexWithError:&error];
    NSString *value;

    switch (bioSex.biologicalSex) {
        case HKBiologicalSexNotSet:
            value = @"unknown";
            break;
        case HKBiologicalSexFemale:
            value = @"female";
            break;
        case HKBiologicalSexMale:
            value = @"male";
            break;
        case HKBiologicalSexOther:
            value = @"other";
            break;
    }

    if(value == nil){
        NSLog(@"error getting biological sex: %@", error);
        reject(@"error getting biological sex", nil, nil);
        return;
    }

    NSDictionary *response = @{
            @"value" : value,
    };

    resolve(response);
}


- (void)characteristic_getDateOfBirth:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
    NSError *error;
    NSDate *dob = [self.healthStore dateOfBirthWithError:&error];

    if(error != nil){
        NSLog(@"error getting date of birth: %@", error);
        reject(@"error getting date of birth", nil, nil);
        return;
    }
    if(dob == nil) {
        NSDictionary *response = @{
                                   @"value" : [NSNull null],
                                   @"age" : [NSNull null]
                                   };
        resolve(response);
        return;
    }

    NSString *dobString = [RCTFitKit buildISO8601StringFromDate:dob];

    NSDate *now = [NSDate date];
    NSDateComponents *ageComponents = [[NSCalendar currentCalendar] components:NSCalendarUnitYear fromDate:dob toDate:now options:NSCalendarWrapComponents];
    NSUInteger ageInYears = ageComponents.year;

    NSDictionary *response = @{
            @"value" : dobString,
            @"age" : @(ageInYears),
    };

    resolve(response);
}



@end
