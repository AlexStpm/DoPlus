import { useParams, useNavigate } from "react-router-dom";
import Toolbar from "./Toolbar";
import { useEffect, useRef, useState } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import MarkdownView from "react-showdown";
import ManageTask from "./ManageTask";

const ViewTask = (props) => {
    const navigate = useNavigate();
    const { taskId, boardId } = useParams();
    const [boardInfo, setBoardInfo] = useState({});
    const [task, setTask] = useState({});
    const [tags, setTags] = useState([]);
    const axiosPrivate = useAxiosPrivate();
    const [years, setYears] = useState(0);
    const [months, setMonths] = useState(0);
    const [days, setDays] = useState(0);
    const [hours, setHours] = useState(0);
    const [minutes, setMinutes] = useState(0);
    const [addedTags, setAddedTags] = useState([]);
    const [manageTaskActive, setManageTaskActive] = useState(false);

    const [errMsg, setErrMsg] = useState('');
    const errRef = useRef();


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

    const fetchBoardInfo = async () => {
        try {
            const response = await axiosPrivate.get(`/board/${boardId}/info`);
            setBoardInfo(response.data);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get board info failed");
            }
            errRef.current.focus();
        }
    }

    const fetchTask = async () => {
        try {
            const response = await axiosPrivate.get(`/task/getbyid/${taskId}`);
            setTask(response.data);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
                errRef.current?.focus();
            } else if (err.response?.status === 404) {
                navigate("/missing");
            } else {
                setErrMsg("Get task Failed");
                errRef.current?.focus();
            }
        }
    }
    useEffect(() => {
        fetchTags();
        fetchTask();
        fetchBoardInfo();
    }, [taskId, boardId]);


    useEffect(() => {
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
    }, [task]);

    useEffect(() => {
        setAddedTags(tags?.filter(tag => task?.tags?.includes(tag.id)));
    }, [tags]);


    return (
        <div className="ViewTask">
            <Toolbar />
            <h1>Task overview</h1>
            <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
            <div className="ViewTaskPage">
                {!boardInfo.isArchived && <div className="UpdateButton"><button className="btn btn-warning" onClick={() => setManageTaskActive(prev => !prev)}>Update</button></div>}
                <div className="ViewTaskInfo">
                    <h2 style={{ color: task.title ? "black" : "grey", maxWidth: "50%" }}
                    >{task.title ? task.title : "Untitled"}</h2>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Priority:
                        </div>
                        <div className={`Priority ${task.priority}`}>
                            {task.priority}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Severity:
                        </div>
                        <div className={`Severity ${task.severity}`}>
                            {task.severity}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Assignee:
                        </div>
                        <div className="">
                            {task.assignee}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Reporter:
                        </div>
                        <div className="">
                            {task.reporter}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Estimation:
                        </div>
                        <div className="Estimation">
                            {years !== 0 && (years === 1 ? <div>{years} year </div> : <div> {years} years </div>)}
                            {months !== 0 && (months === 1 ? <div>{months} month</div> : <div> {months} months</div>)}
                            {days !== 0 && (days === 1 ? <div>{days} day</div> : <div> {days} days</div>)}
                            {hours !== 0 && (hours === 1 ? <div>{hours} hour</div> : <div> {hours} hours</div>)}
                            {minutes !== 0 && (minutes === 1 ? <div>{minutes} minute</div> : <div> {minutes} minutes</div>)}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Status:
                        </div>
                        <div className="">
                            {task.status}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Tags:
                        </div>
                        <div className="TagList">
                            {addedTags.map((tag, index) => <div key={index} className="Tag">{tag.name}</div>)}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            ID:
                        </div>
                        <div className="">
                            {task.id}
                        </div>
                    </div>
                    <div className="ViewTaskOption">
                        <div className="ViewTaskOptionLabel">
                            Description:
                        </div>
                    </div>
                    <div className="ViewTaskDescription">
                        <MarkdownView
                            markdown={task.description}
                            options={{ emoji: true, strikethrough: true, tables: true, openLinksInNewWindow: true, literalMidWordUnderscores: true }}
                        />
                    </div>

                </div>
                {!boardInfo.isArchived && <ManageTask task={task} active={manageTaskActive} setActive={setManageTaskActive} />}
            </div>
        </div>
    );
}

export default ViewTask;