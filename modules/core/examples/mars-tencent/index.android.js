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
  View,
  Button,
  TouchableOpacity,
  Platform,
  TouchableNativeFeedback
} from 'react-native';

var MarsCore = require('react-native-mars-core');
var CONVERSATION_HOST = "marsopen.cn"

var main_pb = require('./js/proto/main_pb')

export default class example extends Component {

  constructor(props) {
      super();
      MarsCore.init(8080, CONVERSATION_HOST, [8081], 1)
      this.state = {
        //convlist : ['STD_DISCUSS', 'STN_DISCUSS', 'OTHER_DISCUSS']
        convlist : null,
        errinfo: null
      };
      this.queryConversationList.bind(this)
  }

  componentWillMount() {
    MarsCore.setOnPushListener(this.onRecvPush);
    this.queryConversationList()

  }


  onRecvPush(obj) {
    console.info("onRecvPush => ", JSON.stringify(obj))
  }

  queryConversationList() {
      let req = new main_pb.ConversationListRequest()
      req.setType(main_pb.ConversationListRequest.FilterType.DEFAULT);
      req.setAccessToken("")
      MarsCore.post(CONVERSATION_HOST, "/mars/getconvlist", req.serializeBinary()).then(res => {
        let response = main_pb.ConversationListResponse.deserializeBinary(res);
        console.info("response => ", response)
        // debugger
        let slist = []
        for (let k of response.getListList())
        {
            let s = "[notice]:" + k.getNotice() + '\n[topic]:' + k.getTopic() + " [name]:" + k.getName()
            slist.push(s)
        }

        this.setState({convlist: slist})
      }).catch(err => {
        // console.err("err to post => ", err)
        this.setState({errinfo : err})
      });
  }

  


  render() {

    if (this.state.convlist) {
       const buttonStyles = [styles.button, {width:200, height:80, justifyContent:'center'}];
       const textStyles = [styles.text];
       const Touchable = Platform.OS === 'android' ? TouchableNativeFeedback : TouchableOpacity;
      return (
        <View style={styles.convlist}>
          <Touchable
                //onPress={this.queryConversationList}
                
          >
             <View style={buttonStyles}>
                <Text style={textStyles}>{this.state.convlist[0]}</Text>
            </View>
          </Touchable>
          <Touchable
                //onPress={this.queryConversationList}
          >
             <View style={buttonStyles}>
                <Text style={textStyles}>{this.state.convlist[1]}</Text>
            </View>
          </Touchable>

          <Touchable
                //onPress={this.queryConversationList}
          >
             <View style={buttonStyles}>
                <Text style={textStyles}>{this.state.convlist[2]}</Text>
            </View>
           </Touchable>

        </View>
      )
    }

    let info = 'Mars is loading ....'

    if (this.state.errinfo) {
      info = this.state.errinfo
    }

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native Mars!
        </Text>

        <Text style={styles.instructions}>
            {info}
        </Text>



      </View>
    );
  }
}


let defaultBlue = '#2196F3';
if (Platform.OS === 'ios') {
  // Measured default tintColor from iOS 10
  defaultBlue = '#0C42FD';
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

  convlist: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },

  button: Platform.select({
    ios: {},
    android: {
      elevation: 4,
      backgroundColor: defaultBlue,
      borderRadius: 2,
      
    },
    
    
  }),
  text: Platform.select({
    ios: {
      color: defaultBlue,
      textAlign: 'center',
      padding: 8,
      fontSize: 18,
      
    },
    android: {
      textAlign: 'center',
      color: 'white',
      padding: 8,
      fontWeight: '500',
      // backgroundColor: "black"
    },
  }),


});

AppRegistry.registerComponent('example', () => example);
