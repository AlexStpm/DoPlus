import useAxiosPrivate from "../hooks/useAxiosPrivate"
import { useState, useEffect, useRef } from "react";
import Toolbar from "./Toolbar";

const AdminPage = () => {
    const axiosPrivate = useAxiosPrivate();
    const [users, setUsers] = useState([]);
    const [errMsg, setErrMsg] = useState('');
    const [searchedUser, setSearchedUser] = useState('');
    const [createUserRequest, setCreateUserRequest] = useState({
        "firstName": "",
        "lastName": "",
        "username": "",
        "password": ""

    });

    const errRef = useRef();

    const fetchUsers = async () => {
        try {
            const response = await axiosPrivate.get('/admin/getallusers');
            setUsers(response.data);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get users Failed");
            }
            errRef.current.focus();
        }
    };

    useEffect(() => {
        fetchUsers();
    }, [])

    const handleInputChange = (userId, field, value) => {
        const updatedUsers = users.map((user) => {
            if (user.id === userId) {
                return { ...user, [field]: value };
            }
            return user;
        });
        setUsers(updatedUsers);
    };

    const handleSearch = (text) => {
        setSearchedUser(text);
    }

    useEffect(() => {
        setErrMsg("");
    }, [users])

    const nameRegex = /^[a-zA-Zа-яА-Я0-9]{3,30}$/;
    const usernameRegex = /^[a-zA-Z0-9]{3,15}$/;
    const passwordRegex = /^.{8,32}$/;

    const isUserValid = (user) => {
        if (!nameRegex.test(user.firstName)) {
            setErrMsg("Please ensure that the first name is between 3 and 30 characters in length, and does not contain any punctuation marks or special symbols.");
            return false;
        }
        if (!nameRegex.test(user.lastName)) {
            setErrMsg("Please ensure that the last name is between 3 and 30 characters in length, and does not contain any punctuation marks or special symbols.");
            return false;
        }
        if (!usernameRegex.test(user.username)) {
            setErrMsg("Ensure that the username is between 3 and 15 characters in length, consisting only of letters and numbers without any punctuation marks or special symbols.");
            return false;
        }
        return true;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!isUserValid(createUserRequest)) {
            return;
        }
        if (!passwordRegex.test(createUserRequest.password)) {
            setErrMsg("Ensure that the password is between 8 and 32 characters in length.");
            return;
        }
        try {
            await axiosPrivate.post('/register', createUserRequest);
            fetchUsers();
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else if (err.response?.status === 409) {
                setErrMsg("User with this username already exists");
            } else {
                setErrMsg("User creation failed");
            }
            errRef.current.focus();
        }
    }

    const handleChange = (e) => {
        const { name, value } = e.target;
        setCreateUserRequest((prevRequest) => ({ ...prevRequest, [name]: value }));
        setErrMsg("");
    };

    const handleUpdateClick = async (userId) => {
        const userToUpdate = users.find((user) => user.id === userId);
        if (!isUserValid(userToUpdate)) {
            return;
        }
        const updateRequest = {
            username: userToUpdate.username,
            firstName: userToUpdate.firstName,
            lastName: userToUpdate.lastName,
            isActive: userToUpdate.isActive ? 1 : 0
        };

        try {
            await axiosPrivate.put(`/admin/updateuser/${userId}`, updateRequest);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else if (err.response?.status === 409) {
                setErrMsg("User with this username already exists");
            } else {
                setErrMsg("User update  Failed");
            }
            errRef.current.focus();
        }
    };
    const changeActive = async (userId) => {

        const userToUpdate = users.find((user) => user.id === userId);
        if (!isUserValid(userToUpdate)) {
            return;
        }
        const updateRequest = {
            username: userToUpdate.username,
            firstName: userToUpdate.firstName,
            lastName: userToUpdate.lastName,
            isActive: userToUpdate.isActive ? 0 : 1
        };
        try {
            await axiosPrivate.put(`/admin/updateuser/${userId}`, updateRequest);

            setUsers((prevUsers) =>
                prevUsers.map((user) =>
                    user.id === userId ? { ...user, isActive: updateRequest.isActive } : user
                )
            );
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else if (err.response?.status === 409) {
                setErrMsg("User with this username already exists");
            } else {
                setErrMsg("User update  Failed");
            }
            errRef.current.focus();
        }
    }

    const activeUsers = users.filter((user) => user.isActive);
    const inactiveUsers = users.filter((user) => !user.isActive);

    return (
        <div className="Admin">
            <Toolbar isAdmin={true} />
            <h1>Manage users</h1>
            <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
            <div className="AdminPage">

                <div className="usersTable">
                    <input
                        className="SearchBar"
                        type="text"
                        value={searchedUser}
                        placeholder="Search by username"
                        onChange={(e) => handleSearch(e.target.value)}
                    />
                    <table>
                        <thead>
                            <tr>
                                <th>Username</th>
                                <th>First name</th>
                                <th>Last Name</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {activeUsers.map((user) => (
                                user.username.includes(searchedUser) &&
                                <tr key={user.id}>
                                    <td>
                                        <input
                                            type="text"
                                            value={user.username}
                                            data-testid = "usernameTableInput"
                                            onChange={(e) => handleInputChange(user.id, 'username', e.target.value)}
                                        />
                                    </td>
                                    <td>
                                        <input
                                            type="text"
                                            value={user.firstName}
                                            data-testid = "firstnameTableInput"
                                            onChange={(e) => handleInputChange(user.id, 'firstName', e.target.value)}
                                        />
                                    </td>
                                    <td>
                                        <input
                                            type="text"
                                            value={user.lastName}
                                            data-testid = "lastnameTableInput"
                                            onChange={(e) => handleInputChange(user.id, 'lastName', e.target.value)}
                                        />
                                    </td>
                                    <td>
                                        <button className="btn btn-warning" onClick={() => handleUpdateClick(user.id)}>Update</button>
                                        <button className={user.isActive ? 'btn btn-danger' : 'btn btn-success'} onClick={() => changeActive(user.id)}>
                                            {user.isActive ? 'Deactivate' : 'Activate'}
                                        </button>

                                    </td>
                                </tr>
                            ))}
                        </tbody>
                        <tbody style={{ backgroundColor: 'grey' }}>
                            {inactiveUsers.map((user) => (
                                user.username.includes(searchedUser) &&
                                <tr key={user.id} >
                                    <td>
                                        <input
                                            style={{ backgroundColor: 'grey' }}
                                            type="text"
                                            value={user.username}
                                            onChange={(e) => handleInputChange(user.id, 'username', e.target.value)}
                                        />
                                    </td>
                                    <td>
                                        <input
                                            style={{ backgroundColor: 'grey' }}
                                            type="text"
                                            value={user.firstName}
                                            onChange={(e) => handleInputChange(user.id, 'firstName', e.target.value)}
                                        />
                                    </td>
                                    <td>
                                        <input
                                            style={{ backgroundColor: 'grey' }}
                                            type="text"
                                            value={user.lastName}
                                            onChange={(e) => handleInputChange(user.id, 'lastName', e.target.value)}
                                        />
                                    </td>
                                    <td>
                                    <button className="btn btn-warning" onClick={() => handleUpdateClick(user.id)}>Update</button>
                                        <button className={user.isActive ? 'btn btn-danger' : 'btn btn-success'} onClick={() => changeActive(user.id)}>
                                            {user.isActive ? 'Deactivate' : 'Activate'}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                <div className="AddUserForm">
                    <h2>Create new user</h2>
                    <form onSubmit={handleSubmit}>
                        <label>First name:</label>
                        <input
                            type="text"
                            name="firstName"
                            placeholder="Firstname"
                            data-testid="FirstInput"
                            onChange={handleChange} />
                        <label>Last name:</label>
                        <input
                            type="text"
                            name="lastName"
                            placeholder="Lastname"
                            data-testid="LastInput"
                            onChange={handleChange} />
                        <label>Username:</label>
                        <input
                            type="text"
                            name="username"
                            placeholder="Username"
                            data-testid="usernameInput"
                            onChange={handleChange} />
                        <label>Password:</label>
                        <input
                            type="text"
                            name="password"
                            placeholder="Password"
                            data-testid="PasswordInput"
                            onChange={handleChange} />
                        <button className="btn btn-primary" type="submit">Create User</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default AdminPage
