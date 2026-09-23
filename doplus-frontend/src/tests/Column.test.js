import React from 'react';
import { render, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import Column from '../components/Column';


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

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: jest.fn(),
    useLocation: jest.fn(),
}));


jest.mock('../components/TaskCard', () => (props) => <div data-testid="mocked-task-card">
    <h3>{props.task.title}</h3>
    <h3>{props.task.priority}</h3>
</div>);

describe('Column component', () => {
    test('renders without crashing', () => {
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        const mockTask = [{
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
          }];
          render(<Column title="titleBig" tasks={mockTask}/>);
          expect(screen.getByText("titleBig")).toBeInTheDocument();
          expect(screen.getByText(mockTask[0].priority)).toBeInTheDocument();
          expect(screen.getByText(mockTask[0].title)).toBeInTheDocument();

    });

});
