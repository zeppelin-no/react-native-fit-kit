'use strict'

let { AppleHealthKit } = require('react-native').NativeModules;

import Constants from './constants';

let FitKit = Object.assign({}, AppleHealthKit, {
  Constants: Constants
});

export default FitKit
module.exports = FitKit;
