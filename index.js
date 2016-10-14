'use strict'

let { FitKit } = require('react-native').NativeModules;

import Constants from './constants';

console.log(FitKit);

let HealthKit = Object.assign({}, FitKit, {
  Constants: Constants
});

export default HealthKit
module.exports = HealthKit;
