import React from 'react';
import { render, act, fireEvent, waitFor, screen } from '@testing-library/react';
import BoardInfo from '../components/BoardInfo';
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

describe('BoardInfo component', () => {
    const boardId = '1';
    const mockOwner = { id: 1, name: 'John Doe', username: 'johndoe' };
    const mockManagers = [
        { id: 2, name: 'Jane Doe', username: 'janedoe' },
        { id: 3, name: 'Jim Beam', username: 'jimbeam' },
    ];

    beforeEach(() => {
        axiosPrivate.get.mockImplementation((url) => {
            switch (url) {
                case `/board/${boardId}/owner`:
                    return Promise.resolve({ data: mockOwner });
                case `/board/${boardId}/managers`:
                    return Promise.resolve({ data: mockManagers });
                default:
                    return Promise.reject(new Error('not found'));
            }
        });
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    test('loads and displays owner and managers information', async () => {
        let active = true;
        const setActive = jest.fn();
  
        await act(async () => {
            render(<BoardInfo boardId={boardId} active={active} setActive={setActive} />);
        });
  
        await act(async () => {
            await waitFor(() => {
                expect(screen.getByText(`Board owner: ${mockOwner.name} (${mockOwner.username})`)).toBeInTheDocument();
                mockManagers.forEach(manager => {
                    expect(screen.getByText(`${manager.name} (${manager.username})`)).toBeInTheDocument();
                });
            });
        });
    });

    test('calls setActive on backdrop click', async () => {
        const setActive = jest.fn();
        const active = true;

        await act(async () => {
            render(<BoardInfo boardId="1" active={active} setActive={setActive} />);
        });

        const boardInfoDiv = screen.getByTestId('BoardInfo');
        fireEvent.click(boardInfoDiv);

        expect(setActive).toHaveBeenCalledWith(false);
    });

    test('handles API request errors', async () => {
        axiosPrivate.get.mockRejectedValueOnce(new Error('Async error'));

        const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

        const setActive = jest.fn();
        const active = true;

        await act(async () => {
            render(<BoardInfo boardId="1" active={active} setActive={setActive} />);
        });

        expect(consoleSpy).toHaveBeenCalled();

        consoleSpy.mockRestore();
    });

    test('stops click event propagation from BoardInfoContent', async () => {
        const setActive = jest.fn();
        const active = true;

        await act(async () => {
            render(<BoardInfo boardId="1" active={active} setActive={setActive} />);
        });

        const boardInfoContentDiv = screen.getByTestId('BoardInfo').querySelector('.BoardInfoContent');
        fireEvent.click(boardInfoContentDiv);

        expect(setActive).not.toHaveBeenCalled();
    });

    test('renders with inactive class when active prop is false', async () => {
        const setActive = jest.fn();
        const active = false;

        await act(async () => {
            render(<BoardInfo boardId="1" active={active} setActive={setActive} />);
        });

        const boardInfoDiv = screen.getByTestId('BoardInfo');
        expect(boardInfoDiv).toHaveClass('BoardInfo');
    });
});
