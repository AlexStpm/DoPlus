import React from 'react';
import TaskCard from './TaskCard';
import { Droppable } from 'react-beautiful-dnd';


const Column = (props) => {
    const title = props.title;
    const tasks = props.tasks;
    const boardId = props.boardId;
    const isArchived = props.isArchived;

    return (
        <div className='Column'>
            <h3>{title}</h3>
            <Droppable droppableId={props.title}>
                {(provided, snapshot) => (
                    <div className={`TaskList-${snapshot.isDraggingOver}`}
                        ref={provided.innerRef}
                        {...provided.droppableProps}>
                        {tasks.map((task, index) => <TaskCard boardId={boardId} key={task.id} task={task} index={index} isArchived={isArchived} />)}
                        {provided.placeholder}
                    </div>
                )}
            </Droppable>
        </div>
    );
}

export default Column;