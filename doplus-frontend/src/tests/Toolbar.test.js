import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import Toolbar from '../components/Toolbar';
import { axiosPrivate } from "../api/axios";


jest.mock('../components/BoardInfo', () => () => <div data-testid="mocked-boardinfo">Mocked BoardInfo</div>);
jest.mock('../components/ManageTask', () => () => <div data-testid="mocked-managetask">Mocked ManageTask</div>);
jest.mock('../components/NotificationsList', () => () => <div data-testid="mocked-notificationlist">Mocked NotificationsList</div>);
jest.mock('../api/axios', () => ({
    axiosPrivate: {
        get: jest.fn(),
        post: jest.fn(),
        put: jest.fn(),
        delete: jest.fn(),
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
    useLocation: jest.fn(),
}));

describe('Toolbar component', () => {
    let mockUser, mockNotifications, mockBoards;
    beforeEach(() => {
        Router.useParams.mockReturnValue({ boardId: '1' });

        mockUser = { id: 1, name: 'name', username: 'username' };
        jest.spyOn(Storage.prototype, 'getItem');
        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        Router.useLocation.mockReturnValue({
            pathname: "/board/task/",
        });

        mockNotifications = [
            {
                "id": 1,
                "taskId": 1,
                "seen": false
            },
            {
                "id": 2,
                "taskId": 2,
                "seen": true
            }
        ];

        mockBoards = [
            {
                "id": 1,
                "name": "Board name 1",
                "isArchived": false
            },
            {
                "id": 2,
                "name": "Board name 2",
                "isArchived": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === `/notification/1/all`) {
                return {
                    data: mockNotifications,
                };
            } else if (url === `/user/getuserboards/1`) {
                return {
                    data: mockBoards,
                };
            }
            return {
                data: "someData",
            };
        })
    });

    test('renders without crashing', async () => {

        await act(async () => {
            render(<Toolbar />);
        });
        await waitFor(() => {
            expect(screen.getByText(`Create Board`)).toBeInTheDocument();
            expect(screen.getByText(`Choose board`)).toBeInTheDocument();
            expect(screen.getByText(`Edit Board`)).toBeInTheDocument();
            expect(screen.getByText(`Log Time`)).toBeInTheDocument();
            expect(screen.getByText(`Add Task`)).toBeInTheDocument();
            expect(screen.getByText(`Notifications`)).toBeInTheDocument();
            expect(screen.getByText(`@${mockUser.username}`)).toBeInTheDocument();
            expect(screen.getByText(`Log Out`)).toBeInTheDocument();
        });
    });

    test('test log out button', async () => {

        jest.spyOn(Storage.prototype, 'clear');
        Storage.prototype.clear = jest.fn();

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<Toolbar />);
        });

        fireEvent.click(screen.getByText('Log Out'));

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/login', { replace: true });
        });
    });

    test('test Create Board button', async () => {

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<Toolbar />);
        });

        fireEvent.click(screen.getByText('Create Board'));

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/createBoard');
        });
    });

    test('test Edit Board button', async () => {

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<Toolbar />);
        });

        fireEvent.click(screen.getByText('Edit Board'));

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/editBoard/1');
        });
    });

    test('test Change Board', async () => {

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<Toolbar />);
        });

        fireEvent.change(screen.getByTestId('Board-select'), { target: { value: "2" } });

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/board/2');
        });
    });

    test('test Log Time button', async () => {

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<Toolbar />);
        });

        fireEvent.click(screen.getByText('Log Time'));

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/timeManagement/1');
        });
    });

    test('test Filters', async () => {
        var severityFilter;
        const setSeverityFilter = (newValue) => { severityFilter = newValue };

        var estimationFilter;
        const setEstimationFilter = (newValue) => { estimationFilter = newValue };

        var creationTimeFilter;
        const setCreationTimeFilter = (newValue) => { creationTimeFilter = newValue };

        var tagFilter;
        const setTagFilter = (newValue) => { tagFilter = newValue };

        const mockTags = [
            {
                "id": 1,
                "name": "ENV",
                "isActive": true
            },
            {
                "id": 2,
                "name": "DRAFT",
                "isActive": false
            }
        ];

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        await act(async () => {
            render(<Toolbar
                showFilters={true}
                severityFilter={severityFilter}
                setSeverityFilter={setSeverityFilter}
                estimationFilter={estimationFilter}
                setEstimationFilter={setEstimationFilter}
                creationTimeFilter={creationTimeFilter}
                setCreationTimeFilter={setCreationTimeFilter}
                tags={mockTags}
                tagFilter={tagFilter}
                setTagFilter={setTagFilter} />);
        });
        
        fireEvent.change(screen.getByTestId('severityFilter'), { target: { value: "Critical" } });
        await waitFor(() => {
            expect(severityFilter).toBe("Critical");
            expect(tagFilter).toBe(-1);
            expect(creationTimeFilter).toBe(-1);
            expect(estimationFilter).toBe(-1);
        });

        fireEvent.change(screen.getByTestId('tagFilter'), { target: { value: 1 } });
        await waitFor(() => {
            expect(tagFilter).toBe("1");
            expect(severityFilter).toBe("");
            expect(creationTimeFilter).toBe(-1);
            expect(estimationFilter).toBe(-1);
        });

        fireEvent.change(screen.getByTestId('creationTimeFilter'), { target: { value: 24 } });
        await waitFor(() => {
            expect(creationTimeFilter).toBe("24");
            expect(severityFilter).toBe("");
            expect(tagFilter).toBe(-1);
            expect(estimationFilter).toBe(-1);
        });

        fireEvent.change(screen.getByTestId('estimationFilter'), { target: { value: 60 } });
        await waitFor(() => {
            expect(estimationFilter).toBe("60");
            expect(severityFilter).toBe("");
            expect(tagFilter).toBe(-1);
            expect(creationTimeFilter).toBe(-1);
        });
    });
});