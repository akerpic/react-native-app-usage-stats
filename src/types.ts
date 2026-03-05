/**
 * A single app's usage record for a specific date.
 */
export interface AppUsageRecord {
  /** Package name (Android) or bundle identifier (iOS). */
  trackId: string;
  /** Foreground usage time in seconds. */
  usage: number;
  /** Date in YYYY-MM-DD format (device local timezone). */
  date: string;
}

/**
 * A daily report containing usage data for all apps.
 */
export interface DailyUsageReport {
  /** Date in YYYY-MM-DD format (device local timezone). */
  date: string;
  /** Array of per-app usage records for this day. */
  data: Array<{ trackId: string; usage: number }>;
}

/**
 * Stable error codes thrown by the module.
 */
export type UsageStatsErrorCode =
  | 'E_PERMISSION_DENIED'
  | 'E_NOT_SUPPORTED'
  | 'E_UNKNOWN';
