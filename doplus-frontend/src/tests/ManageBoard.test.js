import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import { useParams, useNavigate } from 'react-router-dom';
import ManageBoard from '../components/ManageBoard';
import { axiosPrivate } from "../api/axios";
import '@testing-library/jest-dom/extend-expect';
import { MemoryRouter } from 'react-router-dom';

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: jest.fn(),
  useParams: jest.fn()
}));

jest.mock('../api/axios', () => ({
    axiosPrivate: {
        get: jest.fn(),
        put: jest.fn(),
        post: jest.fn(),
        delete: jest.fn(),
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

jest.mock('../components/Toolbar', () => () => <div data-testid="mocked-toolbar">Mocked Toolbar</div>);

describe('ManageBoard component - Create new board scenario', () => {
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    axiosPrivate.get.mockResolvedValue({ data: [] });
    axiosPrivate.put.mockResolvedValue({});

    useNavigate.mockImplementation(() => mockNavigate);

    Storage.prototype.getItem = jest.fn(() => JSON.stringify({ id: "1", username: "testUser" }));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });


  test('renders correctly for creating a new board', async () => {
    axiosPrivate.post.mockResolvedValue({});
    useParams.mockReturnValue({ boardId: undefined });
    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    await screen.findByText('Create new board');

    const createBoardButtons = await screen.findAllByText('Create Board');
    expect(createBoardButtons.length).toBe(1);

    await screen.findByPlaceholderText('Board Name');

    expect(screen.queryByText('Board settings')).toBeNull();
  });

  test('displays an error message when the board name is longer than 140 characters', async () => {
    axiosPrivate.post.mockResolvedValue({});
    useParams.mockReturnValue({ boardId: undefined });

    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    const boardNameInput = screen.getByPlaceholderText('Board Name');
    fireEvent.change(boardNameInput, { target: { value: 'a'.repeat(141) } });

    const createBoardButton = screen.getByText('Create Board');
    fireEvent.click(createBoardButton);

    const errorMessage = await screen.findByText('Board name must be less than 140 characters long!');
    expect(errorMessage).toBeInTheDocument();
  });

  test('displays an error message when the board name is empty', async () => {
    axiosPrivate.post.mockResolvedValue({});
    useParams.mockReturnValue({ boardId: undefined });

    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    const boardNameInput = screen.getByPlaceholderText('Board Name');
    fireEvent.change(boardNameInput, { target: { value: '' } });

    const createBoardButton = screen.getByText('Create Board');
    fireEvent.click(createBoardButton);

    const errorMessage = await screen.findByText('Board name cannot be empty!');
    expect(errorMessage).toBeInTheDocument();
  });
});

describe('ManageBoard component - Edit board scenario', () => {
  const mockBoardInfo = { name: 'Test Board' };
  const mockOwner = { id: '1', name: 'OwnerFirstName OwnerLastName', username: 'ownerUser' };
  const mockManagers = [
    { id: '2', name: 'Manager One', username: 'manager1' },
    { id: '3', name: 'Manager Two', username: 'manager2' }
  ];
  const mockEmployees = [
    { id: '4', name: 'Employee First', username: 'employee1' },
    { id: '5', name: 'Employee Second', username: 'employee2' }
  ];
  const mockUsers = [
    { id: '1', name: 'OwnerFirstName OwnerLastName', username: 'ownerUser' },
    { id: '2', name: 'Manager One', username: 'manager1' },
    { id: '3', name: 'Manager Two', username: 'manager2' },
    { id: '4', name: 'Employee First', username: 'employee1' },
    { id: '5', name: 'Employee Second', username: 'employee2' },
    { id: '10', name: 'Employee Third', username: 'employee3' }
  ];
  const mockTags = [
    { id: '6', name: 'Tag1', isActive: true },
    { id: '7', name: 'Tag2', isActive: false }
  ];

  const mockNavigate = jest.fn();
  const mockBoardId = '456';

  beforeEach(() => {
    mockNavigate.mockClear();
    jest.clearAllMocks();
    axiosPrivate.get.mockClear();
    axiosPrivate.put.mockClear();
    axiosPrivate.delete.mockClear();
    axiosPrivate.post.mockClear();

    useParams.mockReturnValue({ boardId: mockBoardId });
    useNavigate.mockReturnValue(mockNavigate);

    axiosPrivate.get.mockImplementation((url) => {
      switch (url) {
        case `/board/456/tags`:
          return Promise.resolve({ data: mockTags });
        case `/board/456/owner`:
          return Promise.resolve({ data: mockOwner });
        case `/board/456/managers`:
          return Promise.resolve({ data: mockManagers });
        case `/board/456/employees`:
          return Promise.resolve({ data: mockEmployees });
        case `/user/all`:
          return Promise.resolve({ data: mockUsers });
        case `/board/456/info`:
          return Promise.resolve({ data: mockBoardInfo });
        case `/user/getuserboards/${JSON.parse(localStorage.getItem("ownerUser")).id}`:
          return Promise.resolve({ data: [{ id: '456', name: 'Test Board' }] });
        case `/notification/${JSON.parse(localStorage.getItem("ownerUser")).id}/all`:
          return Promise.resolve({ data: [] });
        default:
          return Promise.reject(new Error('not found'));
      }
    });

    Storage.prototype.getItem.mockReturnValue(JSON.stringify({ id: "1", username: "ownerUser" }));

  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('renders edit board page correctly', async () => {
    useParams.mockReturnValue({ boardId: '456' });
    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });
    await waitFor(() => expect(screen.getByText('Board settings')).toBeInTheDocument());

    expect(await screen.findByText('Edit')).toBeInTheDocument();
    expect(await screen.findByText(`${mockBoardInfo.name}`)).toBeInTheDocument();

    expect(await screen.findByText('Roles')).toBeInTheDocument();
    expect(await screen.findByText('Owner')).toBeInTheDocument();
    expect(await screen.findByText(new RegExp(`${mockOwner.name} \\(${mockOwner.username}\\)`, 'i'))).toBeInTheDocument();
    expect(await screen.findByText('Managers')).toBeInTheDocument();
    for (const manager of mockManagers) {
      expect(await screen.findByText(new RegExp(`${manager.name} \\(${manager.username}\\)`, 'i'))).toBeInTheDocument();
    }
    expect(await screen.findByText('Employees')).toBeInTheDocument();
    for (const employee of mockEmployees) {
      expect(await screen.findByText(new RegExp(`${employee.name} \\(${employee.username}\\)`, 'i'))).toBeInTheDocument();
    }

    const addButtons = await screen.findAllByText('Add');
    expect(addButtons.length).toBe(3);
    const removeButtons = document.querySelectorAll('.removeUserButton');
    expect(removeButtons.length).toBe(mockManagers.length + mockEmployees.length);

    expect(await screen.findByText('Tags')).toBeInTheDocument();
    expect(await screen.findByText('Active tags')).toBeInTheDocument();
    expect(await screen.findByText('Unactive tags')).toBeInTheDocument();
    for (const tag of mockTags) {
      expect(await screen.findByText(new RegExp(`${tag.name}`, 'i'))).toBeInTheDocument();
    }
    expect(document.querySelector('.btn.btn-success')).toBeInTheDocument();
    expect(document.querySelector('.btn.btn-danger:not(.removeUserButton)')).toBeInTheDocument();

    expect(screen.getByText('Archive board')).toBeInTheDocument();
    expect(screen.getByText('Close')).toBeInTheDocument();

  });

  test('displays "No Server Response" error message when fetching tags fails without server response', async () => {
    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith('/tags')) {
        return Promise.reject({ response: undefined });
      }
      switch (url) {
        case `/board/${mockBoardId}/owner`:
          return Promise.resolve({ data: mockOwner });
        case `/board/${mockBoardId}/managers`:
          return Promise.resolve({ data: mockManagers });
        case `/board/${mockBoardId}/employees`:
          return Promise.resolve({ data: mockEmployees });
        case `/user/all`:
          return Promise.resolve({ data: mockUsers });
        case `/board/${mockBoardId}/info`:
          return Promise.resolve({ data: mockBoardInfo });
        default:
          return Promise.resolve({ data: [] });
      }
    });

    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    const errorMessage = await screen.findByText("No Server Response");
    expect(errorMessage).toBeInTheDocument();
  });

  test('displays "No Server Response" error message when fetching board info fails', async () => {
    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith(`/board/${mockBoardId}/tags`)) {
        return Promise.resolve({ data: [] });
      }
      if (url.endsWith(`/board/${mockBoardId}/info`)) {
        return Promise.reject({ response: undefined });
      }
      return Promise.resolve({ data: [] });
    });

    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    expect(await screen.findByText("No Server Response")).toBeInTheDocument();
  });

  test('displays "Get info Failed" error message when fetching board info fails', async () => {
    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith(`/board/${mockBoardId}/tags`)) {
        return Promise.resolve({ data: [] });
      }
      if (url.endsWith(`/board/${mockBoardId}/info`)) {
        return Promise.reject({ response: { status: 404 } });
      }
      return Promise.resolve({ data: [] });
    });

    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    expect(await screen.findByText("Get info Failed")).toBeInTheDocument();
  });

  test('redirects to /unauthorized if the current user is not the owner or a manager', async () => {
    Storage.prototype.getItem.mockReturnValue(JSON.stringify({ id: "999", username: "notOwnerOrManager" }));

    axiosPrivate.get.mockResolvedValueOnce({ data: [] });

    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    const editButton = screen.getByText('Edit');
    await act(async () => {
      fireEvent.click(editButton);
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/unauthorized");
    });
  });

  test('sets isEditing to true if the current user is the owner or a manager', async () => {
    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    const editButton = screen.getByText('Edit');
    await act(async () => {
      fireEvent.click(editButton);
    });

    await waitFor(() => {
      const saveButton = screen.getByText('Save');
      expect(saveButton).toBeInTheDocument();
    });
  });

  test('displays an error message if the board name is too long or empty', async () => {
    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    const editButton = screen.getByText('Edit');
    await act(async () => {
      fireEvent.click(editButton);
    });

    const boardNameInput = screen.getByPlaceholderText('Board Name');
    fireEvent.change(boardNameInput, { target: { value: '' } });

    const saveButton = screen.getByText('Save');
    fireEvent.click(saveButton);

    expect(await screen.findByText("Board name must be less than 140 characters long and not empty!")).toBeInTheDocument();
  });

  test('successfully removes a manager from the manager list', async () => {
    const managerToRemove = mockManagers[0];

    axiosPrivate.delete.mockResolvedValueOnce({});

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const updatedManagers = mockManagers.filter(manager => manager.id !== managerToRemove.id);
    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith(`/board/${mockBoardId}/managers`)) {
        return Promise.resolve({ data: updatedManagers });
      }
      return Promise.resolve({ data: [] });
    });

    const removeButton = screen.getByTestId(`RemoveManagerButtonSubmit${managerToRemove.id}`);
    fireEvent.click(removeButton);

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      expect(axiosPrivate.delete).toHaveBeenCalledWith(`/board/${mockBoardId}/managers/${managerToRemove.id}/remove`);
    });

    await waitFor(() => {
      expect(axiosPrivate.get).toHaveBeenCalledWith(`/board/${mockBoardId}/managers`);
    });

    const managerText = new RegExp(`${managerToRemove.name} \\(${managerToRemove.username}\\)`);
    expect(screen.queryByText(managerText)).not.toBeInTheDocument();
  });

  test('displays "Remove manager Failed" error message when removing a manager fails with server error', async () => {
    axiosPrivate.delete.mockRejectedValueOnce({ response: { data: 'Error message', status: 404 } });
  
    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Test Board')).toBeInTheDocument();
    });
  
    const removeManagerButton = screen.getByTestId(`RemoveManagerButtonSubmit${mockManagers[0].id}`);
    await act(async () => {
      fireEvent.click(removeManagerButton);
    });
  
    await waitFor(() => {
      expect(screen.getByText("Remove manager Failed")).toBeInTheDocument();
    });
  });
  
  test('displays "No Server Response" error message when removing a manager fails due to network error', async () => {
    axiosPrivate.delete.mockRejectedValueOnce({ response: undefined });
  
    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Test Board')).toBeInTheDocument();
    });
  
    const removeManagerButton = screen.getByTestId(`RemoveManagerButtonSubmit${mockManagers[0].id}`);
    await act(async () => {
      fireEvent.click(removeManagerButton);
    });
  
    await waitFor(() => {
      expect(screen.getByText("No Server Response")).toBeInTheDocument();
    });
  });

  test('successfully adds a manager and updates the manager list', async () => {
    const newManager = { id: '6', name: 'New Manager', username: 'newmanager' };
    axiosPrivate.post.mockResolvedValueOnce({ data: newManager });

    axiosPrivate.get.mockResolvedValueOnce({
      data: [mockOwner, ...mockManagers, newManager]
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addButton = screen.getByTestId('AddManagerButtonOpen');
    fireEvent.click(addButton);

    const selectInput = screen.getByTestId('AddManagerSelect');
    const submitButton = screen.getByTestId('AddManagerButtonSubmit');

    fireEvent.change(selectInput, { target: { value: newManager.username } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(axiosPrivate.post).toHaveBeenCalledWith(
        `/board/${mockBoardId}/managers/${newManager.id}/add`
      );
    });

    expect(await screen.findByText(new RegExp(`${newManager.name} \\(${newManager.username}\\)`))).toBeInTheDocument();
  });

  test('displays "No Server Response" error message when the server does not respond on adding manager', async () => {
    const newManager = { id: '6', name: 'New Manager', username: 'newmanager' };

    axiosPrivate.get.mockResolvedValueOnce({
      data: [mockOwner, ...mockManagers, newManager]
    });

    axiosPrivate.post.mockRejectedValueOnce({ response: null });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addButton = screen.getByTestId('AddManagerButtonOpen');
    fireEvent.click(addButton);

    const selectInput = screen.getByTestId('AddManagerSelect');
    const submitButton = screen.getByTestId('AddManagerButtonSubmit');

    fireEvent.change(selectInput, { target: { value: newManager.username } });
    fireEvent.click(submitButton);

    const errorMessage = await screen.findByText('No Server Response');
    expect(errorMessage).toBeInTheDocument();
  });

  test('displays "Add user to managers failed" error message when adding a manager fails', async () => {
    const newManager = { id: '6', name: 'New Manager', username: 'newmanager' };

    axiosPrivate.get.mockResolvedValueOnce({
      data: [mockOwner, ...mockManagers, newManager]
    });

    axiosPrivate.post.mockRejectedValueOnce({ response: { status: 404 } });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addButton = screen.getByTestId('AddManagerButtonOpen');
    fireEvent.click(addButton);

    const selectInput = screen.getByTestId('AddManagerSelect');
    const submitButton = screen.getByTestId('AddManagerButtonSubmit');

    fireEvent.change(selectInput, { target: { value: newManager.username } });
    fireEvent.click(submitButton);

    const errorMessage = await screen.findByText('Add user to managers failed');
    expect(errorMessage).toBeInTheDocument();
  });

  test('redirects to unauthorized page if the user is not an owner or manager when trying to add a manager', async () => {
    Storage.prototype.getItem = jest.fn(() => JSON.stringify({ id: "999", username: "notOwnerOrManager" }));
    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addManagerButton = screen.getByTestId('AddManagerButtonOpen');
    fireEvent.click(addManagerButton);

    expect(mockNavigate).toHaveBeenCalledWith("/unauthorized");
  });

  test('successfully removes a employee from the employee list', async () => {
    const employeeToRemove = mockEmployees[0];

    axiosPrivate.delete.mockResolvedValueOnce({});

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const updatedEmployees = mockEmployees.filter(employee => employee.id !== employeeToRemove.id);
    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith(`/board/${mockBoardId}/employees`)) {
        return Promise.resolve({ data: updatedEmployees });
      }
      return Promise.resolve({ data: [] });
    });

    const removeButton = screen.getByTestId(`RemoveEmployeeButtonSubmit${employeeToRemove.id}`);
    fireEvent.click(removeButton);

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      expect(axiosPrivate.delete).toHaveBeenCalledWith(`/board/${mockBoardId}/employees/${employeeToRemove.id}/remove`);
    });

    await waitFor(() => {
      expect(axiosPrivate.get).toHaveBeenCalledWith(`/board/${mockBoardId}/employees`);
    });

    const employeeText = new RegExp(`${employeeToRemove.name} \\(${employeeToRemove.username}\\)`);
    expect(screen.queryByText(employeeText)).not.toBeInTheDocument();
  });

  test('successfully adds a employee and updates the employee list', async () => {
    const newEmployee = { id: '8', name: 'New Employee', username: 'newemployee' };
    axiosPrivate.post.mockResolvedValueOnce({ data: newEmployee });

    axiosPrivate.get.mockResolvedValueOnce({
      data: [mockOwner, ...mockEmployees, newEmployee]
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addButton = screen.getByTestId('AddEmployeeButtonOpen');
    fireEvent.click(addButton);

    const selectInput = screen.getByTestId('AddEmployeeSelect');
    const submitButton = screen.getByTestId('AddEmployeeButtonSubmit');

    fireEvent.change(selectInput, { target: { value: newEmployee.username } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(axiosPrivate.post).toHaveBeenCalledWith(
        `/board/${mockBoardId}/employees/${newEmployee.id}/add`
      );
    });

    expect(await screen.findByText(new RegExp(`${newEmployee.name} \\(${newEmployee.username}\\)`))).toBeInTheDocument();
  });

  test('displays "No Server Response" error message when the server does not respond on adding employee', async () => {
    const newEmployee = { id: '8', name: 'New Employee', username: 'newemployee' };

    axiosPrivate.get.mockResolvedValueOnce({
      data: [mockOwner, ...mockEmployees, newEmployee]
    });

    axiosPrivate.post.mockRejectedValueOnce({ response: null });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addButton = screen.getByTestId('AddEmployeeButtonOpen');
    fireEvent.click(addButton);

    const selectInput = screen.getByTestId('AddEmployeeSelect');
    const submitButton = screen.getByTestId('AddEmployeeButtonSubmit');

    fireEvent.change(selectInput, { target: { value: newEmployee.username } });
    await act(async () => {
      fireEvent.click(submitButton);
    });

    const errorMessage = await screen.findByText('No Server Response');
    expect(errorMessage).toBeInTheDocument();
  });

  test('displays "Add user to employees failed" error message when adding a employee fails', async () => {
    const newEmployee = { id: '8', name: 'New Employee', username: 'newemployee' };

    axiosPrivate.get.mockResolvedValueOnce({
      data: [mockOwner, ...mockEmployees, newEmployee]
    });

    axiosPrivate.post.mockRejectedValueOnce({ response: { status: 404 } });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addButton = screen.getByTestId('AddEmployeeButtonOpen');
    fireEvent.click(addButton);

    const selectInput = screen.getByTestId('AddEmployeeSelect');
    const submitButton = screen.getByTestId('AddEmployeeButtonSubmit');

    fireEvent.change(selectInput, { target: { value: newEmployee.username } });
    await act(async () => {
      fireEvent.click(submitButton);
    });

    const errorMessage = await screen.findByText('Add user to employees failed');
    expect(errorMessage).toBeInTheDocument();
  });

  test('redirects to "/unauthorized" when an unauthorized user attempts to add an employee', async () => {
    Storage.prototype.getItem = jest.fn(() => JSON.stringify({ id: "999", username: "unauthorizedUser" }));

    useNavigate.mockImplementation(() => mockNavigate);

    render(
      <MemoryRouter>
        <ManageBoard />
      </MemoryRouter>
    );

    const addEmployeeButton = screen.getByTestId('AddEmployeeButtonOpen');
    await act(async () => {
      fireEvent.click(addEmployeeButton);
    });

    expect(mockNavigate).toHaveBeenCalledWith("/unauthorized");
  });

  test('successfully adds a tag and updates the tag list', async () => {
    const newTagName = 'New Tag';
    const mockTag = { id: 'new', name: newTagName, isActive: true };

    axiosPrivate.post.mockResolvedValueOnce({ data: mockTag });

    const initialTags = [
      { id: '1', name: 'Tag1', isActive: true },
      { id: '2', name: 'Tag2', isActive: false }
    ];

    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith(`/board/${mockBoardId}/tags`)) {
        return Promise.resolve({ data: [...initialTags, mockTag] });
      }
      return Promise.resolve({ data: [] });
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addTagButton = screen.getByTestId('AddTagButton');
    fireEvent.click(addTagButton);

    const input = screen.getByTestId('TagNameInput');
    fireEvent.change(input, { target: { value: newTagName } });

    await act(async () => {
      fireEvent.click(addTagButton);
    });


    await waitFor(() => {
      expect(axiosPrivate.post).toHaveBeenCalledWith(
        `/board/tag/add`,
        { boardId: "456", name: "New Tag" }
      );
    });

    expect(await screen.findByText(newTagName)).toBeInTheDocument();
  });

  test('displays an error message when the tag name is incorrect', async () => {
    const longTagName = 'a'.repeat(141);

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const addTagButton = screen.getByTestId('AddTagButton');
    fireEvent.click(addTagButton);

    const input = screen.getByTestId('TagNameInput');
    fireEvent.change(input, { target: { value: longTagName } });

    await act(async () => {
      fireEvent.click(addTagButton);
    });

    expect(await screen.findByText('Tag name must be less than 140 characters long and not empty!')).toBeInTheDocument();
  });

  test('successfully deactivates a tag and updates the tag list', async () => {
    const tagToDeactivate = mockTags[0];
    const updatedTag = { ...tagToDeactivate, isActive: false };

    axiosPrivate.put.mockResolvedValueOnce({});

    const initialTags = [
      tagToDeactivate,
      { id: '2', name: 'Tag2', isActive: true }
    ];

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith(`/board/${mockBoardId}/tags`)) {
        return Promise.resolve({ data: [updatedTag, ...initialTags.filter(tag => tag.id !== tagToDeactivate.id)] });
      }
      return Promise.resolve({ data: [] });
    });

    const deactivateButton = screen.getByTestId(`DeactivateTagButton${tagToDeactivate.id}`);
    await act(async () => {
      fireEvent.click(deactivateButton);
    });

    await waitFor(() => {
      expect(axiosPrivate.put).toHaveBeenCalledWith(
        `/board/tag/${tagToDeactivate.id}/deactivate`
      );
    });

    const activateButton = screen.getByTestId(`ActivateTagButton${tagToDeactivate.id}`);
    expect(activateButton).toBeInTheDocument();
  });

  test('successfully activates a tag and updates the tag list', async () => {
    const tagToActivate = mockTags[1];
    const updatedTag = { ...tagToActivate, isActive: true };

    axiosPrivate.put.mockResolvedValueOnce({});

    const initialTags = [
      { id: '1', name: 'Tag1', isActive: true },
      tagToActivate
    ];


    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    axiosPrivate.get.mockImplementation(url => {
      if (url.endsWith(`/board/${mockBoardId}/tags`)) {
        return Promise.resolve({ data: [updatedTag, ...initialTags.filter(tag => tag.id !== tagToActivate.id)] });
      }
      return Promise.resolve({ data: [] });
    });

    const activateButton = screen.getByTestId(`ActivateTagButton${tagToActivate.id}`);
    await act(async () => {
      fireEvent.click(activateButton);
    });

    await waitFor(() => {
      expect(axiosPrivate.put).toHaveBeenCalledWith(
        `/board/tag/${tagToActivate.id}/activate`
      );
    });

    const deactivateButton = screen.getByTestId(`DeactivateTagButton${tagToActivate.id}`);
    expect(deactivateButton).toBeInTheDocument();
  });

  test('successfully archives a board and navigates to board page', async () => {
    axiosPrivate.put.mockResolvedValue({});

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const archiveButton = screen.getByText('Archive board');
    fireEvent.click(archiveButton);

    await waitFor(() => {
      expect(axiosPrivate.put).toHaveBeenCalledWith(`/board/456/archive`);
      expect(mockNavigate).toHaveBeenCalledWith(`/board/456`);
    });
  });

  test('displays an error message when archiving fails due to server not responding', async () => {
    axiosPrivate.put.mockRejectedValueOnce({ response: null });

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const archiveButton = screen.getByText('Archive board');
    fireEvent.click(archiveButton);

    await waitFor(() => {
      expect(screen.getByText("No Server Response")).toBeInTheDocument();
    });
  });

  test('button to archive the board is not available to managers', async () => {
    Storage.prototype.getItem = jest.fn(() => JSON.stringify({ id: "2", username: "manager1" }));

    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const archiveButton = screen.queryByText('Archive board');
    expect(archiveButton).not.toBeInTheDocument();
  });

  test('navigates to the board page when the Close button is clicked', async () => {
    await act(async () => {
      render(
        <MemoryRouter>
          <ManageBoard />
        </MemoryRouter>
      );
    });

    const closeButton = screen.getByText('Close');
    fireEvent.click(closeButton);

    expect(mockNavigate).toHaveBeenCalledWith(`/board/${mockBoardId}`);
  });
});
