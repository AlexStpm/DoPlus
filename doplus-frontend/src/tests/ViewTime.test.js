import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import ViewTime from '../components/ViewTime';
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

describe('ViewTime component', () => {
    beforeEach(() => {
        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);

        Router.useParams.mockReturnValue({ taskId: '1', boardId: '1' , userId: 1});
        Router.useLocation.mockReturnValue({
            state: undefined,
        });

        const mockUser = { id: 1, name: 'user', username: 'user' };
        jest.spyOn(Storage.prototype, 'getItem');
        Storage.prototype.getItem = jest.fn().mockReturnValue(JSON.stringify(mockUser));

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
          }, 
          {
            "taskId": 2,
            "id": 2,
            "description": "Description",
            "dateTime": "2024-02-08T14:07:20.475Z"
          }];
    
        axiosPrivate.get.mockImplementation((url) => {
          if (url === `/tm/1`) {
            return {
              data: mockTmView,
            };
          }
          else if (url === `/user/getuserboards/1`) {
            return {
              data: mockBoard,
            };
          } 
            return {
            data: "someData",
          };
        })
    });

    test('renders without crashing', async() => {

        await act(async () => {
            render(<ViewTime />);
          });
          await waitFor(() => {
            expect(screen.getByText('User time overview')).toBeInTheDocument();
          });

    });

    test('handle day change', async() => {

        await act(async () => {
            render(<ViewTime />);
          });

        fireEvent.change(screen.getByTestId('DaysChangeInput'), { target: { value: "1" } });
        fireEvent.click(screen.getByText('Accept'));
          await waitFor(() => {
            expect(screen.getByText('User time overview')).toBeInTheDocument();
          });

    });

    test('handle day change NaN', async() => {
        await act(async () => {
            render(<ViewTime />);
          });

        fireEvent.change(screen.getByTestId('DaysChangeInput'), { target: { value: "x" } });
          await waitFor(() => {
            expect(screen.getByText('User time overview')).toBeInTheDocument();
          });

    });

    test('handle day change <1', async() => {

        await act(async () => {
            render(<ViewTime />);
          });

        fireEvent.change(screen.getByTestId('DaysChangeInput'), { target: { value: "0" } });
          await waitFor(() => {
            expect(screen.getByText('User time overview')).toBeInTheDocument();
          });

    });

    test('handle day change >100', async() => {

        await act(async () => {
            render(<ViewTime />);
          });

        fireEvent.change(screen.getByTestId('DaysChangeInput'), { target: { value: "111" } });
          await waitFor(() => {
            expect(screen.getByText('User time overview')).toBeInTheDocument();
          });

    });



});