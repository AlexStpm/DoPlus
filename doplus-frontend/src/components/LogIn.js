import { useRef, useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

import axios from '../api/axios';

const LOGIN_URL = '/authenticate'

const LogIn = () => {

    const navigate = useNavigate();
    const location = useLocation();
    const from = location.state?.from?.pathname || "/";

    const userRef = useRef();
    const errRef = useRef();

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errMsg, setErrMsg] = useState('');


    useEffect(() => {
        userRef.current.focus();
        localStorage.clear();
    }, [])

    useEffect(() => {
        setErrMsg("");
    }, [username, password])

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(LOGIN_URL, { username, password });
            localStorage.setItem('accessToken', response.data.access_token);
            localStorage.setItem('user', JSON.stringify(response.data.user_info));
            setUsername("");
            setPassword("");
            if (response.data.user_info.username === "rootadmin") {
                navigate('/admin', { replace: true });
            } else {
                navigate('/board', { replace: true });
            }
        }
        catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else if (err.response?.status === 400) {
                setErrMsg("Missing Username or Password");
            } else if (err.response?.status === 404 || err.response?.status === 403) {
                setErrMsg("Incorrect username or password");
            } else if (err.response?.status === 410) {
                setErrMsg("User is not active");
            } else {
                setErrMsg("Login Failed");
            }
            errRef.current.focus();
        }

    }

    return (
        <div className='LogInForm'>
            <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
            <h1>Sign In</h1>
            <form onSubmit={handleSubmit}>
                <label htmlFor="username">Username:</label>
                <input
                    type="text"
                    id='username'
                    ref={userRef}
                    onChange={(e) => setUsername(e.target.value)}
                    value={username}
                    required
                />

                <label htmlFor="password">Password:</label>
                <input
                    type="password"
                    id="password"
                    onChange={(e) => setPassword(e.target.value)}
                    value={password}
                    required
                />
                <button className='cool'>Log In</button>
            </form>
        </div>

    );
}

export default LogIn;