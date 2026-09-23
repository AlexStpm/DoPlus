import { useEffect, useRef, useState } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import ReactMde from "react-mde";
import { useNavigate, useParams } from "react-router-dom";
import Showdown from "showdown";
import 'react-mde/lib/styles/css/react-mde-all.css';
import { BsFillTrashFill } from "react-icons/bs";

function ManageTask({ task, active, setActive }) {
    const axiosPrivate = useAxiosPrivate();
    const navigate = useNavigate();

    const [selectedTab, setSelectedTab] = useState("write")

    const [errMsg, setErrMsg] = useState('');
    const errRef = useRef();

    const [title, setTitle] = useState("");
    const [users, setUsers] = useState([]);
    const [assignee, setAssignee] = useState(NaN);
    const [reporter, setReporter] = useState(JSON.parse(localStorage.getItem("user")).id);
    const [priority, setPriority] = useState("");
    const [severity, setSeverity] = useState("");
    const [estimation, setEstimation] = useState(0);
    const [years, setYears] = useState(0);
    const [months, setMonths] = useState(0);
    const [days, setDays] = useState(3);
    const [hours, setHours] = useState(0);
    const [minutes, setMinutes] = useState(0);

    const [tags, setTags] = useState([]);
    const [selectedTag, setSelectedTag] = useState(-1);
    const [addedTags, setAddedTags] = useState([]);
    const [status, setStatus] = useState("Postponed");
    const [description, setDescription] = useState("");
    const [isLoaded, setIsLoaded] = useState(false);

    const { boardId } = useParams();

    const prioritys = ["Blocker", "Highest", "High", "Medium", "Low"];
    const severitys = ["Critical", "Major", "Moderate", "Low "];
    const statuses = ["Postponed", "To Do", "In progress", "Feedback", "Done"];

    const fetchTags = async () => {
        try {
            const response = await axiosPrivate.get(`/board/${boardId}/tags`);
            setTags(response.data);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get tags Failed");
            }
            errRef.current.focus();
        }
    };
    const fetchUsers = async () => {
        try {
            const responseOwner = await axiosPrivate.get(`/board/${boardId}/owner`);
            const responseManagers = await axiosPrivate.get(`/board/${boardId}/managers`);
            const responseEmloyees = await axiosPrivate.get(`/board/${boardId}/employees`);
            setUsers([responseOwner.data, ...responseManagers.data, ...responseEmloyees.data]);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get users Failed");
            }
            errRef.current?.focus();
        }
    };

    useEffect(() => {
        fetchTags();
        fetchUsers();
    }, []);

    useEffect(() => {
        if (task && users.length !== 0) {
            setTitle(task.title);
            setAssignee(users.find(user => user.username == task.assignee)?.id);
            setReporter(users.find(user => user.username == task.reporter)?.id);
            setPriority(task.priority);
            setSeverity(task.severity);
            setEstimation(task.estimation);
            setAddedTags(tags.filter(tag => task?.tags?.includes(tag.id)));

            setYears(parseInt(task.estimation / 535680));
            setMonths(parseInt(task.estimation / 44640)
                - parseInt(task.estimation / 535680) * 12);
            setDays(parseInt(task.estimation / 1440)
                - parseInt(task.estimation / 535680) * 12 * 31
                - (parseInt(task.estimation / 44640) - parseInt(task.estimation / 535680) * 12) * 31);
            setHours(parseInt(task.estimation / 60)
                - parseInt(task.estimation / 535680) * 12 * 31 * 24
                - (parseInt(task.estimation / 44640) - parseInt(task.estimation / 535680) * 12) * 31 * 24
                - (parseInt(task.estimation / 1440)
                    - parseInt(task.estimation / 535680) * 12 * 31
                    - (parseInt(task.estimation / 44640) - parseInt(task.estimation / 535680) * 12) * 31) * 24);
            setMinutes(parseInt(task.estimation)
                - parseInt(task.estimation / 535680) * 12 * 31 * 24 * 60
                - (parseInt(task.estimation / 44640) - parseInt(task.estimation / 535680) * 12) * 31 * 24 * 60
                - (parseInt(task.estimation / 1440)
                    - parseInt(task.estimation / 535680) * 12 * 31
                    - (parseInt(task.estimation / 44640) - parseInt(task.estimation / 535680) * 12) * 31) * 24 * 60
                - (parseInt(task.estimation / 60)
                    - parseInt(task.estimation / 535680) * 12 * 31 * 24
                    - (parseInt(task.estimation / 44640) - parseInt(task.estimation / 535680) * 12) * 31 * 24
                    - (parseInt(task.estimation / 1440)
                        - parseInt(task.estimation / 535680) * 12 * 31
                        - (parseInt(task.estimation / 44640) - parseInt(task.estimation / 535680) * 12) * 31) * 24) * 60);

            setStatus(task.status);
            setDescription(task.description);
            setIsLoaded(true);
        }

    }, [task, users])

    const converter = new Showdown.Converter({
        emoji: true,
        tables: true,
        simplifiedAutoLink: true,
        strikethrough: true,
        tasklists: true
    });

    const handleYearsChange = (years) => {
        if (Number.isNaN(parseInt(years))) {
            setYears(0);
            return;
        }
        if (parseInt(years) < 0) {
            setYears(0);
        } else if (parseInt(years) >= 100) {
            setYears(100);
            setMonths(0);
            setDays(0);
            setHours(0);
            setMinutes(0);
        } else {
            setYears(parseInt(years));
        }
    }

    const handleMonthsChange = (months) => {
        if (Number.isNaN(parseInt(months)) || years == 100) {
            setMonths(0);
            return;
        }
        if (parseInt(months) < 0) {
            setMonths(0);
        } else if (parseInt(months) >= 11) {
            setMonths(11);
        } else {
            setMonths(parseInt(months));
        }
    }

    const handleDaysChange = (days) => {
        if (Number.isNaN(parseInt(days)) || years == 100) {
            setDays(0);
            return;
        }
        if (parseInt(days) < 0) {
            setDays(0);
        } else if (parseInt(days) >= 30) {
            setDays(30);
        } else {
            setDays(parseInt(days));
        }
    }

    const handleHoursChange = (hours) => {
        if (Number.isNaN(parseInt(hours)) || years == 100) {
            setHours(0);
            return;
        }
        if (parseInt(hours) < 0) {
            setHours(0);
        } else if (parseInt(hours) >= 23) {
            setHours(23);
        } else {
            setHours(parseInt(hours));
        }
    }

    const handleMinutesChange = (minutes) => {
        if (Number.isNaN(parseInt(minutes)) || years == 100) {
            setMinutes(0);
            return;
        }
        if (parseInt(minutes) < 0) {
            setMinutes(0);
        } else if (parseInt(minutes) >= 59) {
            setMinutes(59);
        } else {
            setMinutes(parseInt(minutes));
        }
    }

    useEffect(() => {
        setEstimation(minutes + hours * 60 + days * 24 * 60 + months * 31 * 24 * 60 + years * 12 * 31 * 24 * 60);
    }, [years, months, days, hours, minutes]);

    useEffect(() => {
        setErrMsg("");
    }, [title, assignee, reporter, priority, severity, estimation, addedTags, status, description]);

    const handleAddTag = () => {
        setAddedTags(prev => {
            const tagToAdd = tags.find(tag => tag.id == selectedTag);
            const tagAlreadyAdded = prev.some(existingTag => existingTag.id == selectedTag);
            if (!tagAlreadyAdded && tagToAdd) {
                return [...prev, tagToAdd];
            }
            return prev;
        });
    }
    const handleRemoveTag = (tagId) => {
        setAddedTags(prev => prev.filter(tag => tag.id != tagId));
    }

    const handleCreateTask = async () => {
        if (!(description.length < 1440)) {
            setErrMsg("Description must be less than 1440 symbols");
            return;
        }
        const createTaskRequest = {
            "title": title,
            "assigneeId": Number.isNaN(parseInt(assignee)) ? null : parseInt(assignee),
            "reporterId": parseInt(reporter),
            "boardId": parseInt(boardId),
            "priority": priority,
            "severity": severity,
            "estimation": parseInt(estimation),
            "status": status,
            "description": description,
            "tagIds": addedTags.map(tag => parseInt(tag.id)),
            "creationTime": new Date().toString()
        }
        try {
            const response = await axiosPrivate.post(`/task/add`, createTaskRequest);
            if (!Number.isNaN(parseInt(assignee))) {
                const createNotificationRequest = {
                    "userId": parseInt(assignee),
                    "taskId": parseInt(response.data)
                }
                await axiosPrivate.post(`/notification/sent`, createNotificationRequest);
            }
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Create task Failed");
            }
            errRef.current.focus();
            return;
        }
        navigate(0);
    }
    const handleUpdateTask = async () => {
        if (!(description.length < 1440)) {
            setErrMsg("Description must be less than 1440 symbols");
            return;
        }
        const upadteTaskRequest = {
            "title": title,
            "assigneeId": Number.isNaN(parseInt(assignee)) ? null : parseInt(assignee),
            "reporterId": parseInt(reporter),
            "priority": priority,
            "severity": severity,
            "estimation": parseInt(estimation),
            "status": status,
            "description": description,
            "tags": addedTags.map(tag => parseInt(tag.id))
        }
        try {
            await axiosPrivate.put(`/task/${task.id}/update`, upadteTaskRequest);
            if (!Number.isNaN(parseInt(assignee))) {
                const createNotificationRequest = {
                    "userId": parseInt(assignee),
                    "taskId": parseInt(task.id)
                }
                await axiosPrivate.post(`/notification/sent`, createNotificationRequest);
            }
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else if (err.response?.status === 404) {
                setErrMsg("Task not found!(May be deleted)");
            } else {
                setErrMsg("Update task Failed");
            }
            errRef.current.focus();
            return;
        }
        navigate(0);
    }

    const handleDeleteTask = async () => {
        try {
            await axiosPrivate.delete(`/task/${task.id}/delete`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Delete task Failed");
            }
            errRef.current.focus();
            return;
        }
        navigate(`/board/${boardId}`);
    }

    return (
        <div className={active ? "ManageTask active" : "ManageTask"} onClick={() => setActive(false)}>
            <div className={active ? "ManageTaskContent active" : "ManageTaskContent"} onClick={e => e.stopPropagation()}>
                {task
                    ? <div className="ManageTaskHeader"><h1>Update task</h1> <button data-testid="DeleteButton" className="btn btn-danger" onClick={handleDeleteTask}><BsFillTrashFill className="TrashIcon" /></button> </div>
                    : <h1>Create new task</h1>}
                <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
                <div className="ManageTaskForm">
                    <div className={title ? "TaskOptionTitle notEmpty" : "TaskOptionTitle"}>
                        <input
                            className="TitleInput"
                            type="text"
                            data-testid="TitleInput"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                        />
                        <div className="underline"></div>
                        <label className="TitleLabel">Title</label>
                    </div>
                    <div className="TaskOption">
                        <label>Assignee:</label>
                        {isLoaded
                            ? <select data-testid="AssigneeSelect" defaultValue={assignee} onChange={(e) => setAssignee(e.target.value)}>
                                {assignee
                                    ? <option value={assignee}>{users?.find(user => user.id == assignee)?.username}</option>
                                    : <option value="" style={{ color: "gray" }}>NONE</option>}
                                {users.map((user, index) => assignee != user.id && <option key={index} value={user.id}>{user.username}</option>)}
                                {assignee && <option value="" style={{ color: "gray" }}>NONE</option>}
                            </select>
                            : <select data-testid="AssigneeSelect" defaultValue="" onChange={(e) => setAssignee(e.target.value)}>
                                <option value="" style={{ color: "gray" }}>NONE</option>
                                {users.map((user, index) => <option key={index} value={user.id}>{user.username}</option>)}
                            </select>}
                    </div>
                    <div className="TaskOption">
                        <label>Reporter:</label>
                        {isLoaded
                            ? <select data-testid="ReporterSelect" defaultValue={reporter} onChange={(e) => setReporter(e.target.value)}>
                                {<option value={reporter}>{users?.find(user => user.id == reporter)?.username}</option>}
                                {users.map((user, index) => reporter != user.id && <option key={index} value={user.id}>{user.username}</option>)}
                            </select>
                            : <select data-testid="ReporterSelect" defaultValue={JSON.parse(localStorage.getItem("user")).id} onChange={(e) => setReporter(e.target.value)}>
                                {<option value={JSON.parse(localStorage.getItem("user")).id}>{users?.find(user => user.id == JSON.parse(localStorage.getItem("user")).id)?.username}</option>}
                                {users.map((user, index) => JSON.parse(localStorage.getItem("user")).id != user.id && <option key={index} value={user.id}>{user.username}</option>)}
                            </select>}
                    </div>
                    <div className="TaskOption">
                        <label>Priority:</label>
                        {isLoaded
                            ? <select data-testid="PrioritySelect" defaultValue={priority} onChange={(e) => setPriority(e.target.value)}>
                                {priority
                                    ? <option value={priority}>{priority}</option>
                                    : <option value="" style={{ color: "gray" }}>NONE</option>
                                }
                                {prioritys.map((item, index) => item != priority && <option key={index} value={item}>{item}</option>)}
                                {priority && <option value="" style={{ color: "gray" }}>NONE</option>}
                            </select>
                            : <select data-testid="PrioritySelect" defaultValue="" onChange={(e) => setPriority(e.target.value)}>
                                <option value="" style={{ color: "gray" }}>NONE</option>
                                {prioritys.map((priority, index) => <option key={index} value={priority}>{priority}</option>)}
                            </select>}
                    </div>
                    <div className="TaskOption">
                        <label>Severity:</label>
                        {isLoaded
                            ? <select data-testid="SeveritySelect" defaultValue={severity} onChange={(e) => setSeverity(e.target.value)}>
                                {severity
                                    ? <option value={severity}>{severity}</option>
                                    : <option value="" style={{ color: "gray" }}>NONE</option>
                                }
                                {severitys.map((item, index) => item != severity && <option key={index} value={item}>{item}</option>)}
                                {severity && <option value="" style={{ color: "gray" }}>NONE</option>}
                            </select>
                            : <select data-testid="SeveritySelect" defaultValue="" onChange={(e) => setSeverity(e.target.value)}>
                                <option value="" style={{ color: "gray" }}>NONE</option>
                                {severitys.map((severity, index) => <option key={index}>{severity}</option>)}
                            </select>}
                    </div>
                    <div className="TaskOption">
                        <label>Estimation:</label>
                        <div className="EstimationProperties">
                            <label>years:</label>
                            <input
                                type="number"
                                data-testid="EstimationYearsInput"
                                value={years}
                                min="0"
                                max="100"
                                onChange={(e) => handleYearsChange(e.target.value)} />
                            <label>months:</label>
                            <input
                                type="number"
                                data-testid="EstimationMonthsInput"
                                value={months}
                                min="0"
                                max="11"
                                onChange={(e) => handleMonthsChange(e.target.value)} />
                            <label>days:</label>
                            <input
                                type="number"
                                data-testid="EstimationDaysInput"
                                value={days}
                                min="0"
                                max="30"
                                onChange={(e) => handleDaysChange(e.target.value)} />
                            <label>hours:</label>
                            <input
                                type="number"
                                data-testid="EstimationHoursInput"
                                value={hours}
                                min="0"
                                max="23"
                                onChange={(e) => handleHoursChange(e.target.value)} />
                            <label>minutes:</label>
                            <input
                                type="number"
                                data-testid="EstimationMinutesInput"
                                value={minutes}
                                min="0"
                                max="59"
                                onChange={(e) => handleMinutesChange(e.target.value)} />
                        </div>
                    </div>
                    <div className="TaskOption">
                        <label>Tags:</label>
                        <select data-testid="TagsSelect" defaultValue="select tag" onChange={(e) => setSelectedTag(e.target.value)}>
                            <option value="select tag" disabled>select tag</option>
                            {tags.filter(tag => tag.isActive).map(tag => <option key={tag.id} value={tag.id}>{tag.name.slice(0, 20)}</option>)}
                        </select>
                        <button className="btn btn-success AddTagButton" onClick={handleAddTag}>Add</button>
                    </div>
                    <div className="TagList">
                        {addedTags.map(tag => <div className="Tag" data-testid="SelectedTag" key={tag.id} onClick={() => handleRemoveTag(tag.id)}>{tag.name} </div>)}
                    </div>
                    <div className="TaskOption">
                        <label>Status:</label>
                        {isLoaded
                            ? <select data-testid="StatusSelect" defaultValue={status} onChange={(e) => setStatus(e.target.value)}>
                                {<option value={status}>{status}</option>}
                                {statuses.map((item, index) => item != status && <option key={index} value={item}>{item}</option>)}
                            </select>
                            : <select data-testid="StatusSelect" defaultValue={statuses[0]} onChange={(e) => setStatus(e.target.value)}>
                                {statuses.map((status, index) => <option key={index}>{status}</option>)}
                            </select>}
                    </div>
                    <label>Description:</label>
                    <ReactMde
                        value={description}
                        onChange={setDescription}
                        selectedTab={selectedTab}
                        onTabChange={setSelectedTab}
                        toolbarCommands={[["header", "bold", "italic", "strikethrough"], ["link", "quote", "code", "image"], ["unordered-list", "ordered-list"]]}
                        generateMarkdownPreview={(markdown) =>
                            Promise.resolve(converter.makeHtml(markdown))
                        }
                        maxEditorHeight={30}
                        minPreviewHeight={20}
                        heightUnits="vh"
                    />
                    {task
                        ? <div className="AcceptButton"><button className="btn btn-success" onClick={handleUpdateTask}>Accept</button></div>
                        : <div className="AcceptButton"><button className="btn btn-success" onClick={handleCreateTask}>Accept</button></div>}
                </div>
            </div>
        </div>
    );
}

export default ManageTask;