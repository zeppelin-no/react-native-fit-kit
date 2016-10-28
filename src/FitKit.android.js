'use strict';

let { FitKit } = require('react-native').NativeModules;
import { PermissionsAndroid } from 'react-native';

const initFitKit = options => {
  return new Promise(async (resolve, reject) => {
    try {
      const granted = await PermissionsAndroid.requestPermission(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION, {
          title: 'ACCESS_FINE_LOCATION Permission',
          message: 'loool',
        }
      );

      if (granted) {
        resolve(true);
      } else {
        reject(false);
      }
    } catch (e) {
      reject(false);
      console.log('ojda');
    }
  });
};

const getActivities = options => (
  new Promise(async (resolve, reject) => {
    try {
      const hasPermission = PermissionsAndroid.checkPermission(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);

      if (!hasPermission) {
        const granted = await initFitKit();
        if (!granted) {
          reject('did not get permission');
        }
      }

      const activities = await FitKit.getActivities(options);
      if (activities) {
        resolve(activities);
      } else {
        reject('could not get activities');
      }
    } catch (e) {
      reject('could not get activities');
    }
  })
);

export default {
  initFitKit,
  getActivities,
};
module.exports = {
  initFitKit,
  getActivities,
};
