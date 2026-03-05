#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(UsageStatsModule, NSObject)

RCT_EXTERN_METHOD(openUsageAccessSettings:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(hasUsageAccess:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getUsageByApps:(NSArray *)packageNames
                  days:(NSInteger)days
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getUsageAllApps:(NSInteger)days
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end
