import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import MangeTime from '../components/MangeTime';
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
    useLocation: jest.fn(),
}));

jest.mock('../components/Toolbar', () => () => <div data-testid="mocked-toolbar">Mocked Toolbar</div>);

describe('MangeTime component', () => {
  let mockOwner, mockEmployees, mockManagers;
  mockOwner = { id: 1, name: 'owner', username: 'owner' };

  mockEmployees = [{ id: 2, name: 'employee1', username: 'employee1' },
  { id: 3, name: 'employee2', username: 'employee2' }];

  mockManagers = [{ id: 4, name: 'manager1', username: 'manager1' },
  { id: 5, name: 'manager2', username: 'manager2' }];

  let mockTmView = [{
    "taskId": 1,
    "id": 1,
    "description": "Description",
    "dateTime": "2024-02-09T14:07:20.475Z"
  }];

  const mockTask = [{
      "taskId" : '1',
      "description": "Description",
      "title": "Task title",
      "dateTime": "2024-02-09T14:07:20.475Z",
      "assignee": "employee1",
      "reporter": "employee2",
      "priority": "Medium",
      "severity": "Moderate",
      "estimation": 2,
      "status": "To Do",
      "tags": [
        1,
        2
      ],
      "creationTime": "2024-02-09T14:07:20.475Z",
      "boardId": 1
    }];

    beforeEach(() => {
      Router.useLocation.mockReturnValue({
        state: undefined,
        pathname: "edit",
    });

      Router.useParams.mockReturnValue({ taskId: '1', id: '1', timeId: '1', selectedTaskId: '1' });
      const mockUser = { id: 1, name: 'user', username: 'user' };
      jest.spyOn(Storage.prototype, 'getItem');
      Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));


      const mockNavigate = jest.fn();
      Router.useNavigate.mockReturnValue(mockNavigate);

          const mockBoard = [{
            "taskId": 1,
            "id": 1,
            "boardId": 1
          }];

          const mockTmView = [{
            "taskId": 1,
            "id": 1,
            "description": "Description",
            "dateTime": "2024-02-09T14:07:20.475Z"
          }];
    
        axiosPrivate.get.mockImplementation((url) => {
          if (url === `/tm/1`) {
            return {
              data: mockTmView,
            };
         }
          else if (url === `/board/1/owner`) {
                return {
                  data: mockOwner,
                };
              } 
          else if (url === `/board/1/managers`) {
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
          } else if (url === `/user/getuserboards/1`) {
            return {
              data: mockBoard,
            };
          } else if (url === `/board/1/tasks`) {
            return {
              data: mockTask,
            };
          }           return {
            data: "someData",
          };
        })
    
        
    });

    test('renders without crashing', async() => {
          await act(async () => {
            render(<MangeTime />);
          });
          fireEvent.click(screen.getByText('Save'));
          await waitFor(() => {
            expect(screen.getByText('Save')).toBeInTheDocument();
          });


    });

    test('renders when server is down', async() => {
          axiosPrivate.get.mockImplementation((url) => {
           if (url === `/user/getuserboards/1`) {
            throw new Error();
            }          
          })

          await act(async () => {
            render(<MangeTime />);
          });
          await waitFor(() => {
            expect(screen.getByText('No Server Response')).toBeInTheDocument();
          });


    });


    test('update tm task is not selected', async() => {

      Router.useParams.mockReturnValue({ id: '1',  selectedTaskId: -1  });

      await act(async () => {
        render(<MangeTime />);
      });
      fireEvent.click(screen.getByText('Save'));
      await waitFor(() => {
        expect(screen.getByText('Task is not selected!')).toBeInTheDocument();
      });


  });

  test('save tm task is not selected', async() => {
    Router.useLocation.mockReturnValue({
      state: undefined,
      pathname: "not3диt",
  });

    Router.useParams.mockReturnValue({ id: '1',  selectedTaskId: -1  });

    await act(async () => {
      render(<MangeTime />);
    });
    fireEvent.click(screen.getByText('Save'));
    await waitFor(() => {
      expect(screen.getByText('Task is not selected!')).toBeInTheDocument();
    });


});


  test('update task description >140', async() => {

    const mockTmView = [{
      "taskId": 1,
      "id": 1,
      "description": "DescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescription",
      "dateTime": "2024-02-09T14:07:20.475Z"
    }];

    axiosPrivate.get.mockImplementation((url) => {
      if (url === `/tm/1`) {
        return {
          data: mockTmView,
        };
     }
      else if (url === `/board/1/owner`) {
            return {
              data: mockOwner,
            };
          } 
      else if (url === `/board/1/managers`) {
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
      } else if (url === `/user/getuserboards/1`) {
        return {
          data: mockBoard,
        };
      } else if (url === `/board/1/tasks`) {
        return {
          data: mockTask,
        };
      }           return {
        data: "someData",
      };
    });

    await act(async () => {
      render(<MangeTime />);
    });
    fireEvent.click(screen.getByText('Save'));
    await waitFor(() => {
      expect(screen.getByText('Description must be less than 140 characters long!')).toBeInTheDocument();
    });


});


test('save task description >140', async() => {

    mockTmView = [{
      "taskId": 1,
      "id": 1,
      "description": "DescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescription",
      "dateTime": "2024-02-09T14:07:20.475Z"
    }];

    axiosPrivate.get.mockImplementation((url) => {
    if (url === `/tm/1`) {
      return {
        data: mockTmView,
      };
    }
    else if (url === `/board/1/owner`) {
          return {
            data: mockOwner,
          };
        } 
    else if (url === `/board/1/managers`) {
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
    } else if (url === `/user/getuserboards/1`) {
      return {
        data: mockBoard,
      };
    } else if (url === `/board/1/tasks`) {
      return {
        data: mockTask,
      };
    }           return {
      data: "someData",
    };
  });
  Router.useLocation.mockReturnValue({
    state: undefined,
    pathname: "not3диt",
});



  await act(async () => {
    render(<MangeTime />);
  });
  fireEvent.click(screen.getByText('Save'));
  await waitFor(() => {
    expect(screen.getByText('Description must be less than 140 characters long!')).toBeInTheDocument();
  });


});



test('save task ', async() => {
  Router.useLocation.mockReturnValue({
    state: undefined,
    pathname: "not3диt",
});

  await act(async () => {
    render(<MangeTime />);
  });
  fireEvent.click(screen.getByText('Save'));
  await waitFor(() => {
    expect(screen.getByText('Save')).toBeInTheDocument();
  });


});

  test('successful task deletion', async() => {
      await act(async () => {
        render(<MangeTime />);
      });
      fireEvent.click(screen.getByText('Remove'));
      await waitFor(() => {
        expect(screen.getByText('Save')).toBeInTheDocument();
      });
});



});