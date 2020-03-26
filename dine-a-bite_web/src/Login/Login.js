import React from 'react'
import './Login.css';

const Login = () => {
    return(
        <div className="Login">
            <div>
                <label for="username">Username:</label>
                <input 
                    type="text" 
                    id="username"
                    placeholder="username"
                    required
                />
            </div>
            <div>
                <label for="password">Password:</label>
                <input 
                    type="password" 
                    id="password" 
                    name="password" 
                    minlength="8"
                    placeholder="password"
                    required
                />
            </div>
            <div>
                <button>login</button>  
            </div>
        </div>
    )
}

export default Login