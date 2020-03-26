import React, {Component} from 'react';
import './App.css';
import Login from './Login/Login';
import EventCreatorEntry from './EventCreator/EventCreatorEntry';
import EventCreator from './EventCreator/EventCreator';

class App extends Component{
  state = {
    welcomeText: "Hallo"
  }

  render () {
    return(
    <div className="App">
      <EventCreator/>
    </div>
    )
  }
}

export default App;
