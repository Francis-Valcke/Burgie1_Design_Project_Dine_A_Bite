import React, { Component } from "react";
import SidebarEntry from "./SidebarEntry";


class Sidebar extends Component {

    render(){
        return(
            <div>
                <SidebarEntry> Hanlo </SidebarEntry>
                <SidebarEntry> Vrindt </SidebarEntry>
            </div>
        )
    }
}

export default Sidebar