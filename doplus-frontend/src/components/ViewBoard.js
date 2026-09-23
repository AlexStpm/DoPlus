import useAxiosPrivate from "../hooks/useAxiosPrivate"
import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import Toolbar from "./Toolbar";
import Column from "./Column";
import { DragDropContext } from "react-beautiful-dnd";


const ViewBoard = () => {
    const navigate = useNavigate();
    const axiosPrivate = useAxiosPrivate();
    const { boardId } = useParams();
    const [tasks, setTasks] = useState([]);
    const [tags, setTags] = useState([]);

    const [errMsg, setErrMsg] = useState('');
    const [boardInfo, setBoardInfo] = useState({});


    const [taskId, setTaskId] = useState(-1);
    const [status, setStatus] = useState("");

    const [severityFilter, setSeverityFilter] = useState("");
    const [estimationFilter, setEstimationFilter] = useState(-1);
    const [creationTimeFilter, setCreationTimeFilter] = useState(-1);
    const [tagFilter, setTagFilter] = useState(-1);

    const isInitialMount = useRef(true);

    const errRef = useRef();

    const fetchBoardTasks = async () => {
        try {
            const response = await axiosPrivate.get(`/board/${boardId}/tasks`);
            setTasks(response.data);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Get tasks Failed");
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
                setErrMsg("Get board info Failed");
            }
            errRef.current.focus();
        }
    };

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

    useEffect(() => {
        if (isInitialMount.current) {
            isInitialMount.current = false;
        } else {
            changestatus(taskId, status);
        }

    }, [taskId, status]);


    const changestatus = async (taskId, status) => {
        try {
            await axiosPrivate.put(`/task/${taskId}/changestatus/${status}`);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No Server Response");
            } else {
                setErrMsg("Change task status failed");
            }
            errRef.current.focus();
        }
    };

    const isUserOnBoard = async () => {
        try {
            if (JSON.parse(localStorage.getItem("user")).username !== "rootadmin") {
                const response = await axiosPrivate.get(`/user/getuserboards/${JSON.parse(localStorage.getItem("user")).id}`);
                if(!Number.isNaN(parseInt(boardId)) && (!(response.data.some(board => board.id === parseInt(boardId))))){
                    navigate("/unauthorized");
                }
            }
        } catch (err) {
            navigate("/unauthorized");
        }
    };

    useEffect(() => {
        isUserOnBoard();
        if (!Number.isNaN(parseInt(boardId))) {
            fetchBoardTasks();
            fetchBoardInfo();
            fetchTags();
        }
    }, [boardId])

    const onDragEnd = (result) => {
        const { destination, source, draggableId } = result;
        if (!destination) {
            return;
        }
        if (destination.droppableId === source.droppableId && destination.index === source.index) {
            return;
        }
        setTasks(prevTasks => {
            return prevTasks.map(task => {
                if (task.id == draggableId) {
                    return { ...task, status: destination.droppableId };
                }
                return task;
            });
        });
        setTaskId(draggableId);
        setStatus(destination.droppableId);
    }

    return (
        <div className="BoardView">
            <Toolbar
                showFilters={true}
                severityFilter={severityFilter}
                setSeverityFilter={setSeverityFilter}
                estimationFilter={estimationFilter}
                setEstimationFilter={setEstimationFilter}
                creationTimeFilter={creationTimeFilter}
                setCreationTimeFilter={setCreationTimeFilter}
                tags={tags}
                tagFilter={tagFilter}
                setTagFilter={setTagFilter}
            />
            {boardInfo && <h1>{boardInfo.name}</h1>}
            <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
            {!Number.isNaN(parseInt(boardId))
                ? <div className="Board">
                    <DragDropContext
                        onDragEnd={onDragEnd}>
                        <Column
                            boardId={boardId}
                            title="Postponed"
                            tasks={tasks.filter(item => item.status === "Postponed" &&
                                (severityFilter === "" || item.severity === severityFilter) &&
                                (estimationFilter < 0 || item.estimation < estimationFilter) &&
                                (creationTimeFilter < 0 || Math.floor((new Date() - new Date(item.creationTime)) / (1000 * 60 * 60)) < creationTimeFilter) &&
                                (tagFilter < 0 || item.tags.includes(parseInt(tagFilter))))}
                            isArchived={boardInfo.isArchived} />
                        <Column
                            boardId={boardId}
                            title="To Do"
                            tasks={tasks.filter(item => item.status === "To Do" &&
                                (severityFilter === "" || item.severity === severityFilter) &&
                                (estimationFilter < 0 || item.estimation < estimationFilter) &&
                                (creationTimeFilter < 0 || Math.floor((new Date() - new Date(item.creationTime)) / (1000 * 60 * 60)) < creationTimeFilter) &&
                                (tagFilter < 0 || item.tags.includes(parseInt(tagFilter))))}
                            isArchived={boardInfo.isArchived} />
                        <Column
                            boardId={boardId}
                            title="In progress"
                            tasks={tasks.filter(item => item.status === "In progress" &&
                                (severityFilter === "" || item.severity === severityFilter) &&
                                (estimationFilter < 0 || item.estimation < estimationFilter) &&
                                (creationTimeFilter < 0 || Math.floor((new Date() - new Date(item.creationTime)) / (1000 * 60 * 60)) < creationTimeFilter) &&
                                (tagFilter < 0 || item.tags.includes(parseInt(tagFilter))))}
                            isArchived={boardInfo.isArchived} />
                        <Column
                            boardId={boardId}
                            title="Feedback"
                            tasks={tasks.filter(item => item.status === "Feedback" &&
                                (severityFilter === "" || item.severity === severityFilter) &&
                                (estimationFilter < 0 || item.estimation < estimationFilter) &&
                                (creationTimeFilter < 0 || Math.floor((new Date() - new Date(item.creationTime)) / (1000 * 60 * 60)) < creationTimeFilter) &&
                                (tagFilter < 0 || item.tags.includes(parseInt(tagFilter))))}
                            isArchived={boardInfo.isArchived} />
                        <Column
                            boardId={boardId}
                            title="Done"
                            tasks={tasks.filter(item => item.status === "Done" &&
                                (severityFilter === "" || item.severity === severityFilter) &&
                                (estimationFilter < 0 || item.estimation < estimationFilter) &&
                                (creationTimeFilter < 0 || Math.floor((new Date() - new Date(item.creationTime)) / (1000 * 60 * 60)) < creationTimeFilter) &&
                                (tagFilter < 0 || item.tags.includes(parseInt(tagFilter))))}
                            isArchived={boardInfo.isArchived} />
                    </DragDropContext>
                </div>
                : <h1> Choose board or create new one. </h1>}
        </div>
    )
}

export default ViewBoard