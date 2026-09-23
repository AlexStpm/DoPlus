import { useEffect, useState, useRef } from 'react';
import Toolbar from './Toolbar';
import { useParams, useNavigate } from 'react-router-dom';
import useAxiosPrivate from "../hooks/useAxiosPrivate"
import { FaMinus } from "react-icons/fa6";
import { FiPlus } from "react-icons/fi";

const ManageBoard = () => {
    const navigate = useNavigate();
    const axiosPrivate = useAxiosPrivate();
    const [errMsg, setErrMsg] = useState('');
    const { boardId } = useParams();
    const [owner, setOwner] = useState({});
    const [managers, setManagers] = useState([]);
    const [employees, setEmployees] = useState([]);
    const [users, setUsers] = useState([]);
    const [unemployedUsers, setUnemployedUsers] = useState([]);
    const [newTag, setNewTag] = useState("");
    const [tags, setTags] = useState([]);

    const [isEditing, setIsEditing] = useState(false);
    const [isAddingManager, setIsAddingManager] = useState(false);
    const [isAddingEmployee, setIsAddingEmployee] = useState(false);
    const [isAddingTag, setIsAddingTag] = useState(false);

    const [addManagerUsername, setAddManagerUsername] = useState("");
    const [addEmployeeUsername, setAddEmployeeUsername] = useState("");


    const [boardName, setBoardName] = useState("");

    const errRef = useRef();

    const fetchTags = async () => {
        try {
            const tagsResponse = await axiosPrivate.get(`/board/${boardId}/tags`);
            setTags(tagsResponse.data);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get tags failed");
            }
            errRef.current.focus();
        }
    };

    const fetchInfo = async () => {
        try {
            const usersResponse = await axiosPrivate.get(`/user/all`);
            setUsers(usersResponse.data);
            const ownerResponse = await axiosPrivate.get(`/board/${boardId}/owner`);
            setOwner(ownerResponse.data);
            const managersResponse = await axiosPrivate.get(`/board/${boardId}/managers`);
            setManagers(managersResponse.data);
            const employeesResponse = await axiosPrivate.get(`/board/${boardId}/employees`);
            if (!([ownerResponse.data, ...managersResponse.data].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
                navigate("/unauthorized");
            }
            setEmployees(employeesResponse.data);
            const boardInfoResponse = await axiosPrivate.get(`/board/${boardId}/info`);
            setBoardName(boardInfoResponse.data.name);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get info Failed");
            }
            errRef.current.focus();
        }
    };

    useEffect(() => {
        if (!Number.isNaN(parseInt(boardId))) {
            fetchInfo();
            fetchTags();
        }
    }, [boardId]);
    useEffect(() => {
        setErrMsg("");
    }, [newTag, boardName]);

    useEffect(() => {
        const otherUsers = users.filter(user =>
            user.id != owner.id &&
            !managers.some(manager => manager.id === user.id) &&
            !employees.some(employee => employee.id === user.id)
        );
        setUnemployedUsers(otherUsers);
    }, [owner, managers, employees, users]);

    const handleCreateBoard = async () => {
        if (boardName.trim().length > 140) {
            setErrMsg("Board name must be less than 140 characters long!");
            errRef.current.focus();
            return;
        }
        if (boardName.trim().length == 0) {
            setErrMsg("Board name cannot be empty!");
            errRef.current.focus();
            return;
        }
        var response = {};
        try {
            response = await axiosPrivate({
                url: `/board/add/${JSON.parse(localStorage.getItem("user")).id}`,
                headers: {
                    'Content-Type': 'text/plain'
                },
                method: 'post',
                data: boardName.trim()
            });
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Create Board Failed");
            }
            errRef.current.focus();
            return;
        }
        navigate(`/editBoard/${response.data}`);
    }

    const handleEditClick = () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        setIsEditing(true);
    }

    const handleSaveClick = async () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        if (boardName.trim().length > 140 || boardName.trim().length === 0) {
            setErrMsg("Board name must be less than 140 characters long and not empty!");
            errRef.current.focus();
            return;
        }
        try {
            await axiosPrivate({
                url: `/board/${boardId}/update`,
                headers: {
                    'Content-Type': 'text/plain'
                },
                method: 'put',
                data: boardName.trim()
            });
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get users Failed");
            }
            errRef.current.focus();
            return;
        }
        setIsEditing(false);
    }

    const handleAddManagerClick = () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        setIsAddingManager(true);
        setIsAddingEmployee(false);
        setAddManagerUsername(unemployedUsers[0]?.username);
    }

    const handleAddManagerChange = (managerUsername) => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        setAddManagerUsername(managerUsername);
    }


    const handleAddManager = async () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        const userToAdd = unemployedUsers.find(user => user.username === addManagerUsername);
        setManagers(prev => [...prev, userToAdd]);
        try {
            await axiosPrivate.post(`/board/${boardId}/managers/${userToAdd.id}/add`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Add user to managers failed");
            }
            errRef.current.focus();
        }

        setIsAddingManager(false);
    }

    const handleAddEmployeeClick = () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        setIsAddingEmployee(true);
        setIsAddingManager(false);
        setAddEmployeeUsername(unemployedUsers[0]?.username);
    }

    const handleAddEmployeeChange = (employeeUsername) => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        setAddEmployeeUsername(employeeUsername);
    }

    const handleAddEmployee = async () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        const userToAdd = unemployedUsers.find(user => user.username === addEmployeeUsername);
        setEmployees(prev => [...prev, userToAdd]);
        try {
            await axiosPrivate.post(`/board/${boardId}/employees/${userToAdd.id}/add`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Add user to employees failed");
            }
            errRef.current.focus();
        }
        setIsAddingEmployee(false);
    }

    const handleAddTagClick = async () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        if (!isAddingTag) {
            setIsAddingTag(true);
            return;
        }
        const createTagRequest = {
            "boardId": boardId,
            "name": newTag
        }
        if (newTag.length > 140 || newTag.length === 0) {
            setErrMsg("Tag name must be less than 140 characters long and not empty!");
            errRef.current.focus();
            return;
        }
        try {
            await axiosPrivate.post(`/board/tag/add`, createTagRequest);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Create tag Failed");
            }
            errRef.current.focus();
        }
        fetchTags();
        setIsAddingTag(false);
        setNewTag("");
    }

    const handleDeactivateTag = async (tagId) => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        try {
            await axiosPrivate.put(`/board/tag/${tagId}/deactivate`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Deactivate tag Failed");
            }
            errRef.current.focus();
        }
        fetchTags();
    };

    const handleActivateTag = async (tagId) => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        try {
            await axiosPrivate.put(`/board/tag/${tagId}/activate`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Activate tag Failed");
            }
            errRef.current.focus();
        }
        fetchTags();
    };

    const handleRemoveManager = async (managerId) => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        setIsAddingEmployee(false);
        setIsAddingManager(false);
        try {
            await axiosPrivate.delete(`/board/${boardId}/managers/${managerId}/remove`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Remove manager Failed");
            }
            errRef.current.focus();
        }
        fetchInfo();
    }

    const handleRemoveEmployee = async (employeeId) => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        setIsAddingEmployee(false);
        setIsAddingManager(false);
        try {
            await axiosPrivate.delete(`/board/${boardId}/employees/${employeeId}/remove`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Remove employee Failed");
            }
            errRef.current.focus();
        }
        fetchInfo();
    }

    const handleArchiveBoard = async () => {
        if (!([owner, ...managers].some(user => user.id === JSON.parse(localStorage.getItem("user")).id))) {
            navigate("/unauthorized");
        }
        try {
            await axiosPrivate.put(`/board/${boardId}/archive`);
            navigate(`/board/${boardId}`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Archive board Failed");
            }
            errRef.current.focus();
        }
    }


    return (
        <div className='ManageBoard'>
            <Toolbar />
            {Number.isNaN(parseInt(boardId)) ? <h1>Create new board</h1> : <h1>Board settings</h1>}
            <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
            <div className='ManageBoardPage'>
                {Number.isNaN(parseInt(boardId))
                    ? <div className='CreateBoard'>
                        <input
                            type='text'
                            value={boardName}
                            onChange={(e) => setBoardName(e.target.value)}
                            placeholder='Board Name' />
                        <button className="btn btn-success" onClick={handleCreateBoard}>Create Board</button>
                    </div>
                    : <div className='EditBoard'>
                        {isEditing
                            ? <div className='EditBoardNameForm'>
                                <input
                                    type='text'
                                    value={boardName}
                                    onChange={(e) => setBoardName(e.target.value)}
                                    placeholder='Board Name' />
                                <button className="btn btn-success" onClick={handleSaveClick}>Save</button>
                            </div>
                            : <div className='SavedBoardName'>
                                <p>{boardName}</p>
                                <button className="btn btn-warning" onClick={handleEditClick}>Edit</button>
                            </div>}
                        <div className='RolesAndTags'>
                            <div className='Roles'>
                                <h2>Roles</h2>
                                <h3>Owner</h3>
                                {owner.name} ({owner.username})
                                <div className='Managers'>
                                    <h3>Managers</h3>
                                    {isAddingManager
                                        ? unemployedUsers.length !== 0 ? <div className='AddManagerForm'>
                                            <select data-testid="AddManagerSelect" onChange={(e) => handleAddManagerChange(e.target.value)}>
                                                {unemployedUsers.map(user => <option key={user.id}>{user.username}</option>)}
                                            </select>
                                            <button data-testid="AddManagerButtonSubmit" className="btn btn-success" onClick={handleAddManager}>Add</button>
                                        </div>
                                            : <p className='AddManagerForm'>There are no users left</p>
                                        : <div className='AddManager'>
                                            <button data-testid="AddManagerButtonOpen" className="btn btn-success" onClick={handleAddManagerClick}>Add</button>
                                        </div>}
                                </div>
                                <ul>
                                    {managers.map(manager => (<li key={manager.id}>{manager.name} ({manager.username})
                                        <button data-testid={`RemoveManagerButtonSubmit${manager.id}`} className="removeUserButton btn btn-danger" onClick={() => handleRemoveManager(manager.id)}><FaMinus /></button>
                                    </li>))}
                                </ul>
                                <div className='Employees'>
                                    <h3>Employees</h3>
                                    {isAddingEmployee
                                        ? unemployedUsers.length !== 0 ? <div className='AddEmployeeForm'>
                                            <select data-testid="AddEmployeeSelect" onChange={(e) => handleAddEmployeeChange(e.target.value)}>
                                                {unemployedUsers.map(user => <option key={user.id}>{user.username}</option>)}
                                            </select>
                                            <button data-testid="AddEmployeeButtonSubmit" className="btn btn-success" onClick={handleAddEmployee}>Add</button>
                                        </div>
                                            : <p className='AddEmployeeForm'>There are no users left</p>
                                        : <div className='AddEmployee'>
                                            <button data-testid="AddEmployeeButtonOpen" className="btn btn-success" onClick={handleAddEmployeeClick} >Add</button>
                                        </div>}
                                </div>
                                <ul>
                                    {employees.map(employee => (<li key={employee.id}>{employee.name} ({employee.username})
                                        <button data-testid={`RemoveEmployeeButtonSubmit${employee.id}`} className="removeUserButton btn btn-danger" onClick={() => handleRemoveEmployee(employee.id)}><FaMinus /></button>
                                    </li>))}
                                </ul>
                            </div>
                            <div className='Tags'>
                                <div className='AddTag'>
                                    <h2>Tags</h2>
                                    {isAddingTag && <input
                                        data-testid="TagNameInput"
                                        type='text'
                                        value={newTag}
                                        onChange={(e) => setNewTag(e.target.value)} />}
                                    <button data-testid="AddTagButton" className="btn btn-success" onClick={handleAddTagClick}>Add</button>
                                </div>
                                <h3>Active tags</h3>
                                <ul>
                                    {tags.map(tag => {
                                        return (tag.isActive &&
                                            <div className='TagIsActiveSwitch' key={tag.id}>
                                                <li>
                                                    {tag.name}
                                                </li>
                                                <button data-testid={`DeactivateTagButton${tag.id}`} className="btn btn-danger" onClick={() => handleDeactivateTag(tag.id)}><FaMinus /></button>
                                            </div>)
                                    })}
                                </ul>
                                <h3 className='unactiveTags'>Unactive tags</h3>
                                <ul>
                                    {tags.map(tag => {
                                        return (!tag.isActive &&
                                            <div className='TagIsActiveSwitch' key={tag.id}>
                                                <li>
                                                    {tag.name}
                                                </li>
                                                <button data-testid={`ActivateTagButton${tag.id}`} className="btn btn-success" onClick={() => handleActivateTag(tag.id)}><FiPlus /></button>
                                            </div>)
                                    })}
                                </ul>
                            </div>
                        </div>
                        <div className="ManageBoardActions">
                            {owner.id === JSON.parse(localStorage.getItem("user")).id && <button className="btn btn-warning" onClick={handleArchiveBoard}>Archive board</button>}
                            <button className="btn btn-danger" onClick={() => navigate(`/board/${boardId}`)}>Close</button>
                        </div>
                    </div>}
            </div>
        </div>
    );
}

export default ManageBoard;