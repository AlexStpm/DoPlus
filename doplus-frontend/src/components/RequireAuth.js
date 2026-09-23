import { useLocation, Navigate, Outlet } from "react-router-dom";

const RequireAuth = (props) => {
    const location = useLocation();
    const user = JSON.parse(localStorage.getItem('user'));

    return (
        props.allowedUser === undefined
            ? user
                ? user?.username !== "rootadmin"
                    ? <Outlet />
                    : <Navigate to="/unauthorized" state={{ from: location }} replace />
                : <Navigate to="/login" state={{ from: location }} replace />
            : user?.username === props.allowedUser
                ? <Outlet />
                : user
                    ? <Navigate to="/unauthorized" state={{ from: location }} replace />
                    : <Navigate to="/login" state={{ from: location }} replace />
    );
}

export default RequireAuth;