import {Header, Body, Title, Container, Content, List, ListItem, Button, Text, CheckBox, Toast, Root } from 'native-base';
import {StyleProvider} from 'native-base-shoutem-theme';
import React, { Component, TouchableNativeFeedback } from 'react';
import getTheme from './native-base-theme/components';
import material from './native-base-theme/variables/material';
import { Platform, StyleSheet, View } from 'react-native';

import HelloWorld from './DataKitAPI';


type Props = {};
export default class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      showToast: false
    }
  }

  helloWorld=async()=> {
    try {
      let helloWorldStr = await HelloWorld.helloWorld(this);
      console.log(helloWorldStr)
    } catch (e) {
      console.error(e);
    }
  }

  onPressConnect=()=> {
    Toast.show({
      text: 'Connecting to DataKit',
      position: 'bottom',
      type: 'success',
      duration: 1500 })
  }

  datakitConnect=async()=> {
    try {
      let connect = await DataKitAPI.connectDataKit();
      Toast.show({
        text: connect,
        position: 'bottom',
        type: 'success',
        duration: 1500
      })
    } catch (e) {
      console.error(e);
    }
  }

  render() {
    return (
      <Root>
        <StyleProvider style={getTheme(material)}>
          <Container>
            <Header>
              <Body>
                <Title>mCerebrum DemoApp</Title>
              </Body>
            </Header>
            <Content>
              <List>
                <ListItem>
                  <Button block bordered brandPrimary onPress={this.datakitConnect}>
                    <Text>Connect</Text>
                  </Button>
                </ListItem>
                <ListItem>
                  <Button block bordered brandPrimary onPress={this.onPressConnect}>
                    <Text>Register</Text>
                  </Button>
                </ListItem>
                <ListItem>
                  <Button block bordered brandPrimary>
                    <Text>Subscribe</Text>
                  </Button>
                </ListItem>
                <ListItem>
                  <Button block bordered brandPrimary>
                    <Text>Insert</Text>
                  </Button>
                  <CheckBox padding checked={false} />
                  <Body>
                    <Text>High frequency insert</Text>
                  </Body>
                </ListItem>
                <ListItem>
                  <Button block bordered brandPrimary>
                    <Text>Query</Text>
                  </Button>
                </ListItem>
              </List>
            </Content>
          </Container>
        </StyleProvider>
      </Root>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#2b3675',
  },
  button: {
    fontSize: 20,
    backgroundColor: 'white'
  },
  checkbox: {

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
