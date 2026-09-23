import { useParams, useNavigate, Link } from "react-router-dom";
import { useState, useRef, useEffect } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate";

import Toolbar from "./Toolbar";

const ViewTime = () => {
    const navigate = useNavigate();
    const axiosPrivate = useAxiosPrivate();
    const { userId } = useParams();

    const [errMsg, setErrMsg] = useState('');
    const [timeRows, setTimeRows] = useState([]);
    const [boards, setBoards] = useState([]);
    const [boardTasks, setBoardTasks] = useState([]);
    const [days, setDays] = useState(7);
    const [input, setInput] = useState(7);

    const errRef = useRef();

    const handleDaysChange = (e) => {
        if (Number.isNaN(parseInt(e.target.value))) {
            setInput(1);
            return;
        }
        if (parseInt(e.target.value) < 1) {
            setInput(1);
        } else if (parseInt(e.target.value) > 100) {
            setInput(100);
        } else {
            setInput(parseInt(e.target.value));
        }
    }

    const handleAccept = () => {
        setDays(input);
    }

    const isDateLaterThan = (date) => {
        const currentDate = new Date();
        currentDate.setHours(23, 59, 59);
        const targetDate = new Date(date + "T23:59:59");
        return (Math.floor((currentDate - targetDate) / (1000 * 60 * 60 * 24)) < days) && currentDate > targetDate;
    }

    const fetchTimeTable = async () => {
        try {
            const response = await axiosPrivate.get(`/tm/${userId}`);
            setTimeRows(response.data.sort((a, b) => {
                const dateA = new Date(a.dateTime);
                const dateB = new Date(b.dateTime);
                return dateB - dateA;
            }));
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get time table failed");
            }
            errRef.current.focus();
        }
    };

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

    const fetchBoardTasks = async (boardId) => {
        try {
            const response = await axiosPrivate.get(`/board/${boardId}/tasks`);
            const tasksArray = response.data;
            const updatedBoardTasks = transformTasksArray(tasksArray, boardId);
            setBoardTasks((prev) => [...prev, updatedBoardTasks]);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get tasks Failed");
            }
            errRef.current.focus();
        }
    };
    useEffect(() => {
        boards.map(board => fetchBoardTasks(board.id));
    }, [boards]);

    const transformTasksArray = (tasksArray, boardId) => {
        const tasksByBoard = { boardId, taskIds: [] };
        tasksArray.forEach((task) => {
            tasksByBoard.taskIds.push(task.id);
        });
        return tasksByBoard;
    };


    useEffect(() => {
        if (Number.isNaN(parseInt(userId))) {
            navigate('/missing');
        } else if (parseInt(userId) !== parseInt(JSON.parse(localStorage.getItem("user")).id)) {
            navigate('/unauthorized');
        } else {
            fetchTimeTable();
            fetchUserBoards();
        }
    }, [])

    const handleEdit = (timeId) => {
        navigate(`/timeManagement/edit/${timeId}`);
    }

    const handleAddTime = () => {
        navigate(`/timeManagement/new`);
    }

    return (
        <div className="ViewTime">
            <Toolbar />
            <h1>User time overview</h1>
            <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
            <div className="ViewTimePage">
                <label>Number of days shown:</label>
                <input
                    type="number"
                    value={input}
                    data-testid="DaysChangeInput"
                    min="1"
                    max="100"
                    onChange={handleDaysChange} />
                <button className="btn btn-success" onClick={handleAccept}>Accept</button>
                <button onClick={handleAddTime} className="AddTime-button btn btn-success">Add time</button>
                <table>
                    <thead>
                        <tr>
                            <th>Date and time spent</th>
                            <th>Task</th>
                            <th>Description</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {timeRows.map((timeRow) => (
                            isDateLaterThan(timeRow.dateTime.split(' ')[0]) &&
                            <tr key={timeRow.id}>
                                <td>
                                    {timeRow.dateTime}
                                </td>
                                <td>
                                    <Link to={`/board/${boardTasks.find(item => item.taskIds.includes(timeRow.taskId))?.boardId}/task/${timeRow.taskId}`}>{timeRow.taskId}</Link>
                                </td>
                                <td>
                                    {timeRow.description}
                                </td>
                                <td>
                                    <button className="btn btn-warning" onClick={(e) => handleEdit(timeRow.id)}>Edit</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default ViewTime;