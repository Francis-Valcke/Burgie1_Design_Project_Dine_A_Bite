import React, {Component} from 'react'

const EventCreatorEntry = (props) => {
    return(
        <div>
            <input 
                type="text" 
                placeholder="lon"
                required
            />
            <input 
                type="text" 
                placeholder="lat"
                required
            />
        </div>
    )
}

export default EventCreatorEntry