import React, {Component} from 'react'
import EventCreatorEntry from './EventCreatorEntry';

class EventCreator extends Component {

    state = {
        nodes: [],
        status: null
    }

    onAddNodeHandler = () => {
        this.setState({
            nodes: this.state.nodes.push(EventCreatorEntry())
            // status: "Moekoren"
        })
    }

    render(){

        
        
        return(
            <div>
                <div>
                    
                </div>
                <div>
                    <button onClick={this.onAddNodeHandler}>
                        Add
                    </button>
                </div>
                <div>
                    <p>{this.state.status}</p>
                </div>
            </div>
        )
    }
}

export default EventCreator