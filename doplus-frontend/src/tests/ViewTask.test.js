import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import ViewTask from '../components/ViewTask';
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
jest.mock('../components/ManageTask', () => () => <div data-testid="mocked-ManageTask">Mocked Toolbar</div>);

describe('ViewTask component', () => {

    beforeEach(() => {
        Router.useParams.mockReturnValue({ taskId: '1', boardId: '1' });

        const mockUser = { id: 1, name: 'user', username: 'user' };
        jest.spyOn(Storage.prototype, 'getItem');
        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));
    });

    test('renders without crashing', async () => {

        await act(async () => {
            render(<ViewTask />);
        })
        expect(screen.getByText("Priority:")).toBeInTheDocument();
        expect(screen.getByText("Severity:")).toBeInTheDocument();
        expect(screen.getByText("Assignee:")).toBeInTheDocument();
        expect(screen.getByText("Reporter:")).toBeInTheDocument();
        expect(screen.getByText("Estimation:")).toBeInTheDocument();
        expect(screen.getByText("Status:")).toBeInTheDocument();
        expect(screen.getByText("Tags:")).toBeInTheDocument();
        expect(screen.getByText("ID:")).toBeInTheDocument();
        expect(screen.getByText("Description:")).toBeInTheDocument();
    });

    test('try to load tags when internal server error', async () => {
        
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/board/1/tags') {
                throw { response: { status: 500 } };
            } else if (url === '/board/1/info') {
                return { data: "Board info" }
            } else if (url === '/task/getbyid/1') {
                return { data: "task" };
            }
        })

        await act(async () => {
            render(<ViewTask />);
        })
        expect(screen.getByText("Get tags Failed")).toBeInTheDocument();
    });

    test('try to load board info when internal server error', async () => {
        
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/board/1/tags') {
                return { data: [{ id: 1, name: "tag" }] }
            } else if (url === '/board/1/info') {
                throw { response: { status: 500 } };
            } else if (url === '/task/getbyid/1') {
                return { data: "task" };
            }
        })

        await act(async () => {
            render(<ViewTask />);
        })
        expect(screen.getByText("Get board info failed")).toBeInTheDocument();
    });
    
    test('try to load task when internal server error', async () => {
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/board/1/tags') {
                return { data: [{ id: 1, name: "tag" }] }
            } else if (url === '/board/1/info') {
                return { data: 'boardinfo' }
            } else if (url === '/task/getbyid/1') {
                throw { response: { status: 500 } };
            }
        })

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<ViewTask />);
        })
        
        expect(screen.getByText("Get task Failed")).toBeInTheDocument();
    });

    test('try to load non existing task', async () => {
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/board/1/tags') {
                return { data: [{ id: 1, name: "tag" }] }
            } else if (url === '/board/1/info') {
                return { data: 'boardinfo' }
            } else if (url === '/task/getbyid/1') {
                throw { response: { status: 404 } };
            }
        })

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<ViewTask />);
        })

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/missing');
        });
    });

    test('successfully load task', async () => {
        const mockTask = {
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
          }
        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/board/1/tags') {
                return { data: [{ id: 1, name: "tag" }] }
            } else if (url === '/board/1/info') {
                return { data: 'boardinfo' }
            } else if (url === '/task/getbyid/1') {
                return { data: mockTask };
            }
        })

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<ViewTask />);
        })

        await waitFor(() => {
            expect(screen.getByText(mockTask.title)).toBeInTheDocument();
            expect(screen.getByText(mockTask.assignee)).toBeInTheDocument();
            expect(screen.getByText(mockTask.reporter)).toBeInTheDocument();
            expect(screen.getByText(mockTask.priority)).toBeInTheDocument();
            expect(screen.getByText(mockTask.severity)).toBeInTheDocument();
            expect(screen.getByText(mockTask.id)).toBeInTheDocument();
            expect(screen.getByText(mockTask.status)).toBeInTheDocument();
        });
    });
});