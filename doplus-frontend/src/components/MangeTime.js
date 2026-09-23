import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import Toolbar from './Toolbar';
import DateTimePicker from 'react-datetime-picker';

import 'react-datetime-picker/dist/DateTimePicker.css';
import 'react-calendar/dist/Calendar.css';
import 'react-clock/dist/Clock.css';
import useAxiosPrivate from '../hooks/useAxiosPrivate';

const MangeTime = (props) => {
    const navigate = useNavigate();
    const location = useLocation();
    const axiosPrivate = useAxiosPrivate();
    const { timeId } = useParams();
    const [dateTime, setDateTime] = useState(new Date(new Date().setHours(0, 0, 0, 0)));
    const [dateTimeString, setDateTimeString] = useState("");
    const [boards, setBoards] = useState([]);
    const [tasks, setTasks] = useState([]);
    const [selectedTaskId, setSelectedTaskId] = useState(-1);
    const [description, setDescription] = useState("");
    const [errMsg, setErrMsg] = useState('');

    const errRef = useRef();

    const fetchUserBoards = async () => {
        try {
            const response = await axiosPrivate.get(`/user/getuserboards/${JSON.parse(localStorage.getItem("user")).id}`);
            setBoards(response.data);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get boards Failed");
            }
            errRef.current.focus();
        }
    };

    const fetchTimeRow = async () => {
        try {
            const response = await axiosPrivate.get(`/tm/${JSON.parse(localStorage.getItem("user")).id}`);
            setDescription(response.data.find(row => row.id == timeId).description);
            setSelectedTaskId(response.data.find(row => row.id == timeId).taskId);
            setDateTimeString(response.data.find(row => row.id == timeId).dateTime);
            setDateTime(new Date(response.data.find(row => row.id == timeId).dateTime));
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get time table failed");
            }
            errRef.current.focus();
        }
    }

    const fetchBoardTasks = async (boardId) => {
        try {
            const response = await axiosPrivate.get(`/board/${boardId}/tasks`);
            setTasks(prev => [...prev, ...response.data]);
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
        setDateTimeString(dateTime?.getFullYear() + "-"
            + ("0" + (dateTime?.getMonth() + 1)).slice(-2) + "-"
            + ("0" + dateTime?.getDate()).slice(-2) + " "
            + ("0" + dateTime?.getHours()).slice(-2) + ":"
            + ("0" + dateTime?.getMinutes()).slice(-2));
    }, [dateTime]);

    useEffect(() => {
        fetchUserBoards();
    }, []);
    useEffect(() => {
        if (timeId) {
            fetchTimeRow();
        }
    }, [timeId]);

    useEffect(() => {
        boards.map(board => fetchBoardTasks(board.id));
    }, [boards]);

    useEffect(() => {
        setErrMsg("");
    }, [dateTimeString, selectedTaskId, description]);


    const handleUpdateTm = async () => {
        if (selectedTaskId === -1) {
            setErrMsg("Task is not selected!");
            errRef.current.focus();
            return;
        }

        if (description.length > 140) {
            setErrMsg("Description must be less than 140 characters long!");
            errRef.current.focus();
            return;
        }

        const updateTmRequest = {
            "dateTime": dateTimeString,
            "taskId": selectedTaskId,
            "description": description
        }
        try {
            await axiosPrivate.put(`/tm/${timeId}/update`, updateTmRequest);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else if (err.response?.status === 400) {
                setErrMsg("Total logged time exceeds 24 hours for the day");
            } else {
                setErrMsg("Update tm failed!");
            }
            errRef.current.focus();
            return;
        }
        navigate(`/timeManagement/${JSON.parse(localStorage.getItem("user")).id}`);
    }
    const handleSaveTm = async () => {
        if (selectedTaskId === -1) {
            setErrMsg("Task is not selected!");
            errRef.current.focus();
            return;
        }

        if (description.length > 140) {
            setErrMsg("Description must be less than 140 characters long!");
            errRef.current.focus();
            return;
        }

        const createTmRequest = {
            "userId": JSON.parse(localStorage.getItem("user")).id,
            "dateTime": dateTimeString,
            "taskId": selectedTaskId,
            "description": description
        }
        try {
            await axiosPrivate.post(`/tm/add`, createTmRequest);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else if (err.response?.status === 400) {
                setErrMsg("Total logged time exceeds 24 hours for the day");
            } else {
                setErrMsg("Create tm failed!");
            }
            errRef.current.focus();
            return;
        }
        navigate(`/timeManagement/${JSON.parse(localStorage.getItem("user")).id}`);
    }

    const handleRemoveTm = async () => {
        try {
            await axiosPrivate.delete(`/tm/${timeId}/delete`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Remove tm failed!");
            }
            errRef.current.focus();
            return;
        }
        navigate(`/timeManagement/${JSON.parse(localStorage.getItem("user")).id}`);
    }

    return (
        <div className="ManageTime">
            <Toolbar />
            <h1>Manage time record </h1>
            <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
            <div className="ManageTimePage">
                <div className="TimeOption">
                    <label className="Date">Date:</label>
                    <label className="TimePeriod">Time spent:</label>
                    <DateTimePicker
                        value={dateTime}
                        onChange={setDateTime}
                        format="y-MM-dd HH:mm"
                        maxDetail="minute"
                        calendarIcon={false}
                        showLeadingZeros={true}
                        minDate={new Date(Date.parse("1960-01-01 00:00"))}
                        maxDate={new Date(Date.parse("2222-12-31 00:00"))} />
                </div>
                <div className="TimeOption">
                    <label>Task:</label>
                    {selectedTaskId !== -1
                        ? <select defaultValue={selectedTaskId} onChange={(e) => setSelectedTaskId(e.target.value)}>
                            <option value={selectedTaskId}>(#{tasks.find(task => task.id == selectedTaskId)?.id}) {tasks.find(task => task.id == selectedTaskId)?.title.slice(0, 20)}</option>
                            {tasks.map(task => task.id != selectedTaskId && <option key={task.id} value={task.id}>(#{task.id}) {task.title.slice(0, 20)}</option>)}
                        </select>
                        : <select defaultValue={-1} onChange={(e) => setSelectedTaskId(e.target.value)}>
                            <option value={-1} disabled>select task</option>
                            {tasks.map(task => <option key={task.id} value={task.id}>(#{task.id}) {task.title.slice(0, 20)}</option>)}
                        </select>}
                </div>
                <label>Description:</label>
                <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                />
                <div className="Actions">
                    {location.pathname.includes("edit")
                        ? <div className="EditTimeButtons">
                            <button className="SaveButton btn btn-success" onClick={handleUpdateTm}>Save</button>
                            <button className="btn btn-danger" onClick={handleRemoveTm}>Remove</button>
                        </div>
                        : <button className="SaveButton btn btn-success" onClick={handleSaveTm}>Save</button>}
                </div>
            </div>
        </div>
    );
}

export default MangeTime;