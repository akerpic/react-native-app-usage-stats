# react-native-app-usage-stats

React Native module for querying per-app foreground usage time on Android. Returns a graceful `E_NOT_SUPPORTED` error on iOS.

## Platform Support

| Feature                  | Android | iOS            |
| ------------------------ | ------- | -------------- |
| `openUsageAccessSettings` | Yes     | Rejects E_NOT_SUPPORTED |
| `hasUsageAccess`          | Yes     | Returns `false` |
| `getUsageByApps`          | Yes     | Rejects E_NOT_SUPPORTED |
| `getUsageAllApps`         | Yes     | Rejects E_NOT_SUPPORTED |

> **Why no iOS data?** iOS does not expose per-app foreground usage time to third-party apps. The Screen Time APIs (FamilyControls / DeviceActivity) are restricted to managed device and parental control use cases with special entitlements.

## Installation

```sh
npm install react-native-app-usage-stats
# or
yarn add react-native-app-usage-stats
```

### iOS

```sh
cd ios && pod install
```

The iOS pod compiles but all data methods return `E_NOT_SUPPORTED`.

### Android

**No extra linking needed** for RN >= 0.60 (autolinking).

Add the usage stats permission to your app's `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest ...>
    <uses-permission
        android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions"
        xmlns:tools="http://schemas.android.com/tools" />
    ...
</manifest>
```

> **Important:** `PACKAGE_USAGE_STATS` is a special permission. It cannot be requested at runtime via a dialog. The user must manually grant it in **Settings > Apps > Special access > Usage access**. Use `openUsageAccessSettings()` to navigate the user there.

#### OEM Quirks

- **Samsung / Xiaomi / Huawei**: Some OEMs restrict background data collection. If `getUsageByApps` returns 0 for all apps despite permission being granted, advise users to also disable battery optimization for your app.
- **Android 10+**: Uses `appOpsManager.unsafeCheckOpNoThrow()` for permission checks (handled automatically by the module).

## API

```typescript
import {
  openUsageAccessSettings,
  hasUsageAccess,
  getUsageByApps,
  getUsageAllApps,
} from 'react-native-app-usage-stats';
```

### `openUsageAccessSettings(): Promise<void>`

Opens the system Usage Access settings screen. Android only.

### `hasUsageAccess(): Promise<boolean>`

Returns `true` if the app has usage stats permission. iOS always returns `false`.

### `getUsageByApps(packageNames: string[], days: number): Promise<AppUsageRecord[]>`

Returns foreground usage for the specified packages over the last `days` days (including today).

```typescript
const data = await getUsageByApps(
  ['com.instagram.android', 'com.twitter.android'],
  7
);
// [
//   { trackId: 'com.instagram.android', usage: 3600, date: '2026-03-06' },
//   { trackId: 'com.twitter.android',   usage: 1200, date: '2026-03-06' },
//   { trackId: 'com.instagram.android', usage: 2400, date: '2026-03-05' },
//   ...
// ]
```

### `getUsageAllApps(days: number): Promise<DailyUsageReport[]>`

Returns foreground usage for ALL apps over the last `days` days.

```typescript
const reports = await getUsageAllApps(3);
// [
//   {
//     date: '2026-03-06',
//     data: [
//       { trackId: 'com.instagram.android', usage: 3600 },
//       { trackId: 'com.android.chrome',    usage: 7200 },
//     ]
//   },
//   ...
// ]
```

### Types

```typescript
interface AppUsageRecord {
  trackId: string;   // package name
  usage: number;     // seconds
  date: string;      // YYYY-MM-DD
}

interface DailyUsageReport {
  date: string;
  data: Array<{ trackId: string; usage: number }>;
}
```

### Error Codes

| Code                 | When                                      |
| -------------------- | ----------------------------------------- |
| `E_PERMISSION_DENIED` | Usage access not granted (Android)        |
| `E_NOT_SUPPORTED`     | Called a data method on iOS                |
| `E_UNKNOWN`           | Unexpected error                          |

## Compatibility

- React Native >= 0.71
- Android `minSdkVersion` 21 (Android 5.0+)
- iOS 13+ (compiles; returns `E_NOT_SUPPORTED` for data)

## Example Usage

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text, Button, Platform, Alert } from 'react-native';
import {
  openUsageAccessSettings,
  hasUsageAccess,
  getUsageByApps,
} from 'react-native-app-usage-stats';

export default function App() {
  const [hasAccess, setHasAccess] = useState(false);
  const [usage, setUsage] = useState<any[]>([]);

  useEffect(() => {
    hasUsageAccess().then(setHasAccess);
  }, []);

  const fetchUsage = async () => {
    try {
      const data = await getUsageByApps(
        ['com.instagram.android', 'com.android.chrome'],
        7
      );
      setUsage(data);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    }
  };

  return (
    <View style={{ flex: 1, padding: 20, paddingTop: 60 }}>
      <Text>Permission: {hasAccess ? 'Granted' : 'Not granted'}</Text>

      {!hasAccess && Platform.OS === 'android' && (
        <Button
          title="Grant Usage Access"
          onPress={openUsageAccessSettings}
        />
      )}

      {hasAccess && (
        <Button title="Fetch 7-day usage" onPress={fetchUsage} />
      )}

      {usage.map((item, i) => (
        <Text key={i}>
          {item.date} | {item.trackId} | {Math.round(item.usage / 60)} min
        </Text>
      ))}
    </View>
  );
}
```

## License

MIT
