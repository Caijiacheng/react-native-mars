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
// var MARSHOST = "marsopen.cn"
var MARSHOST = "localhost"

var main_pb = require('./js/proto/main_pb')
var chat_pb = require('./js/proto/chat_pb')
var topic_pb = require('./js/proto/topic_pb')
var messagepush_pb = require('./js/proto/messagepush_pb')

function RndNum(n) {

  var rnd = "";

  for (var i = 0; i < n; i++)

    rnd += Math.floor(Math.random() * 10);

  return rnd;

}

function toConvShow(conv) {
  return "[notice]:" + conv.getNotice() + '\n[topic]:' + conv.getTopic() + " [name]:" + conv.getName()
}

export default class example extends Component {

  constructor(props) {
    super();
    MarsCore.init(8080, MARSHOST, [9081], 200)
    this.state = {
      //convlist : ['STD_DISCUSS', 'STN_DISCUSS', 'OTHER_DISCUSS']
      convlist: null,
      errinfo: null,
      username: '[RNUser:' + RndNum(5) + ']'
    };
    this.queryConversationList.bind(this)
    this.sendMessage.bind(this)

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
    req.setAccessToken("rn_token")
    MarsCore.post(MARSHOST, "/mars/getconvlist", req.serializeBinary()).then(res => {
      let response = main_pb.ConversationListResponse.deserializeBinary(res);
      console.info("response => ", response)
      this.setState({ convlist: response.getListList() })
    }).catch(err => {
      // console.err("err to post => ", err)
      this.setState({ errinfo: err })
    });
  }


  sendMessage(topic, msg) {
    let req = new chat_pb.SendMessageRequest();
    req.setAccessToken("rn_token");
    req.setFrom(this.state.username)
    req.setTo("all")
    req.setText(msg)
    req.setTopic(topic)
    MarsCore.post(MARSHOST, "/mars/sendmessage", req.serializeBinary(), false, true, 3).then(
      res => {
        
        let response = chat_pb.SendMessageResponse.deserializeBinary(res);
        if (chat_pb.SendMessageResponse.Error.ERR_OK != response.getErrCode()) {
          this.setState({ errinfo: response.getErrMsg() })
        }else {
          console.info("sendMessage response ok => ", response.getText())
        }
        
      }
    ).catch(err => {
      // console.error("err to post => ", err)
      this.setState({ errinfo: err })
    })

  }



  render() {
    let info = 'Mars is loading ....'

    if (this.state.errinfo) {
      info = this.state.errinfo
    } else {
      if (this.state.convlist) {
        const buttonStyles = [styles.button, { width: 180, height: 80, justifyContent: 'center' }];
        const textStyles = [styles.text];
        const Touchable = Platform.OS === 'android' ? TouchableNativeFeedback : TouchableOpacity;
        return (
          <View style={styles.convlist}>
            <Touchable
              onPress={() => { this.sendMessage(this.state.convlist[0].getTopic(), "RNMars: hello") } }

              >
              <View style={buttonStyles}>
                <Text style={textStyles}>{toConvShow(this.state.convlist[0])}</Text>
              </View>
            </Touchable>
            <Touchable
              onPress={() => { this.sendMessage(this.state.convlist[1].getTopic(), "RNMars: hello 1") } }
              >
              <View style={buttonStyles}>
                <Text style={textStyles}>{toConvShow(this.state.convlist[1])}</Text>
              </View>
            </Touchable>

            <Touchable
              onPress={() => { this.sendMessage(this.state.convlist[2].getTopic(), "RNMars: hello 2") } }
              >
              <View style={buttonStyles}>
                <Text style={textStyles}>{toConvShow(this.state.convlist[2])}</Text>
              </View>
            </Touchable>

          </View>
        )
      }
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
