//
//  RCTFitKit+TypesAndPermissions.h
//  RCTFitKit
//

#import "RCTFitKit.h"

@interface RCTFitKit (TypesAndPermissions)

- (NSSet *)getReadPermsFromOptions:(NSArray *)options;
- (NSSet *)getWritePermsFromOptions:(NSArray *)options;

@end
