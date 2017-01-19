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
  TouchableNativeFeedback,
  Modal,
  TouchableHighlight,
  ScrollView
} from 'react-native';

var MarsCore = require('react-native-mars-core');
var MARSHOST = "marsopen.cn"
var LONG_LINK_PORT = [8081]
var SHORT_LINK_PORT = 8080
var CLIENT_ID = 200

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

export default class MainView extends Component {

  constructor(props) {
    super();
    MarsCore.init(SHORT_LINK_PORT, MARSHOST, LONG_LINK_PORT, CLIENT_ID)
    this.state = {
      convlist: null,
      errinfo: null,
      username: 'RNUser-' + RndNum(5), 
      modalVisible : false,

      flow : null,
      connstatus: null,
      cgi_history : [],
      sdt_result : null
      

    };

    MarsCore.setOnStatListener( (state) => {
        if (MarsCore.constant.FLOW_CMDID == state.cmdid) {
          this.setState({flow : state.stat})
        }else if (MarsCore.constant.CONNSTATUS_CMDID == state.cmdid) {
          this.setState({connstatus : state.stat})
        }else if (MarsCore.constant.CGIHISTORY_CMDID == state.cmdid) {
           let newcgi = this.state.cgi_history
           newcgi.push(state.stat)
           this.setState({cgi_history : newcgi})
        }else if (MarsCore.constant.SDTRESULT_CMDID == state.cmdid) {
           this.setState({sdt_result : state.stat})
        }

    });



    this.queryConversationList.bind(this)
    this.refresh.bind(this)
  }

  componentWillMount() {
    this.queryConversationList()



  }

  refresh() {
    this.queryConversationList();
  }

  queryConversationList() {
    let req = new main_pb.ConversationListRequest()
    req.setType(main_pb.ConversationListRequest.FilterType.DEFAULT);
    req.setAccessToken("rn_token")
    MarsCore.post(MARSHOST, "/mars/getconvlist", req.serializeBinary()).then(res => {
      let response = main_pb.ConversationListResponse.deserializeBinary(res);
      this.setState({ convlist: response.getListList(), errinfo : null })
    }).catch(err => {
      // console.err("err to post => ", err)
      this.setState({ errinfo: err + ""  })
    });
  }

  navToChatView(conv)
  {
      this.props.navigator.push({id : 'ChatView', conv: conv, username: this.state.username, host : MARSHOST})
  }


  renderMarsStat()
  {
      let cgiHistory = '------\n'
      cgiHistory = cgiHistory + this.state.cgi_history.join('\n----\n')
      return (        
        <Modal
          animationType={"slide"}
          transparent={false}
          visible={this.state.modalVisible}
          onRequestClose={() => {this.setState({modalVisible : false})}}
          >
         <View style={{marginTop: 22}}>
          <View>
            <Text style={styles.stateTitle}>Flow:</Text>
            <Text>{this.state.flow}</Text>
            <Text style={styles.stateTitle}>Connstatus:</Text>
            <Text >{this.state.connstatus}</Text>
            <Text style={styles.stateTitle}>SdtResult:</Text>
            <Text>{this.state.sdt_result}</Text>
            <Text style={styles.stateTitle}>CgiHistory:</Text>

  
            <ScrollView
              ref={(scrollView) => { _scrollView = scrollView; }}
              automaticallyAdjustContentInsets={false}
              onScroll={() => { console.log('onScroll!'); }}
              scrollEventThrottle={200}
              style={styles.scrollView}>
              <Text>{cgiHistory}</Text>
            </ScrollView>
  


          </View>
          <Button  
             onPress={() => { 
                this.setState({modalVisible : false}) }}
              title="Close">

          </Button>
         </View>
        </Modal>)
  }

  render() {
    let info = 'Mars is loading ....'
    let refresh = null

    if (this.state.errinfo) {
      info = this.state.errinfo
      refresh = (          
          <Button
            onPress={() => {
              this.refresh()
            }}
            title="Refresh"
            accessibilityLabel="This sounds great!"
          /> )
    } else {
      if (this.state.convlist) {
        const buttonStyles = [styles.button, { width: 160, height: 80, justifyContent: 'center' }];
        const textStyles = [styles.text];
        const Touchable = Platform.OS === 'android' ? TouchableNativeFeedback : TouchableOpacity;
        return (
          <View style={styles.convlist}>
            {this.renderMarsStat()}
            <Touchable
              //onPress={() => { this.sendMessage(this.state.convlist[0].getTopic(), "RNMars: hello") } }
              onPress={() => { this.navToChatView(this.state.convlist[0]) } }

              >
              <View style={buttonStyles}>
                <Text style={textStyles}>{toConvShow(this.state.convlist[0])}</Text>
              </View>
            </Touchable>
            <Touchable
              //onPress={() => { this.sendMessage(this.state.convlist[1].getTopic(), "RNMars: hello 1") } }
              onPress={() => { this.navToChatView(this.state.convlist[1]) } }
              >
              <View style={buttonStyles}>
                <Text style={textStyles}>{toConvShow(this.state.convlist[1])}</Text>
              </View>
            </Touchable>

            <Touchable
              //onPress={() => { this.sendMessage(this.state.convlist[2].getTopic(), "RNMars: hello 2") } }
              onPress={() => { this.navToChatView(this.state.convlist[2]) } }
              >
              <View style={buttonStyles}>
                <Text style={textStyles}>{toConvShow(this.state.convlist[2])}</Text>
              </View>
            </Touchable>

            <Touchable
              onPress={() => { 
                this.setState({modalVisible : true}) }}
              >
              <View style={[styles.btnShowStat, {position:'absolute', top:0, right:0}]}>
                <Text style={[textStyles, {fontSize: 12}]}>{"marsStatic"}</Text>
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
        {refresh}

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

  btnShowStat: Platform.select({
    ios: {},
    android: {
      elevation: 4,
      backgroundColor: 'palegreen',
      borderRadius: 2,

    },


  }),


  stateTitle: Platform.select({
    ios: {
      color: 'red',
      textAlign: 'center',
      padding: 8,
      fontSize: 18,

    },
    android: {
      textAlign: 'center',
      color: 'red',
      padding: 8,
      fontWeight: '500',
      // backgroundColor: "black"
    },
  }),
  scrollView: {
    backgroundColor: 'ivory',
    height: 300,
  },
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

