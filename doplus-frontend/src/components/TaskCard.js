import React, { useEffect, useState } from 'react';
import { Draggable } from 'react-beautiful-dnd';
import { useNavigate } from 'react-router-dom';

const TaskCard = (props) => {
    const task = props.task;
    const isArchived = props.isArchived;
    const boardId = props.boardId;

    const navigate = useNavigate();
    const [years, setYears] = useState(0);
    const [months, setMonths] = useState(0);
    const [days, setDays] = useState(0);
    const [hours, setHours] = useState(0);
    const [minutes, setMinutes] = useState(0);

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
    }, []);
    const handleClick = () => {
        navigate(`/board/${boardId}/task/${task.id}`)
    }
    return (
        <Draggable draggableId={task.id.toString()} index={props.index} isDragDisabled={isArchived}>
            {(provided, snapshot) => (
                <div className={`TaskCard-${snapshot.isDragging}`}
                    {...provided.draggableProps}
                    {...provided.dragHandleProps}
                    ref={provided.innerRef}
                    data-testid = 'cardClick'
                    onClick={handleClick}>
                    <h4
                        style={{ color: task.title ? "rgb(66, 66, 66)" : "grey" }}
                    >{task.title ? task.title : "Untitled"}</h4>
                    {task.assignee && <p>assignee: {task.assignee}</p>}
                    {task.priority && <div className={`Priority ${task.priority}`}>
                        {task.priority}
                        <span className="PriorityTooltipText"> priority </span>
                    </div>}
                    <div className="Estimation">
                        {years !== 0 && (years === 1 ? <div>{years} year </div> : <div> {years} years </div>)}
                        {months !== 0 && (months === 1 ? <div>{months} month</div> : <div> {months} months</div>)}
                        {days !== 0 && (days === 1 ? <div>{days} day</div> : <div> {days} days</div>)}
                        {hours !== 0 && (hours === 1 ? <div>{hours} hour</div> : <div> {hours} hours</div>)}
                        {minutes !== 0 && (minutes === 1 ? <div>{minutes} minute</div> : <div> {minutes} minutes</div>)}
                        <span className="EstimationTooltipText"> estimation </span>
                    </div>
                    <p className="TaskId">#{task.id}</p>
                </div>
            )}
        </Draggable>
    );
}

export default TaskCard;
