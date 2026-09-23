import React from 'react';
import { render, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import TaskCard from '../components/TaskCard';

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: jest.fn()
}));

jest.mock('react-beautiful-dnd', () => ({
    Droppable: ({ children }) =>
      children(
        {
          draggableProps: {
            style: {},
          },
          innerRef: jest.fn(),
        },
        {}
      ),
    Draggable: ({ children }) =>
      children(
        {
          draggableProps: {
            style: {},
          },
          innerRef: jest.fn(),
        },
        {}
      ),
  }));

describe('TaskCard component', () => {
    test('renders without crashing', async () => {
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
                1,
                2
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }

        render(<TaskCard task={mockTask} />)

        expect(screen.getByText('Task title')).toBeInTheDocument();
        expect(screen.getByText('assignee: employee1')).toBeInTheDocument();
        expect(screen.getByText('Medium')).toBeInTheDocument();
        expect(screen.getByText('#1')).toBeInTheDocument();
    });

    test('when click on Task card go to Task page', async () => {
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
                1,
                2
            ],
            "creationTime": "2024-02-09T14:07:20.475Z",
            "boardId": 1
        }

        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        render(<TaskCard task={mockTask} boardId={1} />)

        fireEvent.click(screen.getByTestId('cardClick'));

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/board/1/task/1');
        });
    });
});
