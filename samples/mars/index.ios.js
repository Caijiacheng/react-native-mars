/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from 'react-native';

var main_pb = require('./js/proto/main_pb');


export default class mars extends Component {


  testProto()
  {
      //
      
      var request = new main_pb.HelloRequest();
      request.setUser("rn_user")
      request.setText("rn_content")

      console.info("res => ", request)

      var bytes = request.serializeBinary();

      console.info("bytes => ", bytes)
      
      var res = main_pb.HelloRequest.deserializeBinary(bytes)

      console.info("res => ", res)

      

  }  

  render() {

    this.testProto();

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.ios.js
        </Text>
        <Text style={styles.instructions}>
          Press Cmd+R to reload,{'\n'}
          Cmd+D or shake for dev menu
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('mars', () => mars);
