//
//  HZScore.h
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

@interface HZScore : NSObject <NSCoding>

// Display Score: The score seen by the user in the game and on the leaderboard
// e.g. "25 points", "15.43 seconds", "$25", etc. (Required)
@property (nonatomic, strong) NSString *displayScore;

// Relative Score: A number used to rank the score against other player scores. (Required)
@property (nonatomic) float relativeScore;

// The ID of the level, specified a new level was added in the Heyzap Developer
// section in the website. (Required)
@property (nonatomic, strong) NSString *levelID;

// The username of the player in the game. Helps Heyzap keep track of the correct scores,
// and correctly match up players. (Optional)
@property (nonatomic, strong) NSString *username;

// This value is only available when you are getting a score back from Heyzap
@property (nonatomic, readonly) int rank;

// Initialization method
- (id) initWithLevelID: (NSString *) levelID;

// Call this to determine the data you have put in is valid.
- (BOOL) isValid;

@end
