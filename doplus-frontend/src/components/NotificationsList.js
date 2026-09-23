import React from 'react';
import Notification from './Notification';


const NotificationsList = ({setNotifications, notifications, active, setActive }) => {
    return (
        <div className={active ? "NotificationsList active" : "NotificationsList"} onClick={() => setActive(false)}>
            <div className={active ? "NotificationsListContent active" : "NotificationsListContent"} onClick={e => e.stopPropagation()}>
                {notifications?.toReversed().slice(0,10).map(notification => <Notification key={notification.id} notification={notification} setNotifications={setNotifications}/>)}
            </div>
        </div>
    );
}

export default NotificationsList;