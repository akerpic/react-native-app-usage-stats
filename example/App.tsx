/**
 * Example app for react-native-app-usage-stats
 *
 * Usage:
 *   npx react-native init UsageStatsExample --template react-native-template-typescript
 *   cd UsageStatsExample
 *   npm install ../react-native-app-usage-stats   # or yarn add file:../react-native-app-usage-stats
 *   # Add PACKAGE_USAGE_STATS permission to android/app/src/main/AndroidManifest.xml
 *   npx react-native run-android
 */

import React, { useEffect, useState } from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  Text,
  Button,
  Platform,
  Alert,
  StyleSheet,
} from 'react-native';
import {
  openUsageAccessSettings,
  hasUsageAccess,
  getUsageByApps,
  getUsageAllApps,
  type AppUsageRecord,
  type DailyUsageReport,
} from 'react-native-app-usage-stats';

export default function App() {
  const [hasAccess, setHasAccess] = useState(false);
  const [byApps, setByApps] = useState<AppUsageRecord[]>([]);
  const [allApps, setAllApps] = useState<DailyUsageReport[]>([]);

  const checkAccess = async () => {
    const granted = await hasUsageAccess();
    setHasAccess(granted);
  };

  useEffect(() => {
    checkAccess();
  }, []);

  const fetchByApps = async () => {
    try {
      const data = await getUsageByApps(
        ['com.instagram.android', 'com.android.chrome', 'com.whatsapp'],
        7
      );
      setByApps(data);
    } catch (e: any) {
      Alert.alert(e.code ?? 'Error', e.message);
    }
  };

  const fetchAllApps = async () => {
    try {
      const data = await getUsageAllApps(3);
      setAllApps(data);
    } catch (e: any) {
      Alert.alert(e.code ?? 'Error', e.message);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scroll}>
        <Text style={styles.title}>react-native-app-usage-stats</Text>
        <Text style={styles.status}>
          Permission: {hasAccess ? 'GRANTED' : 'NOT GRANTED'}
        </Text>

        {!hasAccess && Platform.OS === 'android' && (
          <Button title="Open Usage Access Settings" onPress={openUsageAccessSettings} />
        )}

        <View style={styles.spacer} />
        <Button title="Refresh permission" onPress={checkAccess} />
        <View style={styles.spacer} />
        <Button title="Get usage by apps (7 days)" onPress={fetchByApps} />
        <View style={styles.spacer} />
        <Button title="Get all apps (3 days)" onPress={fetchAllApps} />

        {byApps.length > 0 && (
          <>
            <Text style={styles.heading}>By Apps:</Text>
            {byApps.map((item, i) => (
              <Text key={i} style={styles.row}>
                {item.date} | {item.trackId} | {Math.round(item.usage / 60)} min
              </Text>
            ))}
          </>
        )}

        {allApps.length > 0 && (
          <>
            <Text style={styles.heading}>All Apps:</Text>
            {allApps.map((day, i) => (
              <View key={i}>
                <Text style={styles.dateHeader}>{day.date}</Text>
                {day.data.map((app, j) => (
                  <Text key={j} style={styles.row}>
                    {'  '}{app.trackId}: {Math.round(app.usage / 60)} min
                  </Text>
                ))}
              </View>
            ))}
          </>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  scroll: { padding: 20 },
  title: { fontSize: 20, fontWeight: 'bold', marginBottom: 12 },
  status: { fontSize: 16, marginBottom: 16 },
  spacer: { height: 10 },
  heading: { fontSize: 16, fontWeight: '600', marginTop: 20, marginBottom: 8 },
  dateHeader: { fontSize: 14, fontWeight: '600', marginTop: 10 },
  row: { fontSize: 12, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace', marginBottom: 2 },
});
