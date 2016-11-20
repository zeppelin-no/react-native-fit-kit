//
//  RCTFitKit+Events.m
//  RCTFitKit
//

#import "RCTFitKit+Events.h"
#import "RCTFitKit+Utils.h"

// #import "RCTEventDispatcher.h"
// #import "RCTEventDispatcher.h"

#import "RCTBridge.h"

@implementation RCTFitKit (Events)


- (void)events_initStepCountObserver:(NSDictionary *)input resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
//    HKSampleType *sampleType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    
    resolve(@"harhar");
//    HKObserverQuery *query =
//    [[HKObserverQuery alloc]
//     initWithSampleType:sampleType
//     predicate:nil
//     updateHandler:^(HKObserverQuery *query,
//                     HKObserverQueryCompletionHandler completionHandler,
//                     NSError *error) {
//         
//         if (error) {
//             // Perform Proper Error Handling Here...
//             NSLog(@"*** An error occured while setting up the stepCount observer. %@ ***", error.localizedDescription);
//             reject(@"An error occured while setting up the stepCount observer", nil, nil);
//             return;
//         }
//         
//         resolve(@"harhar");
//         
//         // [self.bridge.eventDispatcher sendAppEventWithName:@"change:steps" body:@{@"name": @"change:steps"}];
//         
//         // [self sendEventWithName:@"harhar" body:@"yolo"];
//         
//         
//         //RCTEvent fu = [[RCTEvent alloc] init];
//         
//         //[self.bridge.eventDispatcher sendEvent:(id<RCTEvent>)]
//         
//          // If you have subscribed for background updates you must call the completion handler here.
//          // completionHandler();
//          
//          }];
//         
//    [self.healthStore executeQuery:query];
}
     
@end
