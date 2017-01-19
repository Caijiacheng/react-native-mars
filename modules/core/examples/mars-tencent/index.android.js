/**
 * @flow
 */
'use strict';

import React, { Component } from 'react';
import {
  AppRegistry,
} from 'react-native';

import MarsNavigator from './MarsNavigator';

class App extends Component {

  render() {
    return (
      <MarsNavigator />
    )
  }
}

AppRegistry.registerComponent('example', () => App);