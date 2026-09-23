import { useEffect, useState } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate";

const BoardInfo = ({boardId, active, setActive }) => {

    const axiosPrivate = useAxiosPrivate();

    const [owner, setOwner] = useState({});
    const [managers, setManagers] = useState([]);

    const fetchInfo = async () => {
        try {
            const ownerResponse = await axiosPrivate.get(`/board/${boardId}/owner`);
            setOwner(ownerResponse.data);
            const managersResponse = await axiosPrivate.get(`/board/${boardId}/managers`);
            setManagers(managersResponse.data);
        } catch (err) {
            console.error(err);
        }
    };
    useEffect(() => {
        fetchInfo();
    }, []);


    return (
        <div data-testid="BoardInfo" className={active ? "BoardInfo active" : "BoardInfo"} onClick={() => setActive(false)}>
            <div className={active ? "BoardInfoContent active" : "BoardInfoContent"} onClick={e => e.stopPropagation()}>
                <h2>Board owner: {owner.name} ({owner.username})</h2>
                <h3>Managers:</h3>
                {managers.map(manager => <p key={manager.id}>{manager.name} ({manager.username})</p>)}
            </div>
        </div>
    );
}

export default BoardInfo;