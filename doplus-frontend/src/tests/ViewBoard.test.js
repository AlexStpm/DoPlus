import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import ViewBoard from '../components/ViewBoard';
import { axiosPrivate } from "../api/axios";

jest.mock('../api/axios', () => ({
    axiosPrivate: {
        get: jest.fn(),
        interceptors: {
            request: {
                use: jest.fn(),
                eject: jest.fn(),
            },
        },
    },
}));

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: jest.fn(),
    useParams: jest.fn(),
}));

jest.mock('../components/Toolbar', () => () => <div data-testid="mocked-toolbar">Mocked Toolbar</div>);
jest.mock('../components/Column', () => (props) => <div data-testid="mocked-column">
    <h3>{props.title}</h3>
    {props.tasks.map((task) => <p key={task.id}>{task.title}</p>)}
</div>);

describe('ViewBoard component', () => {

    beforeEach(() => {
        Router.useParams.mockReturnValue({ boardId: '1' });

        const mockUser = { id: 1, name: 'user', username: 'user' };
        jest.spyOn(Storage.prototype, 'getItem');
        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));
    });

    test('renders without crashing', async () => {
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("Postponed")).toBeInTheDocument();
        expect(screen.getByText("To Do")).toBeInTheDocument();
        expect(screen.getByText("In progress")).toBeInTheDocument();
        expect(screen.getByText("Feedback")).toBeInTheDocument();
        expect(screen.getByText("Done")).toBeInTheDocument();
    });

    test('try to load tasks when internal server error', async () => {
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                throw { response: { status: 500 } };
            } else if (url === '/board/1/info') {
                return { data: "boardinfo" };
            } else if (url === '/board/1/tags') {
                return { data: "tags" };
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("Get tasks Failed")).toBeInTheDocument();

    });

    test('try to load tasks when server is down', async () => {
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                throw new Error();
            } else if (url === '/board/1/info') {
                return { data: "boardinfo" };
            } else if (url === '/board/1/tags') {
                return { data: "tags" };
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("No Server Response")).toBeInTheDocument();

    });

    test('try to board info when internal server error', async () => {
        const mockTasks = [{
            "id": 1,
            "title": "Task title",
            "assignee": "employee1",
            "reporter": "employee2",
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 2,
            "status": "To Do",
            "description": "Description",
            "tags": [
                1
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }]
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                return { data: mockTasks };
            } else if (url === '/board/1/info') {
                throw { response: { status: 500 } };
            } else if (url === '/board/1/tags') {
                return { data: "tags" };
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("Get board info Failed")).toBeInTheDocument();
    });

    test('try to board info when server is down', async () => {
        const mockTasks = [{
            "id": 1,
            "title": "Task title",
            "assignee": "employee1",
            "reporter": "employee2",
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 2,
            "status": "To Do",
            "description": "Description",
            "tags": [
                1
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }]
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                return { data: mockTasks };
            } else if (url === '/board/1/info') {
                throw new Error();
            } else if (url === '/board/1/tags') {
                return { data: "tags" };
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("No Server Response")).toBeInTheDocument();
    });

    test('try to board tags when internal server error', async () => {
        const mockTasks = [{
            "id": 1,
            "title": "Task title",
            "assignee": "employee1",
            "reporter": "employee2",
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 2,
            "status": "To Do",
            "description": "Description",
            "tags": [
                1
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }]
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                return { data: mockTasks };
            } else if (url === '/board/1/info') {
                return { data: "board info" };
            } else if (url === '/board/1/tags') {
                throw { response: { status: 500 } };
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("Get tags failed")).toBeInTheDocument();
    });

    test('try to board tags when server is down', async () => {
        const mockTasks = [{
            "id": 1,
            "title": "Task title",
            "assignee": "employee1",
            "reporter": "employee2",
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 2,
            "status": "To Do",
            "description": "Description",
            "tags": [
                1
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }]
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                return { data: mockTasks };
            } else if (url === '/board/1/info') {
                return { data: "board info" };
            } else if (url === '/board/1/tags') {
                throw new Error();
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("No Server Response")).toBeInTheDocument();
    });

    test('successfully loaded tasks', async () => {
        const mockTasks = [{
            "id": 1,
            "title": "Task title",
            "assignee": "employee1",
            "reporter": "employee2",
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 2,
            "status": "To Do",
            "description": "Description",
            "tags": [
                1
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }]
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 1, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                return { data: mockTasks };
            } else if (url === '/board/1/info') {
                return { data: "board info" };
            } else if (url === '/board/1/tags') {
                return { data: "tags" };
            }
        });
        await act(async () => {
            render(<ViewBoard />);
        })
        expect(screen.getByText("Task title")).toBeInTheDocument();
    });

    test('try to load when user without role tries to log', async () => {
        const mockTasks = [{
            "id": 1,
            "title": "Task title",
            "assignee": "employee1",
            "reporter": "employee2",
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 2,
            "status": "To Do",
            "description": "Description",
            "tags": [
                1
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }]
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                return { data: [{ id: 2, name: "board" }] };
            } else if (url === '/board/1/tasks') {
                return { data: mockTasks };
            } else if (url === '/board/1/info') {
                return { data: "board info" };
            } else if (url === '/board/1/tags') {
                return { data: "tags" };
            }
        });
        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<ViewBoard />);
        })

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/unauthorized');
        });
    });

    test('try to load when unauthorized user tries to log', async () => {
        const mockTasks = [{
            "id": 1,
            "title": "Task title",
            "assignee": "employee1",
            "reporter": "employee2",
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 2,
            "status": "To Do",
            "description": "Description",
            "tags": [
                1
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }]
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/user/getuserboards/1') {
                throw new Error();
            } else if (url === '/board/1/tasks') {
                return { data: mockTasks };
            } else if (url === '/board/1/info') {
                return { data: "board info" };
            } else if (url === '/board/1/tags') {
                return { data: "tags" };
            }
        });
        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<ViewBoard />);
        })

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/unauthorized');
        });
    });

});