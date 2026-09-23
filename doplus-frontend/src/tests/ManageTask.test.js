import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import ManageTask from '../components/ManageTask';
import { axiosPrivate } from "../api/axios";

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: jest.fn(),
  useParams: jest.fn(),
}));


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

describe('ManageTask component', () => {
  let mockOwner, mockEmployees, mockManagers;
  beforeEach(() => {
    Router.useParams.mockReturnValue({ boardId: '1' });

    mockOwner = { id: 1, name: 'owner', username: 'owner' };

    mockEmployees = [{ id: 2, name: 'employee1', username: 'employee1' },
    { id: 3, name: 'employee2', username: 'employee2' }];

    mockManagers = [{ id: 4, name: 'manager1', username: 'manager1' },
    { id: 5, name: 'manager2', username: 'manager2' }];

    axiosPrivate.get.mockImplementation((url) => {
      if (url === `/board/1/owner`) {
        return {
          data: mockOwner,
        };
      } else if (url === `/board/1/managers`) {
        return {
          data: mockEmployees,
        };
      } else if (url === `/board/1/employees`) {
        return {
          data: mockManagers,
        };
      } else if (url === `/board/1/tags`) {
        return {
          data: [{ id: 1, name: "tag1" }, { id: 2, name: "tag2" }, { id: 3, name: "tag3" },],
        };
      }
      return {
        data: "someData",
      };
    })

    const mockUser = { id: 1, name: 'owner', username: 'owner' };

    jest.spyOn(Storage.prototype, 'getItem');

    Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));
  });

  test('renders without crashing', async () => {

    await act(async () => {
      render(<ManageTask active={true} />);
    })

    expect(screen.getByRole('button', { name: 'Accept' })).toBeInTheDocument();
    expect(screen.getByTestId('TitleInput')).toBeInTheDocument();
    expect(screen.getByTestId('AssigneeSelect')).toBeInTheDocument();
    expect(screen.getByTestId('ReporterSelect')).toBeInTheDocument();
    expect(screen.getByTestId('PrioritySelect')).toBeInTheDocument();
    expect(screen.getByTestId('SeveritySelect')).toBeInTheDocument();
    expect(screen.getByTestId('EstimationYearsInput')).toBeInTheDocument();
    expect(screen.getByTestId('EstimationMonthsInput')).toBeInTheDocument();
    expect(screen.getByTestId('EstimationDaysInput')).toBeInTheDocument();
    expect(screen.getByTestId('EstimationHoursInput')).toBeInTheDocument();
    expect(screen.getByTestId('EstimationMinutesInput')).toBeInTheDocument();
    expect(screen.getByTestId('TagsSelect')).toBeInTheDocument();
    expect(screen.getByTestId('StatusSelect')).toBeInTheDocument();

  });

  test('create task when description is more than 1440 symbols', async () => {

    await act(async () => {
      render(<ManageTask active={true} />);
    })

    fireEvent.change(screen.getByTestId('text-area'), { target: { value: "One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin. He lay on his armour - like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections.The bedding was hardly able to cover it and seemed ready to slide off any moment.His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked.\"What's happened to me?\" he thought. It wasn't a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls. A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame. It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer. Gregor then turned to look out the window at the dull weather. Drops of rain could be heard hitting the pane, which made him feel quite sad. \"How about if I sleep a little bit longer and forget all this nonsense\", he thought, but that was something he was unable to do because he was used to sleeping on his right, and in his present state couldn't get into that position. However hard he threw himself onto his right, he always r" } });
    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(screen.getByText('Description must be less than 1440 symbols')).toBeInTheDocument();
    });
  });



  test('given task properties are loaded in component', async () => {
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
    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    await waitFor(() => {
      expect(screen.getByTestId('TitleInput').value).toBe("Task title");
      expect(screen.getByTestId('AssigneeSelect').value).toBe("2");
      expect(screen.getByTestId('ReporterSelect').value).toBe("3");
      expect(screen.getByTestId('PrioritySelect').value).toBe("Medium");
      expect(screen.getByTestId('SeveritySelect').value).toBe("Moderate");
      expect(screen.getByTestId('StatusSelect').value).toBe("To Do");
      expect(screen.getByTestId('EstimationYearsInput').value).toBe("0");
      expect(screen.getByTestId('EstimationMonthsInput').value).toBe("0");
      expect(screen.getByTestId('EstimationDaysInput').value).toBe("0");
      expect(screen.getByTestId('EstimationHoursInput').value).toBe("0");
      expect(screen.getByTestId('EstimationMinutesInput').value).toBe("2");
      expect(screen.getAllByTestId('SelectedTag').length).toBe(2);
      expect(screen.getByTestId('text-area').value).toBe("Description");
    });

  });

  test('update task when description is more than 1440 symbols', async () => {

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
    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.change(screen.getByTestId('text-area'), { target: { value: "One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin. He lay on his armour - like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections.The bedding was hardly able to cover it and seemed ready to slide off any moment.His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked.\"What's happened to me?\" he thought. It wasn't a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls. A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame. It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer. Gregor then turned to look out the window at the dull weather. Drops of rain could be heard hitting the pane, which made him feel quite sad. \"How about if I sleep a little bit longer and forget all this nonsense\", he thought, but that was something he was unable to do because he was used to sleeping on his right, and in his present state couldn't get into that position. However hard he threw himself onto his right, he always r" } });
    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(screen.getByText('Description must be less than 1440 symbols')).toBeInTheDocument();
    });
  });

  test('successful task creation', async () => {

    axiosPrivate.post.mockImplementation((url, data) => {
      if (url === `/task/add`) {
        return {
          data: 1,
        };
      }
      return {
        data: "someData",
      };
    })
    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);
    await act(async () => {
      render(<ManageTask active={true} />);
    })

    fireEvent.change(screen.getByTestId('TitleInput'), { target: { value: "Task title" } });
    fireEvent.change(screen.getByTestId('AssigneeSelect'), { target: { value: "2" } });
    fireEvent.change(screen.getByTestId('ReporterSelect'), { target: { value: "3" } });
    fireEvent.change(screen.getByTestId('PrioritySelect'), { target: { value: "Medium" } });
    fireEvent.change(screen.getByTestId('SeveritySelect'), { target: { value: "Moderate" } });
    fireEvent.change(screen.getByTestId('StatusSelect'), { target: { value: "To Do" } });
    fireEvent.change(screen.getByTestId('EstimationYearsInput'), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId('EstimationMonthsInput'), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId('EstimationDaysInput'), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId('EstimationHoursInput'), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId('EstimationMinutesInput'), { target: { value: "1" } });
    fireEvent.change(screen.getByTestId('TagsSelect'), { target: { value: "1" } });
    fireEvent.click(screen.getByText('Add'));
    fireEvent.change(screen.getByTestId('TagsSelect'), { target: { value: "2" } });
    fireEvent.click(screen.getByText('Add'));
    fireEvent.change(screen.getByTestId('TagsSelect'), { target: { value: "1" } });
    fireEvent.click(screen.getByText('Add'));
    fireEvent.change(screen.getByTestId('text-area'), { target: { value: "Description" } });
    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(0);
    });
  });

  test('successful task update', async () => {

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
    axiosPrivate.put.mockImplementation((url, data) => {
      if (url === `/task/${mockTask.id}/update`) {
        return {
          data: 1,
        };
      }
      return {
        data: "someData",
      };
    })
    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);

    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.change(screen.getByTestId('TitleInput'), { target: { value: "Updated title" } });
    fireEvent.change(screen.getByTestId('AssigneeSelect'), { target: { value: "3" } });
    fireEvent.change(screen.getByTestId('ReporterSelect'), { target: { value: "2" } });
    fireEvent.change(screen.getByTestId('PrioritySelect'), { target: { value: "Blocker" } });
    fireEvent.change(screen.getByTestId('SeveritySelect'), { target: { value: "Critical" } });
    fireEvent.change(screen.getByTestId('StatusSelect'), { target: { value: "Postponed" } });
    fireEvent.change(screen.getByTestId('EstimationYearsInput'), { target: { value: "0" } });
    fireEvent.change(screen.getByTestId('EstimationMonthsInput'), { target: { value: "0" } });
    fireEvent.change(screen.getByTestId('EstimationDaysInput'), { target: { value: "0" } });
    fireEvent.change(screen.getByTestId('EstimationHoursInput'), { target: { value: "0" } });
    fireEvent.change(screen.getByTestId('EstimationMinutesInput'), { target: { value: "10" } });
    fireEvent.change(screen.getByTestId('TagsSelect'), { target: { value: "2" } });
    fireEvent.click(screen.getByText('Add'));
    fireEvent.change(screen.getByTestId('text-area'), { target: { value: "Updated description" } });
    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(0);
    });
  });

  test('try to update task when server is down', async () => {

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
    axiosPrivate.put.mockImplementation(() => {
      throw new Error();
    })
    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);

    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(screen.getByText('No Server Response')).toBeInTheDocument();
    });
  });

  test('try to update task when task is not found', async () => {

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
    axiosPrivate.put.mockImplementation(() => {
      throw { response: { status: 404 } };
    });
    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);

    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(screen.getByText('Task not found!(May be deleted)')).toBeInTheDocument();
    });
  });

  test('try to update task when internal server error', async () => {

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
    axiosPrivate.put.mockImplementation(() => {
      throw { response: { status: 500 } };
    });
    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);

    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(screen.getByText('Update task Failed')).toBeInTheDocument();
    });
  });

  test('try to create task when server is down', async () => {

    axiosPrivate.post.mockImplementation((url, data) => {
      throw new Error();
    });
    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);
    await act(async () => {
      render(<ManageTask active={true} />);
    });


    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(screen.getByText('No Server Response')).toBeInTheDocument();
    });
  });

  test('try to create task when internal server error', async () => {

    axiosPrivate.post.mockImplementation(() => {
      throw { response: { status: 500 } };
    });
    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);
    await act(async () => {
      render(<ManageTask active={true} />);
    });


    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(screen.getByText('Create task Failed')).toBeInTheDocument();
    });
  });

  test('test estimation input', async () => {

    await act(async () => {
      render(<ManageTask active={true} />);
    })

    fireEvent.change(screen.getByTestId('EstimationYearsInput'), { target: { value: "-1" } });
    expect(screen.getByTestId('EstimationYearsInput').value).toBe("0");
    fireEvent.change(screen.getByTestId('EstimationYearsInput'), { target: { value: "200" } });
    expect(screen.getByTestId('EstimationYearsInput').value).toBe("100");
    fireEvent.change(screen.getByTestId('EstimationYearsInput'), { target: { value: "10" } });
    expect(screen.getByTestId('EstimationYearsInput').value).toBe("10");
    fireEvent.change(screen.getByTestId('EstimationMonthsInput'), { target: { value: "-1" } });
    expect(screen.getByTestId('EstimationMonthsInput').value).toBe("0");
    fireEvent.change(screen.getByTestId('EstimationMonthsInput'), { target: { value: "100" } });
    expect(screen.getByTestId('EstimationMonthsInput').value).toBe("11");
    fireEvent.change(screen.getByTestId('EstimationMonthsInput'), { target: { value: "1" } });
    expect(screen.getByTestId('EstimationMonthsInput').value).toBe("1");
    fireEvent.change(screen.getByTestId('EstimationDaysInput'), { target: { value: "-1" } });
    expect(screen.getByTestId('EstimationDaysInput').value).toBe("0");
    fireEvent.change(screen.getByTestId('EstimationDaysInput'), { target: { value: "100" } });
    expect(screen.getByTestId('EstimationDaysInput').value).toBe("30");
    fireEvent.change(screen.getByTestId('EstimationDaysInput'), { target: { value: "1" } });
    expect(screen.getByTestId('EstimationDaysInput').value).toBe("1");
    fireEvent.change(screen.getByTestId('EstimationHoursInput'), { target: { value: "-1" } });
    expect(screen.getByTestId('EstimationHoursInput').value).toBe("0");
    fireEvent.change(screen.getByTestId('EstimationHoursInput'), { target: { value: "100" } });
    expect(screen.getByTestId('EstimationHoursInput').value).toBe("23");
    fireEvent.change(screen.getByTestId('EstimationHoursInput'), { target: { value: "1" } });
    expect(screen.getByTestId('EstimationHoursInput').value).toBe("1");
    fireEvent.change(screen.getByTestId('EstimationMinutesInput'), { target: { value: "-1" } });
    expect(screen.getByTestId('EstimationMinutesInput').value).toBe("0");
    fireEvent.change(screen.getByTestId('EstimationMinutesInput'), { target: { value: "100" } });
    expect(screen.getByTestId('EstimationMinutesInput').value).toBe("59");
    fireEvent.change(screen.getByTestId('EstimationMinutesInput'), { target: { value: "1" } });
    expect(screen.getByTestId('EstimationMinutesInput').value).toBe("1");
    fireEvent.change(screen.getByTestId('EstimationYearsInput'), { target: { value: "100" } });
    expect(screen.getByTestId('EstimationMonthsInput').value).toBe("0");
    expect(screen.getByTestId('EstimationDaysInput').value).toBe("0");
    expect(screen.getByTestId('EstimationHoursInput').value).toBe("0");
    expect(screen.getByTestId('EstimationMinutesInput').value).toBe("0");
  });

  test('successful task deletion', async () => {

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

    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);

    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.click(screen.getByTestId('DeleteButton'));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/board/1');
    });
  });

  test('try to delete when server is down', async () => {

    axiosPrivate.delete.mockImplementation(() => {
      throw new Error();
    });

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

    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);

    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.click(screen.getByTestId('DeleteButton'));

    await waitFor(() => {
      expect(screen.getByText('No Server Response')).toBeInTheDocument();
    });
  });

  test('try to delete when internal server error', async () => {

    axiosPrivate.delete.mockImplementation(() => {
      throw { response: { status: 500 } };
    });

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

    const mockNavigate = jest.fn();
    Router.useNavigate.mockReturnValue(mockNavigate);

    await act(async () => {
      render(<ManageTask active={true} task={mockTask} />);
    })

    fireEvent.click(screen.getByTestId('DeleteButton'));

    await waitFor(() => {
      expect(screen.getByText('Delete task Failed')).toBeInTheDocument();
    });
  });

});
