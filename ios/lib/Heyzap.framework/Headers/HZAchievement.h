//
//  Achievement.h
//  Heyzap
//
//  Created by Maximilian Tagher on 12/7/12.
//
//

#import <Foundation/Foundation.h>

@interface HZAchievement : NSObject

@property (nonatomic, strong, readonly) NSString *title;
@property (nonatomic, strong, readonly) NSString *subtitle;
@property (nonatomic, strong, readonly) NSString *imageURLString;
@property (nonatomic, strong, readonly) NSURL *imageURL;

@property (nonatomic, readonly) BOOL unlocked;
@property (nonatomic, readonly) BOOL isNew;

- (HZAchievement *)initWithDictionary:(NSDictionary *)dictionary;

@end
