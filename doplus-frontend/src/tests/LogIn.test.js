import React from 'react';
import { render, fireEvent, waitFor, screen } from '@testing-library/react';
import Router from 'react-router-dom';
import LogIn from '../components/LogIn';
import axios from '../api/axios';

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: jest.fn(),
    useLocation: jest.fn(),
}));


jest.mock('../api/axios', () => ({
    post: jest.fn(),
}));

describe('LogIn component', () => {

    test('renders without crashing', () => {
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        render(<LogIn />);
        expect(screen.getByText('Sign In')).toBeInTheDocument();
        expect(screen.getByLabelText('Username:')).toBeInTheDocument();
        expect(screen.getByLabelText('Password:')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Log In' })).toBeInTheDocument();
    });

    test('try to log in when server is down', async () => {
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        axios.post.mockImplementation(() => {
            throw new Error();
        });
        render(<LogIn />);
        fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'password' } });
        fireEvent.click(screen.getByText('Log In'));
        await waitFor(() => {
            expect(screen.getByText('No Server Response')).toBeInTheDocument();
        });
    });

    test('try to log in when Missing Username or Password', async () => {
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        axios.post.mockImplementation(() => {
            throw { response: { status: 400 } };
        });
        render(<LogIn />);
        fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Password:'), { target: { value: '' } });
        fireEvent.click(screen.getByText('Log In'));
        await waitFor(() => {
            expect(screen.getByText('Missing Username or Password')).toBeInTheDocument();
        });
    });

    test('try to log in when Incorrect username or password given', async () => {
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        axios.post.mockImplementation(() => {
            throw { response: { status: 403 } };
        });
        render(<LogIn />);
        fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'password' } });
        fireEvent.click(screen.getByText('Log In'));
        await waitFor(() => {
            expect(screen.getByText('Incorrect username or password')).toBeInTheDocument();
        });
    });

    test('try to log in when User is not active', async () => {
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        axios.post.mockImplementation(() => {
            throw { response: { status: 410 } };
        });
        render(<LogIn />);
        fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'password' } });
        fireEvent.click(screen.getByText('Log In'));
        await waitFor(() => {
            expect(screen.getByText('User is not active')).toBeInTheDocument();
        });
    });

    test('try to log in when internal server error', async () => {
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        axios.post.mockImplementation(() => {
            throw { response: { status: 500 } };
        });
        render(<LogIn />);
        fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'password' } });
        fireEvent.click(screen.getByText('Log In'));
        await waitFor(() => {
            expect(screen.getByText('Login Failed')).toBeInTheDocument();
        });
    });

    test('try to log as rootadmin', async () => {
        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        axios.post.mockReturnValue({
            data: {
                access_token: "accessToken",
                user_info: { username: "rootadmin" }
            }
        });
        render(<LogIn />);
        fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'rootadmin' } });
        fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'password' } });
        fireEvent.click(screen.getByText('Log In'));
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/admin', { replace: true });
        });
    });

    test('try to log as regular user', async () => {
        const mockNavigate = jest.fn();
        Router.useNavigate.mockReturnValue(mockNavigate);
        Router.useLocation.mockReturnValue({
            state: undefined,
        });
        axios.post.mockReturnValue({
            data: {
                access_token: "accessToken",
                user_info: { username: "user" }
            }
        });
        render(<LogIn />);
        fireEvent.change(screen.getByLabelText('Username:'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Password:'), { target: { value: 'password' } });
        fireEvent.click(screen.getByText('Log In'));
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/board', { replace: true });
        });
    });
});
