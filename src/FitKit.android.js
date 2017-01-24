'use strict';

let { FitKit } = require('react-native').NativeModules;
import { PermissionsAndroid } from 'react-native';

const initFitKit = (options = {}) => {
  return new Promise(async (resolve, reject) => {
    try {
      const hasPermission = await PermissionsAndroid.checkPermission(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
      let granted = true;

      if (!hasPermission) {
        granted = await PermissionsAndroid.requestPermission(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION, {
            title: 'ACCESS_FINE_LOCATION Permission',
            message: '',
          }
        );
      }

      if (granted) {
        const initSuccess = await FitKit.initFitKit(options);

        if (initSuccess) {
          resolve(true);
        } else {
          reject(false);
        }
      } else {
        reject(false);
      }
    } catch (e) {
      reject(false);
    }
  });
};

export default {
  initFitKit,
};
module.exports = {
  initFitKit,
};
