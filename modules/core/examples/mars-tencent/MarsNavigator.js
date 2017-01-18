/**
 * @flow
 */

'use strict';

import React, { Component } from 'React';
import ReactNative,
{
    Dimensions,
    StyleSheet,
    Platform,
    Navigator,
    BackAndroid,

    Route,
} from 'react-native';


import MainView from './MainView';
import ChatView from './ChatView';

class MarsNavigator extends Component {

    _navigator: Navigator;

    constructor(props: any) {
        super(props);

        (this: any).handleBackButton = this.handleBackButton.bind(this);
    }

    componentDidMount() {
        BackAndroid.addEventListener('hardwareBackPress', this.handleBackButton);
    }

    componentWillUnmount() {
        BackAndroid.removeEventListener('hardwareBackPress', this.handleBackButton);
    }

    handleBackButton():boolean {
        this._navigator && this._navigator.pop();
        return true;
    }

    render() {
        return (
            <Navigator
                ref={nav => this._navigator = nav}
                style={styles.container}
                initialRoute={{id:'MainView'}}
                configureScene={() => {
                    return Navigator.SceneConfigs.VerticalDownSwipeJump;
                }}                
                renderScene={this.renderScene}
                />
        );
    }

    renderScene(route: Route, navigator: Navigator) {
        let ScreenClass = null;
        switch (route.id) {

            case 'MainView':
                ScreenClass = MainView;
                break;
            default:
                ScreenClass = ChatView;
                break;
        }
        return React.createElement(ScreenClass, { navigator: navigator, route: route });
    }

}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: 'black',
    },
});

module.exports = MarsNavigator; 
