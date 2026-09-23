import React from 'react';
import { act, render, fireEvent, waitFor, screen } from '@testing-library/react';
import Notification from '../components/Notification';
import { axiosPrivate } from "../api/axios";
import { BrowserRouter as Router } from 'react-router-dom';
import { MemoryRouter } from 'react-router-dom';


jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: jest.fn(),
  useParams: jest.fn(),
}));


jest.mock('../api/axios', () => ({
    axiosPrivate: {
        get: jest.fn(),
        put: jest.fn(),
        interceptors: {
            request: {
                use: jest.fn(),
                eject: jest.fn(),
            },
            response: {
                use: jest.fn(),
                eject: jest.fn(),
            },
        },
    },
}));

const mockNotification = {
    id: 1,
    taskId: 2,
    seen: false,
};
const mockTask = {
    boardId: 3,
    taskId: 2,
};

describe('Notification component', () => {
    const initialNotifications = [
        { id: 1, taskId: 2, seen: false },
        { id: 2, taskId: 3, seen: false }
    ];

    const setNotifications = jest.fn();

    let lastSetStateCall;
    const setNotificationsMock = jest.fn(updateFn => {
        lastSetStateCall = updateFn(initialNotifications);
    });

    afterEach(() => {
        jest.clearAllMocks();
    });


    test('renders correctly based on notification seen status', async () => {
        axiosPrivate.get.mockResolvedValueOnce({ data: mockTask });
        const unseenNotification = { ...mockNotification, seen: false };
        const { getByText, getByRole, queryByRole, rerender } = render(
            <MemoryRouter>
                <Notification notification={unseenNotification} setNotifications={setNotifications} />
            </MemoryRouter>
        );

        expect(getByText('You have been assigned a task')).toBeInTheDocument();
        
        await waitFor(() => {
            const link = getByRole('link');
            expect(link).toHaveAttribute('href', `/board/${mockTask.boardId}/task/${mockNotification.taskId}`);
        });

        expect(getByRole('button', { name: 'Set seen' })).toBeInTheDocument();

        const seenNotification = { ...mockNotification, seen: true };
        rerender(
            <MemoryRouter>
                <Notification notification={seenNotification} setNotifications={setNotifications} />
            </MemoryRouter>
        );

        expect(queryByRole('button', { name: 'Set seen' })).toBeNull();
    });


    test('calls handleSetSeen and updates notification on click', async () => {
        const setNotifications = jest.fn();
        axiosPrivate.get.mockResolvedValueOnce({ data: mockTask });
        const { getByText } = render(
            <Router>
                <Notification notification={mockNotification} setNotifications={setNotifications} />
            </Router>
        );
    
        await waitFor(() => {
            const setSeenButton = getByText('Set seen');
            fireEvent.click(setSeenButton);
        });
    
        await waitFor(() => {
            expect(axiosPrivate.put).toHaveBeenCalledWith(`/notification/${mockNotification.id}/setseen`);
        });
    
        expect(setNotifications).toHaveBeenCalled();
    });
    
    test('fetches task data on component mount', async () => {
        const setNotifications = jest.fn();
        axiosPrivate.get.mockResolvedValueOnce({ data: mockTask });
        const { getByText } = render(
            <Router>
                <Notification notification={mockNotification} setNotifications={setNotifications} />
            </Router>
        );
    
        await waitFor(() => {
            expect(axiosPrivate.get).toHaveBeenCalledWith(`/task/getbyid/${mockNotification.taskId}`);
        });
    
        await waitFor(() => {
            const taskLink = getByText(`Link to task ${mockNotification.taskId}`);
            expect(taskLink).toHaveAttribute('href', `/board/${mockTask.boardId}/task/${mockNotification.taskId}`);
        });
    });

    test('logs error when fetching task fails', async () => {
        const setNotifications = jest.fn();

        axiosPrivate.get.mockRejectedValueOnce(new Error('Async error fetching task'));

        const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

        await act(async () => {
            render(
                <MemoryRouter>
                    <Notification notification={mockNotification} setNotifications={setNotifications} />
                </MemoryRouter>
            );
        });

        expect(consoleSpy).toHaveBeenCalled();

        consoleSpy.mockRestore();
    });

    test('displays alert when setting notification as seen fails', async () => {
        axiosPrivate.get.mockResolvedValueOnce({ data: mockTask });
        
        axiosPrivate.put.mockRejectedValueOnce(new Error('Error setting notification as seen'));
    
        const mockSetNotifications = jest.fn();
    
        global.alert = jest.fn();
    
        await act(async () => {
            render(
                <MemoryRouter>
                    <Notification notification={mockNotification} setNotifications={mockSetNotifications} />
                </MemoryRouter>
            );
        });
    
        fireEvent.click(screen.getByText('Set seen'));
    
        await waitFor(() => expect(global.alert).toHaveBeenCalledWith("This notification is deleted! Please refresh the page."));
    
        jest.clearAllMocks();
    });
    

    test('updates notification as seen on successful set seen operation', async () => {
        axiosPrivate.get.mockResolvedValueOnce({ data: mockTask });
        
        await act(async () => {
            render(
                <MemoryRouter>
                    <Notification notification={initialNotifications[0]} setNotifications={setNotificationsMock} />
                </MemoryRouter>
            );
        });
    
        await act(async () => {
            fireEvent.click(screen.getByText('Set seen'));
        });
    
        expect(axiosPrivate.put).toHaveBeenCalled();
        expect(setNotificationsMock).toHaveBeenCalled();
    
        await waitFor(() => {
            const newState = setNotificationsMock.mock.calls[0][0](initialNotifications);
    
            expect(newState).toEqual(expect.arrayContaining([
                expect.objectContaining({ id: 1, seen: true }),
                expect.objectContaining({ id: 2, seen: false })
            ]));
        });
    });

});
