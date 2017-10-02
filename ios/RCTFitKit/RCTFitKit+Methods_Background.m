//
//  RCTFitKit+Methods_Background.m
//  RCTFitKit
//

#import "RCTFitKit+Methods_Background.h"
#import "RCTFitKit+Utils.h"

#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>

@implementation RCTFitKit (Methods_Background)

//- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary
//*)launchOptions {
//    [self setTypes];
//    return YES;
//}
//
//-(void) setTypes
//{
//    self.healthStore = [[HKHealthStore alloc] init];
//
//    NSMutableSet* types = [[NSMutableSet alloc]init];
//    [types addObject:[HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount]];
//
//    [self.healthStore requestAuthorizationToShareTypes: types
//                                             readTypes: types
//                                            completion:^(BOOL success, NSError *error) {
//
//                                                dispatch_async(dispatch_get_main_queue(), ^{
//                                                    [self observeQuantityType];
//                                                    [self
//                                                    enableBackgroundDeliveryForQuantityType];
//                                                });
//                                            }];
//}

- (void)background_test:(NSDictionary*)input
               resolver:(RCTPromiseResolveBlock)resolve
               rejecter:(RCTPromiseRejectBlock)reject {
    // HKSampleType* quantityType = [HKSampleType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    //    HKObjectType.characteristicTypeForIdentifier(HKCharacteristicTypeIdentifierDateOfBirth)!,
    //    HKObjectType.characteristicTypeForIdentifier(HKCharacteristicTypeIdentifierBiologicalSex)!,
    //    HKObjectType.quantityTypeForIdentifier(HKQuantityTypeIdentifierBodyMass)!,
    //    HKObjectType.quantityTypeForIdentifier(HKQuantityTypeIdentifierHeight)!,
    //    HKObjectType.workoutType()
    
//    HKObjectType* objectType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    HKSampleType* sample = [HKSampleType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    HKQuantityType* quant = [HKQuantityType quantityTypeForIdentifier:HKQuantityTypeIdentifierHeight];
    HKObjectType* objectType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];

//    HKObjectType* objectType = [HKWorkoutType workoutType];

    //    [self.healthStore disableAllBackgroundDeliveryWithCompletion:^(BOOL success, NSError* error) {}];
    [self observeSampleType:(HKSampleType*)objectType];
    [self enableBackgroundDeliveryForQuantityType:(HKQuantityType*)objectType];

    resolve(@"setting it up!!");
}

- (void)enableBackgroundDeliveryForQuantityType:(HKQuantityType*)objectType {
    //    HKQuantityType* stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    //    HKQuantityType* lol = [HKWorkoutType workoutType];
    
    [self.healthStore
        enableBackgroundDeliveryForType:objectType
                              frequency:HKUpdateFrequencyImmediate
                         withCompletion:^(BOOL success, NSError* error) {
                           if (error) {
                               RCTLogInfo(@"error with enableBackgroundDeliveryForQuantityType: %@", error);
                           }
                           if (success) {
                               RCTLogInfo(@"success setting up enableBackgroundDeliveryForQuantityType");
                           }
                         }];
}

- (void)observeSampleType:(HKSampleType*)sampleType {
    RCTLogInfo(@"setting up observer");
    NSPredicate* predicate = [RCTFitKit predicateForSamplesToday];

    HKObserverQuery* query = [[HKObserverQuery alloc]
        initWithSampleType:sampleType
                 predicate:nil
             updateHandler:^(
                 HKObserverQuery* query, HKObserverQueryCompletionHandler completionHandler, NSError* error) {
               if (error) {
                   RCTLogInfo(@"error with observeQuantityType: %@", error);
               }
               RCTLogInfo(@"in observer!!");

               //               dispatch_async(dispatch_get_main_queue(), ^{
               if (completionHandler) {
                   [self getQuantityResult:sampleType predicate:predicate completionHandler:completionHandler];
               }
               //               });
             }];
    [self.healthStore executeQuery:query];
}

- (void)getQuantityResult:(HKSampleType*)sampleType
                predicate:(NSPredicate*)predicate
        completionHandler:(HKObserverQueryCompletionHandler)completionHandler {
    NSInteger limit = 0;

    //    NSPredicate* predicate = [RCTFitKit predicateForSamplesToday];

    //    NSPredicate* predicate = nil;

    NSString* endKey = HKSampleSortIdentifierEndDate;
    NSSortDescriptor* endDate = [NSSortDescriptor sortDescriptorWithKey:endKey ascending:NO];

    HKSampleQuery* query =
        [[HKSampleQuery alloc] initWithSampleType:sampleType
                                        predicate:predicate
                                            limit:limit
                                  sortDescriptors:@[ endDate ]
                                   resultsHandler:^(HKSampleQuery* query, NSArray* results, NSError* error) {

                                     if (error) {
                                         RCTLogInfo(@"error with getQuantityResult: %@", error);
                                     }

                                     //              dispatch_async(dispatch_get_main_queue(), ^{
                                     // sends the data using HTTP
                                     // [self sendData: [self resultAsNumber:results]];

                                     RCTLogInfo(@"*** appstate %@ ***", results);

                                     RCTLogInfo(@"*** COMPLETING!!!! ***");
                                     RCTLogInfo(@"*** COMPLETING!!!! ***");
                                     RCTLogInfo(@"*** COMPLETING!!!! ***");
                                   RCTLogInfo(@"*** COMPLETING with!!!! %@ ***", completionHandler);

                                     completionHandler();
                                     //                RCTLogInfo(@"*** result %@ ***", RCTSharedApplication());
                                     //                UIApplication* lol = RCTSharedApplication();
                                     //                RCTLogInfo(@"*** result %@ ***", lol);
                                     //                RCTLogInfo(@"*** result %@ ***", [UIApplication
                                     //                sharedApplication]);
                                     //                RCTLogInfo(@"*** result %@ ***",
                                     //                RCTSharedApplication().applicationState);
                                     //                int yolo = RCTSharedApplication().applicationState;
                                     //                RCTLogInfo(@"*** result %@ ***", yolo);
                                     //
                                     //                RCTLogInfo(@"*** applicationState %@ ***", [[UIApplication
                                     //                sharedApplication] applicationState]);
                                     //
                                     //                  UIApplicationState har = [UIApplication
                                     //                  sharedApplication].applicationState;
                                     //                  RCTLogInfo(@"*** harhahrhahr %@ ***", har);
                                     //
                                     //
                                     //                if ([[UIApplication sharedApplication] applicationState] ==
                                     //                UIApplicationStateBackground) {
                                     //                    UILocalNotification* notification = [[UILocalNotification
                                     //                    alloc] init];
                                     //                    notification.fireDate = [[NSDate date]
                                     //                    dateByAddingTimeInterval:10];
                                     //                    notification.alertBody = @"loool :)";
                                     //                    [[UIApplication sharedApplication]
                                     //                    scheduleLocalNotification:notification];
                                     //                }
                                     // [self.bridge.eventDispatcher sendAppEventWithName:@"FitKitStepObserverEvent"
                                     // body:@{ @"steps" : @1 }];

                                     //              });
                                   }];
    [self.healthStore executeQuery:query];
}

@end
