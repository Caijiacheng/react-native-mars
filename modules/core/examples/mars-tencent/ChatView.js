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

import { GiftedChat } from 'react-native-gifted-chat';


var MarsCore = require('react-native-mars-core');
// var MARSHOST = "marsopen.cn"
// var MARSHOST = "localhost"

var main_pb = require('./js/proto/main_pb')
var chat_pb = require('./js/proto/chat_pb')
var topic_pb = require('./js/proto/topic_pb')
var messagepush_pb = require('./js/proto/messagepush_pb')

const PUSH_CMD_ID = 10001
let uid = 3;
let mid = 3;
export default class ChatView extends React.Component {
    constructor(props) {
        super(props);
        this.state = { messages: [], conv: this.props.route.conv, username: this.props.route.username, host: this.props.route.host };
        this.onSend = this.onSend.bind(this);
    }
    componentWillMount() {
        this.setState({
            messages: [
                {
                    _id: 1,
                    text: 'welcome ' + this.state.username + " join " + this.state.conv.getNotice(),
                    createdAt: new Date(Date.UTC(2016, 7, 30, 17, 20, 0)),
                    user: {
                        _id: 2,
                        name: "RNMars",
                        avatar: 'https://facebook.github.io/react/img/logo_og.png',
                    },
                },
            ],
        });
    }

    componentDidMount()
    {
        MarsCore.setOnPushListener((data) => {
            //console.info("onPush data : ", data)
            if (data.cmdid == PUSH_CMD_ID) {
                let mpush = messagepush_pb.MessagePush.deserializeBinary(data.buffer);
                if (mpush.getTopic() === this.state.conv.getTopic())
                {
                    console.info("add => messagePush: " + mpush.getContent() + " from: " + mpush.getFrom())

                    this.setState((previousState) => {
                        return {
                            messages: GiftedChat.append(previousState.messages, [{
                                _id: mid++,
                                text: mpush.getContent() ,
                                createdAt: new Date(Date.UTC(2016, 7, 30, 17, 20, 0)),
                                user: {
                                    _id: uid++,
                                    name: mpush.getFrom(),
                                    avatar: ''
                                },
                            }]),
                        };
                    });

                }else {
                    console.info("skip => messagePush: " + mpush.getContent() + " from: " + mpush.getFrom())
                }
            }


        })
    }

    componentWillUnmount(){
        MarsCore.setOnPushListener(null)
    }

    onSend(messages = []) {

        console.info("messages => ", messages)
        let req = new chat_pb.SendMessageRequest();
        req.setAccessToken("rn_token");
        req.setFrom(this.state.username)
        req.setTo("all")
        req.setText(messages[0].text)
        req.setTopic(this.state.conv.getTopic())
        MarsCore.post(this.state.host, "/mars/sendmessage", req.serializeBinary(), false, true, 3).then(
            res => {

                let response = chat_pb.SendMessageResponse.deserializeBinary(res);
                if (chat_pb.SendMessageResponse.Error.ERR_OK != response.getErrCode()) {
                    //   this.setState({ errinfo: response.getErrMsg() })
                    messages[0].text = "error => " + response.getErrMsg();
                    this.setState((previousState) => {
                        return {
                            messages: GiftedChat.append(previousState.messages, messages),
                        };
                    });

                } else {
                    console.info("sendMessage response ok => ", response.getText())
                    this.setState((previousState) => {
                        return {
                            messages: GiftedChat.append(previousState.messages, messages),
                        };
                    });
                }

            }
        ).catch(err => {
            messages[0].text = "error => " + err;
            this.setState((previousState) => {
                        return {
                            messages: GiftedChat.append(previousState.messages, messages),
                        };
            });
        })

    }
    render() {
        return (
            <GiftedChat
                messages={this.state.messages}
                onSend={this.onSend}
                user={{
                    _id: 1,
                    name: this.state.username,
                }}
                />
        );
    }
}