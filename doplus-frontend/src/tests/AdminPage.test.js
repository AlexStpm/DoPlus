import React from 'react';
import { render, fireEvent, waitFor, screen, act } from '@testing-library/react';
import Router from 'react-router-dom';
import AdminPage from '../components/AdminPage';
import { axiosPrivate } from "../api/axios";

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: jest.fn(),
    useLocation: jest.fn(),
}));

jest.mock('../components/Toolbar', () => () => <div data-testid="mocked-toolbar">Mocked Toolbar</div>);

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

describe('AdminPage', () => {
    test('renders without crashing', async () => {
        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })
        expect(screen.getByText('Manage users')).toBeInTheDocument();
        expect(screen.getByText('Username')).toBeInTheDocument();
        expect(screen.getByText('First name')).toBeInTheDocument();
        expect(screen.getByText('Last Name')).toBeInTheDocument();
        expect(screen.getByText('Action')).toBeInTheDocument();
        expect(screen.getByText('Create new user')).toBeInTheDocument();
        expect(screen.getByText('First name:')).toBeInTheDocument();
        expect(screen.getByText('Last name:')).toBeInTheDocument();
        expect(screen.getByText('Username:')).toBeInTheDocument();
        expect(screen.getByText('Password:')).toBeInTheDocument();
        expect(screen.getByText('Create User')).toBeInTheDocument();

    });

    test('Try to create user with username more then 15 characters ', async () => {
        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "abcdmnmbqwertyuqdfsgbhsfghn" } });
        fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Ivan" } });
        fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "Purin" } });
        fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "12345678" } });
        fireEvent.click(screen.getByText('Create User'));

        expect(screen.getByText('Ensure that the username is between 3 and 15 characters in length, consisting only of letters and numbers without any punctuation marks or special symbols.')).toBeInTheDocument();
    });

    test('Try to create user with lastName less then 3 characters ', async () => {
        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "anton" } });
        fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Ivan" } });
        fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "P" } });
        fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "12345678" } });
        fireEvent.click(screen.getByText('Create User'));

        expect(screen.getByText('Please ensure that the last name is between 3 and 30 characters in length, and does not contain any punctuation marks or special symbols.')).toBeInTheDocument();
    });

    test('Try to create user with firstName special symbols ', async () => {
        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "anton" } });
        fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Iva1@,n" } });
        fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "Purin" } });
        fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "12345678" } });
        fireEvent.click(screen.getByText('Create User'));

        expect(screen.getByText('Please ensure that the first name is between 3 and 30 characters in length, and does not contain any punctuation marks or special symbols.')).toBeInTheDocument();
    });

    test('Try to create user with password less then 8 characters ', async () => {
        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "anton" } });
        fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Ivan" } });
        fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "Purin" } });
        fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "123" } });
        fireEvent.click(screen.getByText('Create User'));

        expect(screen.getByText('Ensure that the password is between 8 and 32 characters in length.')).toBeInTheDocument();
    });

    test('Successfully create user ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            },
            {
                "id": 2,
                "username": "awesomeuser",
                "firstName": "Jane",
                "laststName": "Smith",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.post.mockImplementation((url, data) => {
            return {
                data: "someData",
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })


        await act(async () => {
            fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "awesomeuser" } });
            fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Jane" } });
            fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "Smith" } });
            fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "12345678" } });
            fireEvent.click(screen.getByText('Create User'));

        })

    });

    test('Try to create user when server is down ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            },
            {
                "id": 2,
                "username": "awesomeuser",
                "firstName": "Jane",
                "laststName": "Smith",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.post.mockImplementation((url, data) => {
            if (url === `/register`) {
                throw new Error();
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "awesomeuser" } });
        fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Jane" } });
        fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "Smith" } });
        fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "12345678" } });

        await act(async () => {
            fireEvent.click(screen.getByText('Create User'));
        })
        await waitFor(() => { expect(screen.getByText('No Server Response')).toBeInTheDocument(); })

    });

    test('Try to create user with username alredy exist ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            },
            {
                "id": 2,
                "username": "awesomeuser",
                "firstName": "Jane",
                "laststName": "Smith",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.post.mockImplementation((url, data) => {
            if (url === `/register`) {
                throw { response: { status: 409 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "awesomeuser" } });
        fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Jane" } });
        fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "Smith" } });
        fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "12345678" } });

        await act(async () => {
            fireEvent.click(screen.getByText('Create User'));
        })
        await waitFor(() => { expect(screen.getByText('User with this username already exists')).toBeInTheDocument(); })

    });

    test('Try to create user when enternal server error ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            },
            {
                "id": 2,
                "username": "awesomeuser",
                "firstName": "Jane",
                "laststName": "Smith",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.post.mockImplementation((url, data) => {
            if (url === `/register`) {
                throw { response: { status: 500 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameInput'), { target: { value: "awesomeuser" } });
        fireEvent.change(screen.getByTestId('FirstInput'), { target: { value: "Jane" } });
        fireEvent.change(screen.getByTestId('LastInput'), { target: { value: "Smith" } });
        fireEvent.change(screen.getByTestId('PasswordInput'), { target: { value: "12345678" } });

        await act(async () => {
            fireEvent.click(screen.getByText('Create User'));
        })
        await waitFor(() => { expect(screen.getByText('User creation failed')).toBeInTheDocument(); })

    });

    test('Try to update user when server is down ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw new Error();
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        await act(async () => {
            fireEvent.click(screen.getByText('Update'));
        })

        await waitFor(() => { expect(screen.getByText('No Server Response')).toBeInTheDocument(); })

    });

    test('Try to update user when username alredy exist ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw { response: { status: 409 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        await act(async () => {
            fireEvent.click(screen.getByText('Update'));
        })

        await waitFor(() => { expect(screen.getByText('User with this username already exists')).toBeInTheDocument(); })

    });

    test('Try to update user when  enternal server error ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw { response: { status: 500 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        await act(async () => {
            fireEvent.click(screen.getByText('Update'));
        })

        await waitFor(() => { expect(screen.getByText('User update Failed')).toBeInTheDocument(); })

    });

    test('Try to change user active when server is down ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw new Error();
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        await act(async () => {
            fireEvent.click(screen.getByText('Deactivate'));
        })

        await waitFor(() => { expect(screen.getByText('No Server Response')).toBeInTheDocument(); })

    });

    test('Try to change user active when username alredy exist ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw { response: { status: 409 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        await act(async () => {
            fireEvent.click(screen.getByText('Deactivate'));
        })

        await waitFor(() => { expect(screen.getByText('User with this username already exists')).toBeInTheDocument(); })

    });

    test('Try to change user active when  enternal server error ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw { response: { status: 500 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        await act(async () => {
            fireEvent.click(screen.getByText('Deactivate'));
        })

        await waitFor(() => { expect(screen.getByText('User update Failed')).toBeInTheDocument(); })

    });

    test('Try to change user active when  enternal server error ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                return 'someData';
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        await act(async () => {
            fireEvent.click(screen.getByText('Deactivate'));
        })

        await waitFor(() => { expect(screen.getByText('Activate')).toBeInTheDocument(); })

    });

    test('Try to update user whith incorrect username ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw { response: { status: 500 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })

        fireEvent.change(screen.getByTestId('usernameTableInput'), { target: { value: "aw" } });
        fireEvent.change(screen.getByTestId('firstnameTableInput'), { target: { value: "Jane" } });
        fireEvent.change(screen.getByTestId('lastnameTableInput'), { target: { value: "Smith" } });

        await act(async () => {
            fireEvent.click(screen.getByText('Update'));
        })

        await waitFor(() => { expect(screen.getByText('Ensure that the username is between 3 and 15 characters in length, consisting only of letters and numbers without any punctuation marks or special symbols.')).toBeInTheDocument(); })

    });

    test('Try to update user whith incorrect username ', async () => {
        const mockUsers = [
            {
                "id": 1,
                "username": "coolguy",
                "firstName": "John",
                "laststName": "Doe",
                "isActive": true
            }
        ];

        axiosPrivate.get.mockImplementation((url) => {
            if (url === '/admin/getallusers') {
                return { data: mockUsers };
            }
        });

        axiosPrivate.put.mockImplementation((url, data) => {
            if (url === `/admin/updateuser/1`) {
                throw { response: { status: 500 } };
            };
        });

        const mockUser = { id: 1, name: 'rootadmin', username: 'rootadmin' };
        jest.spyOn(Storage.prototype, 'getItem');

        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

        await act(async () => {
            render(<AdminPage />);
        })
        await waitFor(() => { expect(screen.getByText('Deactivate')).toBeInTheDocument(); })

        fireEvent.change(screen.getByTestId('usernameTableInput'), { target: { value: "aw" } });
        fireEvent.change(screen.getByTestId('firstnameTableInput'), { target: { value: "Jane" } });
        fireEvent.change(screen.getByTestId('lastnameTableInput'), { target: { value: "Smith" } });

        await act(async () => {
            fireEvent.click(screen.getByText('Deactivate'));
        })

        await waitFor(() => { expect(screen.getByText('Ensure that the username is between 3 and 15 characters in length, consisting only of letters and numbers without any punctuation marks or special symbols.')).toBeInTheDocument(); })

    });
});
