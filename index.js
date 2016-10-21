'use strict';

let { FitKit } = require('react-native').NativeModules;

import Constants from './constants';
import deviceSpesific from './src/FitKit';

let HealthKit = Object.assign({}, FitKit, {
  Constants: Constants,
  ...deviceSpesific,
});

export default HealthKit;
module.exports = HealthKit;
