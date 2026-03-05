import { NativeModules, Platform } from 'react-native';
import type { AppUsageRecord, DailyUsageReport } from './types';

export type { AppUsageRecord, DailyUsageReport, UsageStatsErrorCode } from './types';

const LINKING_ERROR =
  `The package 'react-native-app-usage-stats' doesn't seem to be linked. Make sure:\n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const UsageStatsModule =
  NativeModules.UsageStatsModule ??
  new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

/**
 * Opens the system Usage Access settings screen (Android only).
 * On iOS this rejects with E_NOT_SUPPORTED.
 */
export function openUsageAccessSettings(): Promise<void> {
  return UsageStatsModule.openUsageAccessSettings();
}

/**
 * Checks whether the app has Usage Stats permission (Android).
 * On iOS this always resolves `false`.
 */
export function hasUsageAccess(): Promise<boolean> {
  return UsageStatsModule.hasUsageAccess();
}

/**
 * Returns foreground usage data for the specified package names over the last N days.
 *
 * @param packageNames - Array of Android package names to query.
 * @param days - Number of days to look back (including today).
 * @returns Array of per-app, per-day usage records.
 */
export function getUsageByApps(
  packageNames: string[],
  days: number
): Promise<AppUsageRecord[]> {
  return UsageStatsModule.getUsageByApps(packageNames, days);
}

/**
 * Returns foreground usage data for ALL apps over the last N days.
 *
 * @param days - Number of days to look back (including today).
 * @returns Array of daily reports, each containing all apps' usage.
 */
export function getUsageAllApps(days: number): Promise<DailyUsageReport[]> {
  return UsageStatsModule.getUsageAllApps(days);
}
