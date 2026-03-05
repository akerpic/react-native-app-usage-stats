import Foundation

@objc(UsageStatsModule)
class UsageStatsModule: NSObject {

    private static let E_NOT_SUPPORTED = "E_NOT_SUPPORTED"
    private static let notSupportedMessage =
        "App usage statistics are not available on iOS. " +
        "iOS does not provide APIs to query per-app foreground usage time. " +
        "Screen Time data is only accessible via the FamilyControls / DeviceActivity " +
        "frameworks which require special entitlements and are designed for parental controls."

    @objc
    func openUsageAccessSettings(_ resolve: @escaping RCTPromiseResolveBlock,
                                  rejecter reject: @escaping RCTPromiseRejectBlock) {
        reject(UsageStatsModule.E_NOT_SUPPORTED,
               "openUsageAccessSettings is not supported on iOS.",
               nil)
    }

    @objc
    func hasUsageAccess(_ resolve: @escaping RCTPromiseResolveBlock,
                         rejecter reject: @escaping RCTPromiseRejectBlock) {
        resolve(false)
    }

    @objc
    func getUsageByApps(_ packageNames: [String],
                         days: Int,
                         resolver resolve: @escaping RCTPromiseResolveBlock,
                         rejecter reject: @escaping RCTPromiseRejectBlock) {
        reject(UsageStatsModule.E_NOT_SUPPORTED,
               UsageStatsModule.notSupportedMessage,
               nil)
    }

    @objc
    func getUsageAllApps(_ days: Int,
                          resolver resolve: @escaping RCTPromiseResolveBlock,
                          rejecter reject: @escaping RCTPromiseRejectBlock) {
        reject(UsageStatsModule.E_NOT_SUPPORTED,
               UsageStatsModule.notSupportedMessage,
               nil)
    }

    @objc
    static func requiresMainQueueSetup() -> Bool {
        return false
    }
}
