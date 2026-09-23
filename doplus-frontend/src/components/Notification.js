import React, { useEffect, useState } from 'react';
import useAxiosPrivate from "../hooks/useAxiosPrivate"
import { Link } from 'react-router-dom';

const Notification = ({ notification, setNotifications }) => {
    const axiosPrivate = useAxiosPrivate();
    const [task, setTask] = useState({});

    const handleSetSeen = async (notificationId) => {
        try {
            await axiosPrivate.put(`/notification/${notificationId}/setseen`)
            setNotifications(prev =>
                prev.map(notification => notification.id === notificationId ? { ...notification, seen: true } : notification));
        }
        catch (err) {
            alert("This notification is deleted! Please refresh the page.")
        }
    }

    const fetchTask = async (taskId) => {
        try {
            const response = await axiosPrivate.get(`/task/getbyid/${taskId}`)
            setTask(response.data);
        }
        catch (err) {
            console.error(err);
        }
    }
    useEffect(() => {
        fetchTask(notification.taskId);
    }, [])

    return (
        <div className={notification.seen ? "Notification" : "Notification New"}>
            <p>You have been assigned a task</p>
            <Link to={`/board/${task.boardId}/task/${notification.taskId}`}>Link to task {notification.taskId}</Link>
            {!notification.seen && <button className="btn btn-secondary" onClick={e => handleSetSeen(notification.id)}>Set seen</button>}
        </div>
    );
}

export default Notification;