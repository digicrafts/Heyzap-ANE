//
//  HeyzapSDK.h
//
//  Copyright 2011 Smart Balloon, Inc. All Rights Reserved
//
//  Permission is hereby granted, free of charge, to any person
//  obtaining a copy of this software and associated documentation
//  files (the "Software"), to deal in the Software without
//  restriction, including without limitation the rights to use,
//  copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the
//  Software is furnished to do so, subject to the following
//  conditions:
//
//  The above copyright notice and this permission notice shall be
//  included in all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
//  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
//  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
//  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
//  OTHER DEALINGS IN THE SOFTWARE.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <StoreKit/StoreKit.h>

typedef enum {
    HZDebugLevelVerbose = 3,
    HZDebugLevelInfo = 2,
    HZDebugLevelError = 1,
    HZDebugLevelSilent = 0
} HZDebugLevel;

typedef NS_OPTIONS(NSUInteger, HZOptions) {
    HZOptionsNone    = 0,
    HZOptionsHideStartScreen   = 1 << 1,
    HZOptionsHideDeleteScore   = 1 << 2,
    HZOptionsShowErrors        = 1 << 3,
    HZOptionsHideNotification  = 1 << 24
};

@protocol HeyzapAchievementProtocol <NSObject>
- (NSString *)heyzapAchievementIdentifier;
@end

@class HZCheckinButton;
@class HZExplainView;
@class HZScore;
@class HZLeaderboardRank;

@protocol HeyzapDelegate <NSObject>
@optional
- (void) heyzapWillAppear: (BOOL) animated;
- (void) heyzapDidAppear: (BOOL) animated;
- (void) heyzapWillDisappear: (BOOL) animated;
- (void) heyzapDidDisappear: (BOOL) animated;
@end

@interface HeyzapSDK : NSObject <HeyzapDelegate>

@property (nonatomic, strong) NSString *appId;
@property (nonatomic, strong) NSURL *appURL;

+ (id) sharedSDK;
+ (BOOL) isSupported;

#pragma mark - Initialization

+ (void) setAppName: (NSString *) passedAppName;
+ (void) startHeyzapWithAppId: (NSString *) appId andAppURL: (NSURL *) url andShowPopup:(BOOL)showPopup;
+ (void) startHeyzapWithAppId:(NSString *) appId andShowPopup:(BOOL)showPopup;

+ (void) startHeyzapWithAppId:(NSString *)appId andOptions: (HZOptions) options;
+ (void) startHeyzapWithAppId: (NSString *) appId andAppURL: (NSURL *) url andOptions: (HZOptions) options;


+ (HZCheckinButton*) getCheckinButtonWithLocation: (CGPoint) location;
+ (HZCheckinButton*) getCheckinButtonWithLocation: (CGPoint) location andMessage: (NSString *) message;

#pragma mark - Debug

- (void) setDebugLevel: (HZDebugLevel) debugLevel;

#pragma mark - Checkin Methods

- (IBAction) checkin;
- (IBAction) checkinWithMessage: (NSString *) message;

#pragma mark - Level
- (void) onStartLevel:(void (^)(NSString *))block;

#pragma mark - Leaderboard/Score Methods

// Submit a score for this game.
- (IBAction) submitScore:(HZScore *)score withCompletion: (void(^)(HZLeaderboardRank*, NSError*))completionBlock;
// Open a leaderboard at the specified level ID
- (IBAction) openLeaderboardLevel: (NSString *) levelID;
// Open the leaderboard at the default level ID
- (IBAction) openLeaderboard;

#pragma mark - Parsing URLs coming from Heyzap
extern NSString * const kHeyzapRequestTypeKey;
extern NSString * const kHeyzapRequestArgumentsKey;
+ (BOOL)canParseURL:(NSURL *)url;
+ (NSDictionary *)parseURL:(NSURL *)url;

#pragma mark - Achievements


/** Unlocks achievements and then displays a popup showing the achievements the user has unlocked. This method will only display UI if there are new achievements to show. Because this method displays UI over the screen, call it at e.g. the end of a level. To unlock achievements without interrupting gameplay, see `silentlyUnlockAchievements:`
 
 @param achievementIDs An array of achievement ID strings.
 As a convenience, if you internally represent achievements as objects within your application, you can pass those objects. Just have them conform to the `HeyzapAchievementProtocol`.
 Remember, you need to set your achievement identifiers using the Developer Dashboard on the Heyzap website.
 */
- (void)unlockAchievementsWithIDs:(NSArray *)achievementIDs completion:(void(^)(NSArray *achievements, NSError *error, BOOL *showPopup))block;

/** Stores achievement IDs in `NSUserDefaults` that should be unlocked on the next call of `unlockAchievementsWithIDs:completion:` without adding UI to the screen. Use this method to unlock an achievement without interrupting gameplay.
 
 @param achievementIDs This parameter has the same semantics as in the `unlockAchievementsWithIDs:completion:`. It is safe to call this method multiple times with the same achievement and to pass duplicate achievements -- the UI displayed by `unlockAchievementsWithIDs:completion:` will always be accurate at displaying what achievements are new.
 */
- (void)silentlyUnlockAchievements:(NSArray *)achievementIDs;

- (void)showAllAchievementsWithCompletion:(void(^)(NSArray *achievements, NSError *error, BOOL *showPopup))block;

#pragma mark - Ads
- (void)showAd;
- (void)enableAds;
- (void)showAd:(NSString *)tag;

@end
