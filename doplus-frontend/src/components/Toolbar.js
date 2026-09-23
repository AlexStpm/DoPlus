import { useEffect, useState } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate"
import { useLocation, useNavigate, useParams } from "react-router-dom";
import BoardInfo from "./BoardInfo";
import ManageTask from "./ManageTask";
import NotificationsList from "./NotificationsList";

const Toolbar = (props) => {
    const navigate = useNavigate();
    const { boardId } = useParams();
    const axiosPrivate = useAxiosPrivate();
    const [boards, setBoards] = useState([]);
    const [notifications, setNotifications] = useState([]);
    const [isNewNotificationsExist, setIsNewNotificationsExist] = useState(false);
    const [employees, setEmployees] = useState([]);
    const [isCurrentUserEmployee, setIsCurrentUserEmployee] = useState(true);
    const [isEmployeeLoaded, setIsEmployeeLoaded] = useState(false);

    const [boardInfoActive, setBoardInfoActive] = useState(false);
    const [manageTaskActive, setManageTaskActive] = useState(false);
    const [notificationsListActive, setNotificationsListActive] = useState(false);

    const currentUserId = JSON.parse(localStorage.getItem("user")).id;

    const location = useLocation();


    const fetchUserBoards = async () => {
        try {
            if (JSON.parse(localStorage.getItem("user")).username !== "rootadmin") {
                const response = await axiosPrivate.get(`/user/getuserboards/${JSON.parse(localStorage.getItem("user")).id}`);
                setBoards(response.data);
            }
        } catch (err) {
            console.error(err);
        }
    };
    const fetchUserNotifications = async () => {
        try {
            if (JSON.parse(localStorage.getItem("user")).username !== "rootadmin") {
                const response = await axiosPrivate.get(`/notification/${JSON.parse(localStorage.getItem("user")).id}/all`);
                setNotifications(response.data);
            }
        } catch (err) {
            console.error(err);
        }
    };

    const fetchEmployees = async () => {
        try {
            const employeesResponse = await axiosPrivate.get(`/board/${boardId}/employees`);
            setEmployees(employeesResponse.data);
            setIsEmployeeLoaded(true);
        } catch (err) {
            console.error(err);
        }
    };

    useEffect(() => {
        setIsNewNotificationsExist(false);
        notifications?.slice().reverse().slice(0, 10).some(notification => {
            if (!notification.seen) {
                setIsNewNotificationsExist(true);
                return true;
            }
            return false;
        });
    }, [notifications]);

    useEffect(() => {
        fetchUserBoards();
        fetchUserNotifications();
    }, []);

    useEffect(() => {
        if (boardId) {
            fetchEmployees();
        }
    }, [boardId]);

    useEffect(() => {
        for (let i = 0; i < employees.length; i++) {
            if (employees[i].id == currentUserId) {
                return;
            }
            if (i === employees.length - 1) {
                setIsCurrentUserEmployee(false);
            }
        }
        if (isEmployeeLoaded && employees.length === 0) {
            setIsCurrentUserEmployee(false);
        }
    }, [employees])

    const handleUnauth = () => {
        alert("Access denied!!!")
    }


    const handleLogOut = () => {
        axiosPrivate.post('/logout');
        localStorage.clear();
        navigate('/login', { replace: true });
    }

    const handleCreateBoard = () => {
        navigate('/createBoard');
    }

    const handleManageBoard = () => {
        navigate(`/editBoard/${boardId}`);
    }

    const handleBoardChange = (boardId) => {
        navigate(`/board/${boardId}`);
        navigate(0);
    }

    const handlelogTime = () => {
        navigate(`/timeManagement/${JSON.parse(localStorage.getItem("user")).id}`);
    }

    const handleSeverityFilterChange = (value) => {
        props.setEstimationFilter(-1)
        props.setCreationTimeFilter(-1)
        props.setTagFilter(-1)
        props.setSeverityFilter(value)
    }

    const handleEstimationFilterChange = (value) => {
        props.setSeverityFilter("")
        props.setCreationTimeFilter(-1)
        props.setTagFilter(-1)
        props.setEstimationFilter(value)
    }

    const handleCreationTimeFilterChange = (value) => {
        props.setSeverityFilter("")
        props.setEstimationFilter(-1)
        props.setTagFilter(-1)
        props.setCreationTimeFilter(value)
    }

    const handleTagFilterChange = (value) => {
        props.setSeverityFilter("")
        props.setEstimationFilter(-1)
        props.setCreationTimeFilter(-1)
        props.setTagFilter(value)
    }

    return (
        <header>
            {!props.isAdmin ? <button className="createBoard-button cool" onClick={handleCreateBoard} > Create Board</button>
                : <button className="createBoard-button cool" onClick={handleUnauth}> Create Board</button>}
            {(!props.isAdmin && boardId && !isCurrentUserEmployee && !boards.find(board => boardId == board.id)?.isArchived) ? <button className="editBoard-button  cool" onClick={handleManageBoard} > Edit Board</button>
                : <button className="editBoard-button  cool" onClick={handleUnauth}> Edit Board</button>}
            {(!props.isAdmin && boards !== undefined && boards.length !== 0) ?
                <select
                    className="Board-select"
                    defaultValue="Choose board"
                    data-testid="Board-select"
                    onChange={(e) => handleBoardChange(e.target.value)}>
                    <option value="Choose board" disabled>Choose board</option>
                    {boards.map((board) => <option key={board.id} value={board.id}>
                        {board.name.slice(0, 20)}
                    </option>)}
                </select>
                : <select
                    className="Board-select"
                    defaultValue="Choose board">
                    <option value="Choose board" disabled>Choose board</option>
                </select>}
            {(!props.isAdmin && boardId) ? <button className="boardInfo-button  cool" onClick={() => setBoardInfoActive(true)} > Info</button>
                : <button className="boardInfo-button  cool" onClick={handleUnauth}> Info</button>}
            {!props.isAdmin ? <button className="logTime-button  cool" onClick={handlelogTime} > Log Time</button> : <button className="logTime-button  cool" onClick={handleUnauth}> Log Time</button>}
            {!props.isAdmin && props.showFilters && boardId &&
                <select
                    className="tagFilter"
                    data-testid="tagFilter"
                    value={props.tagFilter}
                    onChange={(e) => handleTagFilterChange(e.target.value)}
                >
                    <option value={-1} >Tag</option>
                    {props.tags.map(tag => <option key={tag.id} value={tag.id}>{tag.name.slice(0, 20)}</option>)}

                </select>}
            {!props.isAdmin && props.showFilters && boardId &&
                <select
                    className="creationTimeFilter"
                    data-testid="creationTimeFilter"
                    value={props.creationTimeFilter}
                    onChange={(e) => handleCreationTimeFilterChange(e.target.value)}
                >
                    <option value={-1} >Creation time</option>
                    <option value={24} >today</option>
                    <option value={168} >this week</option>
                    <option value={744} >this month</option>
                    <option value={8760} >this year</option>

                </select>}
            {!props.isAdmin && props.showFilters && boardId &&
                <select
                    className="estimationFilter"
                    data-testid="estimationFilter"
                    value={props.estimationFilter}
                    onChange={(e) => handleEstimationFilterChange(e.target.value)}
                >
                    <option value={-1} >Estimation</option>
                    <option value={60} >less than hour</option>
                    <option value={1440} >less than day</option>
                    <option value={10080} >less than week</option>
                    <option value={44640} >less than month</option>
                    <option value={525600} >less than year</option>

                </select>}
            {!props.isAdmin && props.showFilters && boardId &&
                <select
                    className="severityFilter"
                    data-testid="severityFilter"
                    value={props.severityFilter}
                    onChange={(e) => handleSeverityFilterChange(e.target.value)}
                >
                    <option value="" >Severty</option>
                    <option value="Critical" >Critical</option>
                    <option value="Major" >Major</option>
                    <option value="Moderate" >Moderate</option>
                    <option value="Low" >Low</option>

                </select>}
            {(!props.isAdmin && location.pathname.includes('/board/') && !location.pathname.includes('/task/') && !boards.find(board => boardId == board.id)?.isArchived) ? <button className="AddTask-button  cool" onClick={() => setManageTaskActive(prev => !prev)} > Add Task</button>
                : <button className="AddTask-button  cool" onClick={handleUnauth}> Add Task</button>}
            <div className="notificationGroup">
                {!props.isAdmin ? <button className="notifications-button  cool" onClick={() => setNotificationsListActive(true)}> Notifications</button>
                    : <button className="notifications-button  cool" onClick={handleUnauth}> Notifications</button>}
                {!props.isAdmin && isNewNotificationsExist && <div className="notificationIcon"></div>}
            </div>
            <div className="logInfo">
                <div className="logUsername">@{JSON.parse(localStorage.getItem("user")).username}</div>
                <button className="logout-button  cool" onClick={handleLogOut} > Log Out</button>
            </div>
            {!props.isAdmin && boardId && <BoardInfo boardId={boardId} active={boardInfoActive} setActive={setBoardInfoActive} />}
            {!props.isAdmin && <NotificationsList notifications={notifications} active={notificationsListActive} setActive={setNotificationsListActive} setNotifications={setNotifications} />}
            {!props.isAdmin && boardId && location.pathname.includes('/board/') && !location.pathname.includes('/task/') && !boards.find(board => boardId == board.id)?.isArchived && <ManageTask active={manageTaskActive} setActive={setManageTaskActive} />}
        </header>
    );
}

export default Toolbar;