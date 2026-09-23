import { Link } from "react-router-dom"

const Unauthorized = () => {
    return (
        <article style={{ padding: "100px" }}>
            <h1>Oops!</h1>
            <p>You are unauthorized to see that!</p>
            <Link to={`/board`}>To the main page</Link>
        </article>
    )
}

export default Unauthorized