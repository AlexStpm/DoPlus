package ltd.future.planning.tech.DoPlus.dao;

import ltd.future.planning.tech.DoPlus.entity.Board;
import ltd.future.planning.tech.DoPlus.entity.Notification;
import ltd.future.planning.tech.DoPlus.entity.Tag;
import ltd.future.planning.tech.DoPlus.entity.Task;
import ltd.future.planning.tech.DoPlus.entity.TimeManagement;
import ltd.future.planning.tech.DoPlus.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DAOTests {
    @Mock
    private JdbcTemplate mockJdbcTemplate;
    @InjectMocks
    private DAO dao;

    /*************** GET METHOD TESTS ***************/
    //2 - /user/all
    @Test
    public void usersExistGetAllUsersReturnsListOfUsers() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("user_id")).thenReturn(1, 2);
        when(rs.getString("username")).thenReturn("user1", "user2");
        when(rs.getString("password")).thenReturn("pass1", "pass2");
        when(rs.getString("first_name")).thenReturn("First1", "First2");
        when(rs.getString("last_name")).thenReturn("Last1", "Last2");
        when(rs.getInt("role_id")).thenReturn(1, 2);
        when(rs.getInt("is_active")).thenReturn(1, 1);

        when(mockJdbcTemplate.query(eq("SELECT * FROM Users "), any(ResultSetExtractor.class))).thenAnswer(invocation -> {
            ResultSetExtractor<List<User>> extractor = invocation.getArgument(1);
            return extractor.extractData(rs);
        });

        Optional<List<User>> result = dao.getAllUsers();

        assertTrue(result.isPresent());
        List<User> users = result.get();
        assertEquals(2, users.size());

        assertEquals(1, users.get(0).getUserId());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("pass1", users.get(0).getPassword());
        assertEquals("First1", users.get(0).getFirstName());
        assertEquals("Last1", users.get(0).getLastName());
        assertEquals(1, users.get(0).getRoleId());
        assertEquals(1, users.get(0).getIsActive());

        assertEquals(2, users.get(1).getUserId());
        assertEquals("user2", users.get(1).getUsername());
        assertEquals("pass2", users.get(1).getPassword());
        assertEquals("First2", users.get(1).getFirstName());
        assertEquals("Last2", users.get(1).getLastName());
        assertEquals(2, users.get(1).getRoleId());
        assertEquals(1, users.get(1).getIsActive());
    }

    @Test
    public void usersDoNotExistGetAllUsersReturnsOptionalEmpty() {
        when(mockJdbcTemplate.query(eq("SELECT * FROM Users "), any(ResultSetExtractor.class))).thenAnswer(invocation -> {
            ResultSetExtractor<List<User>> extractor = invocation.getArgument(1);
            ResultSet rs = mock(ResultSet.class);
            when(rs.next()).thenReturn(false);
            return extractor.extractData(rs);
        });

        Optional<List<User>> result = dao.getAllUsers();

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty(), "Expected an empty list of users");
    }

    @Test
    public void databaseAccessExceptionGetAllUsersReturnsOptionalEmpty() {
        when(mockJdbcTemplate.query(eq("SELECT * FROM Users "), any(ResultSetExtractor.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Optional<List<User>> result = dao.getAllUsers();

        assertFalse(result.isPresent(), "Expected an empty Optional due to database access exception");
    }

    //4 - /user/getuserboards/{userId}
    @Test
    void userIdGivenGetUserBoardsReturnsListOfBoards() throws Exception {
        int userId = 1;

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("board_id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("Board 1");
        when(rs.getInt("is_archived")).thenReturn(0);

        when(mockJdbcTemplate.query(eq("SELECT b.* FROM Boards b " +
                        "JOIN BoardMembers bm ON b.board_id = bm.board_id " +
                        "WHERE bm.user_id = ?"),
                any(RowMapper.class), eq(userId))).thenAnswer(invocation -> {
            RowMapper<Board> rowMapper = invocation.getArgument(1);
            List<Board> boards = new ArrayList<>();
            while (rs.next()) {
                Board board = rowMapper.mapRow(rs, rs.getRow());
                boards.add(board);
            }
            return boards;
        });

        Optional<List<Board>> result = dao.getUserBoards(userId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        Board board = result.get().get(0);
        assertEquals(1, board.getBoardId());
        assertEquals("Board 1", board.getName());
        assertEquals(userId, board.getBoardId());
        assertEquals(0, board.getIsArchived());
    }

    @Test
    void boardsDoesNotExistGetUserBoardsReturnsOptionalEmpty() {
        //given no boards
        //when
        when(mockJdbcTemplate.query(eq("SELECT b.* FROM Boards b " +
                        "JOIN BoardMembers bm ON b.board_id = bm.board_id " +
                        "WHERE bm.user_id = ?"),
                any(RowMapper.class), anyInt())).thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getUserBoards(1);
        //then
        assertTrue(result.isEmpty());
    }


    //5 - /board/{boardId}/info
    @Test
    void boardExistGetBoardReturnsBoard() throws Exception {
        //given
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("board_id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("name");
        when(rs.getInt("is_archived")).thenReturn(0);
        //when
        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Boards WHERE board_id = ?"), any(RowMapper.class), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<Board> rowMapper = invocation.getArgument(1);
                    if (rs.next()) {
                        return rowMapper.mapRow(rs, rs.getRow());
                    }
                    throw new Exception();
                });
        var result = dao.getBoard(1);
        //then
        assertTrue(result.isPresent());
        assertEquals("name", result.get().getName());
    }

    @Test
    void boardDoesNotExistGetBoardReturnsOptionalEmpty() {
        //given no board
        //when
        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Boards WHERE board_id = ?"), any(RowMapper.class), eq(1)))
                .thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getBoard(1);
        //then
        assertTrue(result.isEmpty());
    }

    //6 - /board/{boardId}/tasks
    @Test
    void boardIdGivenGetBoardTasksReturnsListOfTasks() throws Exception {
        int boardId = 4;

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("title")).thenReturn("Task 1");
        when(rs.getInt("task_id")).thenReturn(1);
        when(rs.getInt("assignee_id")).thenReturn(2);
        when(rs.getInt("reporter_id")).thenReturn(3);
        when(rs.getInt("board_id")).thenReturn(boardId);
        when(rs.getString("priority")).thenReturn("High");
        when(rs.getString("severity")).thenReturn("Critical");
        when(rs.getInt("estimation")).thenReturn(5);
        when(rs.getString("status")).thenReturn("Open");
        when(rs.getString("description")).thenReturn("Description 1");
        when(rs.getString("creation_time")).thenReturn("2022-01-01");

        when(mockJdbcTemplate.query(eq("SELECT * FROM Tasks WHERE board_id = ?"), any(RowMapper.class), eq(boardId))).thenAnswer(invocation -> {
            RowMapper<Task> rowMapper = invocation.getArgument(1);
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                Task task = rowMapper.mapRow(rs, rs.getRow());
                tasks.add(task);
            }
            return tasks;
        });

        Optional<List<Task>> result = dao.getBoardTasks(boardId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        Task task = result.get().get(0);
        assertEquals("Task 1", task.getTitle());
        assertEquals(1, task.getTaskId());
        assertEquals(2, task.getAssigneeId());
        assertEquals(3, task.getReporterId());
        assertEquals(boardId, task.getBoardId());
        assertEquals("High", task.getPriority());
        assertEquals("Critical", task.getSeverity());
        assertEquals(5, task.getEstimation());
        assertEquals("Open", task.getStatus());
        assertEquals("Description 1", task.getDescription());
        assertEquals("2022-01-01", task.getCreationTime());
    }

    @Test
    void boardIdWithNoTasksGivenGetBoardTasksReturnsOptionalEmpty() {
        when(mockJdbcTemplate.query(eq("SELECT * FROM Tasks WHERE board_id = ?"), any(RowMapper.class), eq(4)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Optional<List<Task>> result = dao.getBoardTasks(4);

        assertFalse(result.isPresent());
    }


    //7 /board/{boardId}/owner
    @Test
    void ownerExistGetBoardOwnerReturnsUser() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("user_id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("user1");
        when(rs.getString("password")).thenReturn("pass1");
        when(rs.getString("first_name")).thenReturn("First1");
        when(rs.getString("last_name")).thenReturn("Last1");
        when(rs.getInt("role_id")).thenReturn(1);
        when(rs.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.queryForObject(eq("SELECT u.*  " +
                "FROM Users u " +
                "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                "JOIN Boards b ON b.board_id = bm.board_id " +
                "WHERE bm.role_on_board = 'Owner' AND b.board_id = ?; ?"), any(RowMapper.class), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<User> rowMapper = invocation.getArgument(1);
                    if (rs.next()) {
                        return rowMapper.mapRow(rs, rs.getRow());
                    }
                    throw new Exception();
                });
        var result = dao.getBoardOwner(1);

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }

    @Test
    void ownerDoesNotExistGetBoardOwnerReturnsEmpty() {
        when(mockJdbcTemplate.queryForObject(eq("SELECT u.*  " +
                "FROM Users u " +
                "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                "JOIN Boards b ON b.board_id = bm.board_id " +
                "WHERE bm.role_on_board = 'Owner' AND b.board_id = ?; ?"), any(RowMapper.class), eq(1)))
                .thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getBoardOwner(1);

        assertTrue(result.isEmpty());
    }

    //8 - /board/{boardId}/managers
    @Test
    void managersExistGetBoardManagersReturnsListOfUsers() throws Exception {

        int boardId = 4;

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("user_id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("Ivan");
        when(rs.getString("password")).thenReturn("pass123");
        when(rs.getString("first_name")).thenReturn("Ivan");
        when(rs.getString("last_name")).thenReturn("Kirov");
        when(rs.getInt("role_id")).thenReturn(2);
        when(rs.getInt("is_active")).thenReturn(1);


        when(mockJdbcTemplate.query(eq("SELECT u.* FROM Users u " +
                        "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                        "WHERE bm.board_id = ? AND bm.role_on_board = 'Manager'"),
                any(RowMapper.class), eq(boardId))).thenAnswer(invocation -> {
            RowMapper<User> rowMapper = invocation.getArgument(1);
            List<User> managers = new ArrayList<>();
            while (rs.next()) {
                User manager = rowMapper.mapRow(rs, rs.getRow());
                managers.add(manager);
            }
            return managers;
        });

        Optional<List<User>> result = dao.getBoardManagers(boardId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        User manager = result.get().get(0);
        assertEquals(1, manager.getUserId());
        assertEquals("Ivan", manager.getUsername());
        assertEquals("pass123", manager.getPassword());
        assertEquals("Ivan", manager.getFirstName());
        assertEquals("Kirov", manager.getLastName());
        assertEquals(2, manager.getRoleId());
        assertEquals(1, manager.getIsActive());
    }

    @Test
    void managersDoesNotExistGetBoardManagersReturnsOptionalEmpty() {
        //given no users
        //when
        when(mockJdbcTemplate.query(eq("SELECT u.* FROM Users u " +
                        "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                        "WHERE bm.board_id = ? AND bm.role_on_board = 'Manager'"),
                any(RowMapper.class), anyInt())).thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getBoardManagers(1);
        //then
        assertTrue(result.isEmpty());
    }


    //9 - /board/{boardId}/employees
    @Test
    void employeesExistGetBoardEmployeesReturnsListOfUsers() throws Exception {
        //given
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("user_id")).thenReturn(1, 2);
        when(rs.getString("username")).thenReturn("user1", "user2");
        when(rs.getString("password")).thenReturn("pass1", "pass2");
        when(rs.getString("first_name")).thenReturn("First1", "First2");
        when(rs.getString("last_name")).thenReturn("Last1", "Last2");
        when(rs.getInt("role_id")).thenReturn(1, 2);
        when(rs.getInt("is_active")).thenReturn(1, 1);

        //when
        when(mockJdbcTemplate.query(eq("SELECT u.* FROM Users u " +
                        "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                        "WHERE bm.board_id = ? AND bm.role_on_board = 'Employee'"),
                any(RowMapper.class), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<User> rowMapper = invocation.getArgument(1);
                    List<User> users = new ArrayList<>();
                    while (rs.next()) {
                        User user = rowMapper.mapRow(rs, rs.getRow());
                        users.add(user);
                    }
                    return users;
                });
        var result = dao.getBoardEmployees(1);
        //then
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void employeesDoesNotExistGetBoardEmployeesReturnsOptionalEmpty() {
        //given no users
        //when
        when(mockJdbcTemplate.query(eq("SELECT u.* FROM Users u " +
                        "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                        "WHERE bm.board_id = ? AND bm.role_on_board = 'Employee'"),
                any(RowMapper.class), anyInt())).thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getBoardEmployees(1);
        //then
        assertTrue(result.isEmpty());
    }


    //10 - /board/{boardId}/tags
    @Test
    void boardIdGivenGetAllBoardTagsReturnsListOfTags() {
        int boardId = 1;

        when(mockJdbcTemplate.query(eq("SELECT t.* FROM Tags t WHERE t.board_id = ?"), any(RowMapper.class), eq(boardId)))
                .thenAnswer(invocation -> {
                    RowMapper<Tag> rowMapper = invocation.getArgument(1);
                    ResultSet rsMock = mock(ResultSet.class);
                    when(rsMock.next()).thenReturn(true, true, false);
                    when(rsMock.getInt("tag_id")).thenReturn(1, 2);
                    when(rsMock.getInt("board_id")).thenReturn(boardId, boardId);
                    when(rsMock.getString("name")).thenReturn("Tag1", "Tag2");
                    when(rsMock.getInt("is_active")).thenReturn(1, 0);

                    List<Tag> tags = new ArrayList<>();
                    while (rsMock.next()) {
                        tags.add(rowMapper.mapRow(rsMock, 0));
                    }
                    return tags;
                });


        Optional<List<Tag>> result = dao.getAllBoardTags(boardId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("Tag1", result.get().get(0).getName());
        assertTrue(result.get().get(0).getIsActive() == 1);
        assertEquals("Tag2", result.get().get(1).getName());
        assertFalse(result.get().get(1).getIsActive() == 1);
    }

    @Test
    void boardIdWithNoTagsGivenGetAllBoardTagsReturnsOptionalEmpty() {
        int boardId = 1;

        when(mockJdbcTemplate.query(eq("SELECT t.* FROM Tags t WHERE t.board_id = ?"), any(RowMapper.class), eq(boardId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Optional<List<Tag>> result = dao.getAllBoardTags(boardId);

        assertTrue(result.isEmpty(), "Expected an empty Optional for board with no tags");
    }

    //11 - /task/getbyid/{taskId}
    @Test
    void taskExistGetTaskByIdReturnsTask() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("title")).thenReturn("Task 1");
        when(rs.getInt("task_id")).thenReturn(1);
        when(rs.getInt("assignee_id")).thenReturn(2);
        when(rs.getInt("reporter_id")).thenReturn(3);
        when(rs.getInt("board_id")).thenReturn(1);
        when(rs.getString("priority")).thenReturn("High");
        when(rs.getString("severity")).thenReturn("Critical");
        when(rs.getInt("estimation")).thenReturn(5);
        when(rs.getString("status")).thenReturn("Open");
        when(rs.getString("description")).thenReturn("Description 1");
        when(rs.getString("creation_time")).thenReturn("2022-01-01");

        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Tasks WHERE task_id = ?"), any(RowMapper.class), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<Task> rowMapper = invocation.getArgument(1);
                    if (rs.next()) {
                        return rowMapper.mapRow(rs, rs.getRow());
                    }
                    throw new Exception();
                });
        var result = dao.getTaskById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getTaskId());
    }

    @Test
    void taskDoesNotExistGetTaskByIdReturnsEmpty() {

        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Tasks WHERE task_id = ?"), any(RowMapper.class), eq(1)))
                .thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getTaskById(1);

        assertTrue(result.isEmpty());
    }

    //12 - /notification/{userId}/all
    @Test
    void userIdGivenGetUserNotificationsReturnsListOfTags() {
        int userId = 1;

        when(mockJdbcTemplate.query(eq("SELECT *" +
                "FROM Notifications " +
                "WHERE Notifications.user_id = ?;"), any(RowMapper.class), eq(userId)))
                .thenAnswer(invocation -> {
                    RowMapper<Notification> rowMapper = invocation.getArgument(1);
                    ResultSet rsMock = mock(ResultSet.class);
                    when(rsMock.next()).thenReturn(true, true, false);
                    when(rsMock.getInt("notification_id")).thenReturn(1, 2);
                    when(rsMock.getInt("user_id")).thenReturn(userId, userId);
                    when(rsMock.getInt("task_id")).thenReturn(1, 1);
                    when(rsMock.getInt("is_seen")).thenReturn(1, 0);

                    List<Notification> notification = new ArrayList<>();
                    while (rsMock.next()) {
                        notification.add(rowMapper.mapRow(rsMock, 0));
                    }
                    return notification;
                });


        Optional<List<Notification>> result = dao.getUserNotifications(userId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals(1, result.get().get(0).getNotificationId());
        assertTrue(result.get().get(0).getUserId() == 1);
        assertEquals(1, result.get().get(1).getTaskId());
        assertFalse(result.get().get(1).getIsSeen() == 1);
    }

    @Test
    void notificationsDoesNotExistGetUserNotificationsReturnsOptionalEmpty() {
        //given no notifications
        //when
        when(mockJdbcTemplate.query(eq("SELECT *" +
                        "FROM Notifications " +
                        "WHERE Notifications.user_id = ?;"),
                any(RowMapper.class), anyInt())).thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getUserNotifications(1);
        //then
        assertTrue(result.isEmpty());
    }

    //13 - /tm/{userId}
    @Test
    void tmExistGetTimeManagementDataReturnsListOfTm() throws Exception {
        //given
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("TimeManagement_id")).thenReturn(1, 2);
        when(rs.getInt("user_id")).thenReturn(1, 1);
        when(rs.getString("date_time")).thenReturn("datetime1", "datetime2");
        when(rs.getInt("task_id")).thenReturn(1, 2);
        when(rs.getString("description")).thenReturn("description1", "description2");

        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM TimeManagement WHERE user_id = ?"),
                any(RowMapper.class), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<TimeManagement> rowMapper = invocation.getArgument(1);
                    List<TimeManagement> timeManagements = new ArrayList<>();
                    while (rs.next()) {
                        TimeManagement timeManagement = rowMapper.mapRow(rs, rs.getRow());
                        timeManagements.add(timeManagement);
                    }
                    return timeManagements;
                });
        var result = dao.getTimeManagementData(1);
        //then
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void tmDoesNotExistGetTimeManagementDataReturnsOptionalEmpty() {
        //given no tm records
        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM TimeManagement WHERE user_id = ?"),
                any(RowMapper.class), anyInt())).thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getTimeManagementData(1);
        //then
        assertTrue(result.isEmpty());
    }

    /*************** POST METHOD TESTS ***************/
    //3 - /board/add/{userId}
    @Test
    void boardAddedSuccessfullyAddBoardReturnsSuccess() {

        when(mockJdbcTemplate.queryForObject(eq("SELECT last_insert_rowid()"), (Class<Object>) any()))
                .thenReturn(1);
        int result = dao.addBoard("boardName");

        assertEquals(1, result);
    }

    @Test
    void boardAddedUnsuccessfullyAddBoardReturnsFail() {

        when(mockJdbcTemplate.queryForObject(eq("SELECT last_insert_rowid()"), (Class<Object>) any()))
                .thenReturn(0);
        int result = dao.addBoard("boardName");

        assertEquals(0, result);
    }

    //1 - /register
    @Test
    void userExistGetUserByUsernameReturnsUser() throws Exception {
        //given user exist
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("user_id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("user1");
        when(rs.getString("password")).thenReturn("pass1");
        when(rs.getString("first_name")).thenReturn("First1");
        when(rs.getString("last_name")).thenReturn("Last1");
        when(rs.getInt("role_id")).thenReturn(1);
        when(rs.getInt("is_active")).thenReturn(1);
        //when
        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE username = '" + "user1" + "'"), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });
        var result = dao.getUserByUsername("user1");
        //then
        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }

    @Test
    void userDoesNotExistGetUserByUsernameReturnsOptionalEmpty() {
        //given user exist
        //when
        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE username = '" + "username" + "'"), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });
        var result = dao.getUserByUsername("username");
        //then
        assertTrue(result.isEmpty());
    }

    //4 - /board/{boardId}/managers/{userId}/add
    @Test
    void BoardIdAndUserIdAddUserToBoardManagersExecutesSuccessfully() {
        dao.addUserToBoardManagers(1, 1);
        verify(mockJdbcTemplate).update(eq("INSERT INTO BoardMembers (board_id, user_id, role_on_board) VALUES (?, ?, 'Manager')"),
                eq(1), eq(1));
    }

    //5 - /board/{boardId}/employees/{userId}/add
    @Test
    void userIsOnBoardCheckIfUserOnABoardReturnsTrue() throws Exception {
        //given user has role on board
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM BoardMembers WHERE user_id = ? AND board_id = ?"),
                any(ResultSetExtractor.class), anyInt(), anyInt())).thenAnswer(invocation -> {
            ResultSetExtractor<List<User>> extractor = invocation.getArgument(1);
            return extractor.extractData(rs);
        });
        boolean result = dao.checkIfUserOnABoard(1, 1);
        // Then
        assertTrue(result);
    }

    @Test
    void userIsNotOnBoardCheckIfUserOnABoardReturnsFalse() throws Exception {
        //given does not have role on board
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM BoardMembers WHERE user_id = ? AND board_id = ?"),
                any(ResultSetExtractor.class), anyInt(), anyInt())).thenAnswer(invocation -> {
            ResultSetExtractor<List<User>> extractor = invocation.getArgument(1);
            return extractor.extractData(rs);
        });
        boolean result = dao.checkIfUserOnABoard(1, 1);
        // Then
        assertFalse(result);
    }


    //6 - /board/tag/add
    @Test
    void validTagAndBoardIdAddTagToBoardExecutesSuccessfully() {
        dao.addTagToBoard(1, "New Tag");
        verify(mockJdbcTemplate).update(eq("INSERT INTO Tags (board_id, name, is_active) VALUES (?, ?, 1)"),
                eq(1), eq("New Tag"));
    }

    //8 - /notification/sent
    @Test
    void validNotificationAddNotificationExecutesSuccessfully() {
        Notification notification = Notification.builder()
                .userId(1)
                .taskId(1)
                .build();

        dao.addNotification(notification);

        verify(mockJdbcTemplate).update(eq("INSERT INTO Notifications (user_id, task_id, is_seen) VALUES (?, ?, 0)"),
                eq(notification.getUserId()),
                eq(notification.getTaskId()));
    }

    //9 - /tm/add
    @Test
    void validTimeManagementAddUserTimeManagementExecutesUpdate() {
        TimeManagement timeManagement = TimeManagement.builder()
                .userId(1)
                .dateTime("2023-01-01 10:00")
                .taskId(1)
                .description("Worked on task")
                .build();

        dao.addUserTimeManagement(timeManagement);

        verify(mockJdbcTemplate).update(
                eq("INSERT INTO TimeManagement (user_id, date_time, task_id, description) VALUES (?, ?, ?, ?)"),
                eq(timeManagement.getUserId()),
                eq(timeManagement.getDateTime()),
                eq(timeManagement.getTaskId()),
                eq(timeManagement.getDescription())
        );
    }
    //7 - /task/add/

    @Test
    void taskAddedSuccessfullyAddTaskToBoardReturnsSuccess() {

        var mockTask = new Task(1, 1, 1, 1, "high", "high", 1, "high", "description", "title", "01.01.99");
        when(mockJdbcTemplate.queryForObject(eq("SELECT last_insert_rowid()"), (Class<Object>) any()))
                .thenReturn(1);
        int result = dao.addTaskToBoard(mockTask);

        assertEquals(1, result);
    }

    @Test
    void taskAddedUnsuccessfullyAddTaskToBoardReturnsFail() {

        var mockTask = new Task(1, 1, 1, 1, "high", "high", 1, "high", "description", "title", "01.01.99");
        when(mockJdbcTemplate.queryForObject(eq("SELECT last_insert_rowid()"), (Class<Object>) any()))
                .thenReturn(0);
        int result = dao.addTaskToBoard(mockTask);

        assertEquals(0, result);
    }

    /*************** PUT METHOD TESTS ***************/
    //1 - /admin/updateuser/{userId}
    @Test
    void userExistGetUserByIdReturnsUser() throws Exception {
        //given user exists
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("user_id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("user1");
        when(rs.getString("password")).thenReturn("pass1");
        when(rs.getString("first_name")).thenReturn("First1");
        when(rs.getString("last_name")).thenReturn("Last1");
        when(rs.getInt("role_id")).thenReturn(1);
        when(rs.getInt("is_active")).thenReturn(1);
        //when
        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 1), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });
        var result = dao.getUserById(1);
        //then
        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }

    @Test
    void userDoesNotExistGetUserByIdReturnsOptionalEmpty() {
        //given user does not exist
        //when
        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 1), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });
        var result = dao.getUserById(1);
        //then
        assertTrue(result.isEmpty());
    }


    //2 - /board/{boardId}/update
    @Test
    void validParamsGivenUpdateBoardExecutesSuccessfully() {
        int boardId = 1;
        String newName = "Updated Board Name";

        when(mockJdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        dao.updateBoard(boardId, newName);

        verify(mockJdbcTemplate).update(eq("UPDATE Boards SET name = ? WHERE board_id = ?"), any(Object[].class));
    }

    //4- board/tag/{tagId}/deactivate
    @Test
    void validParamsGivenDeactivateBoardExecutesSuccessfully() {
        int tagId = 1;

        when(mockJdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        dao.deactivateTag(tagId);

        verify(mockJdbcTemplate).update(eq("UPDATE Tags SET is_active = 0 WHERE tag_id = ?"), any(Object[].class));
    }

    //6 - /task/{taskId}/update
    @Test
    void validTaskGivenUpdateTaskExecutesUpdate() {
        Task task = Task.builder()
                .taskId(1)
                .title("Updated Task")
                .assigneeId(2)
                .reporterId(3)
                .priority("High")
                .severity("Low")
                .estimation(5)
                .status("Open")
                .description("Updated Description")
                .build();

        Object[] expectedParams = new Object[]{
                task.getTitle(),
                task.getAssigneeId(),
                task.getReporterId(),
                task.getPriority(),
                task.getSeverity(),
                task.getEstimation(),
                task.getStatus(),
                task.getDescription(),
                task.getTaskId()
        };

        when(mockJdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        dao.updateTask(task);

        verify(mockJdbcTemplate).update(eq("UPDATE Tasks SET " +
                "title = ?, " +
                "assignee_id = ?, " +
                "reporter_id = ?, " +
                "priority = ?, " +
                "severity = ?, " +
                "estimation = ?, " +
                "status = ?, " +
                "description = ? " +
                "WHERE task_id = ?"), eq(expectedParams));
    }

    @Test
    void taskIdGivenDeleteTaskTagsExecutesDelete() {
        int taskId = 1;

        dao.deleteTaskTags(taskId);

        verify(mockJdbcTemplate).update(eq("DELETE FROM TaskTags WHERE task_id = ?"), eq(taskId));
    }

    @Test
    void taskIdAndTagIdsGivenAddTaskTagsExecutesInserts() {
        int taskId = 1;
        List<Integer> tagIds = Arrays.asList(2, 3, 4);

        dao.addTaskTags(taskId, tagIds);

        for (Integer tagId : tagIds) {
            verify(mockJdbcTemplate).update(
                    eq("INSERT INTO TaskTags (task_id, tag_id) VALUES (?, ?)"),
                    eq(taskId),
                    eq(tagId)
            );
        }
    }

    //8 - /notification/{notificationId}/setseen
    @Test
    void validNotificationSetSeenNotificationExecutesUpdate() {
        Notification notification = Notification.builder()
                .notificationId(1)
                .build();

        Object[] expectedParams = new Object[]{
                notification.getNotificationId()
        };

        when(mockJdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        dao.setSeenNotification(1);

        verify(mockJdbcTemplate).update(eq("UPDATE Notifications SET is_seen = 1 WHERE notification_id = ?"), eq(expectedParams));
    }

    /*************** DELETE METHOD TESTS ***************/
    //1 - /board/{boardId}/managers/{userId}/remove
    @Test
    void userRemovedSuccessfullyRemoveUserFromBoardManagersReturnsOne() {
        //when
        when(mockJdbcTemplate.update(eq("DELETE FROM BoardMembers WHERE board_id = ? AND user_id = ? AND role_on_board = 'Manager';"), anyInt(), anyInt()))
                .thenReturn(1);
        int result = dao.removeUserFromBoardManagers(1, 1);
        assertEquals(1, result);
    }

    @Test
    void failedToRemoveUserRemoveUserFromBoardManagersReturnsZero() {
        //when
        when(mockJdbcTemplate.update(eq("DELETE FROM BoardMembers WHERE board_id = ? AND user_id = ? AND role_on_board = 'Manager';"), anyInt(), anyInt()))
                .thenReturn(0);
        int result = dao.removeUserFromBoardManagers(1, 1);
        assertEquals(0, result);
    }

    //2 - /board/{boardId}/employees/{userId}/remove
    @Test
    void validBoardIdAndUserIdGivenRemoveUserFromBoardEmployeesReturnsOne() {
        int boardId = 1;
        int userId = 1;
        String sql = "DELETE FROM BoardMembers WHERE board_id = ? AND user_id = ? AND role_on_board = 'Employee';";

        when(mockJdbcTemplate.update(sql, boardId, userId)).thenReturn(1);

        int result = dao.removeUserFromBoardEmployees(boardId, userId);

        assertEquals(1, result);

        verify(mockJdbcTemplate, times(1)).update(eq(sql), eq(boardId), eq(userId));
    }

    //3 - /task/{taskId}/delete
    @Test
    void taskRemovedSuccessfullyDeleteTaskReturnsOne() {

        when(mockJdbcTemplate.update(eq("DELETE FROM Tasks WHERE task_id = ?"), anyInt()))
                .thenReturn(1);
        int result = dao.deleteTask(1);
        assertEquals(1, result);
    }

    @Test
    void failedToRemoveTaskDeleteTaskReturnsZero() {

        when(mockJdbcTemplate.update(eq("DELETE FROM Tasks WHERE task_id = ?"), anyInt()))
                .thenReturn(0);
        int result = dao.deleteTask(1);
    }

    //4 - /tm/{timeId}/delete
    @Test
    void timeDeleteSuccessfullyDeleteUserTimeManagementReturnsOne() {
        //when
        when(mockJdbcTemplate.update(eq("DELETE FROM TimeManagement WHERE TimeManagement_id = ?"), anyInt()))
                .thenReturn(1);
        int result = dao.deleteUserTimeManagement(1);
        assertEquals(1, result);
    }

    @Test
    void failedToDeleteDeleteUserTimeManagementReturnsZero() {
        //when
        when(mockJdbcTemplate.update(eq("DELETE FROM TimeManagement WHERE TimeManagement_id = ?"), anyInt()))
                .thenReturn(0);
        int result = dao.deleteUserTimeManagement(1);
        assertEquals(0, result);
    }

    @Test
    void validUserAddUserExecutesSuccessfully() {
        User user = User.builder()
                .userId(1)
                .username("username")
                .firstName("firstname")
                .lastName("lastname")
                .password("password")
                .roleId(1)
                .isActive(1)
                .build();

        dao.addUser(user);

        verify(mockJdbcTemplate).update(eq("INSERT INTO Users (username, password, first_name, last_name, role_id, is_active) VALUES (?, ?, ?, ?, ?, ?)"),
                eq(user.getUsername()),
                eq(user.getPassword()),
                eq(user.getFirstName()),
                eq(user.getLastName()),
                eq(user.getRoleId()),
                eq(user.getIsActive()));
    }

    @Test
    void validUserIdAndBoardIdAddUserToBoardEmployeesExecutesSuccessfully() {

        dao.addUserToBoardEmployees(1, 1);

        verify(mockJdbcTemplate).update(eq("INSERT INTO BoardMembers (board_id, user_id, role_on_board) VALUES (?, ?, 'Employee')"),
                eq(1),
                eq(1));
    }

    @Test
    void validUserIdAndBoardIdAddUserToBoardOwnerExecutesSuccessfully() {

        dao.addUserToBoardOwner(1, 1);

        verify(mockJdbcTemplate).update(eq("INSERT INTO BoardMembers (board_id, user_id, role_on_board) VALUES (?, ?, 'Owner')"),
                eq(1),
                eq(1));
    }

    @Test
    void tagExistGetTagByIdReturnsTag() throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("tag_id")).thenReturn(1);
        when(rs.getInt("board_id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("tagname");
        when(rs.getInt("is_active")).thenReturn(1);
        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM Tags WHERE tag_id = 1"),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Tag> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });
        var result = dao.getTagById(1);
        //then
        assertTrue(result.isPresent());
        assertEquals("tagname", result.get().getName());
    }

    @Test
    void tagDoesNotExistGetTagByIdReturnsOptionalEmpty() throws Exception {

        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM Tags WHERE tag_id = 1"),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Tag> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });
        var result = dao.getTagById(1);
        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void notificationExistGetNotificationByIdReturnsNotification() throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("notification_id")).thenReturn(1);
        when(rs.getInt("user_id")).thenReturn(1);
        when(rs.getInt("task_id")).thenReturn(1);
        when(rs.getInt("is_seen")).thenReturn(0);
        //when
        when(mockJdbcTemplate.query(eq("SELECT *FROM Notifications WHERE Notifications.notification_id = 1"),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Notification> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });
        var result = dao.getNotificationById(1);
        //then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getNotificationId());
        assertEquals(1, result.get().getUserId());
        assertEquals(1, result.get().getTaskId());
        assertEquals(0, result.get().getIsSeen());
    }

    @Test
    void notificationDoesNotExistGetGetNotificationByIdReturnsOptionalEmpty() throws Exception {

        //when
        when(mockJdbcTemplate.query(eq("SELECT *FROM Notifications WHERE Notifications.notification_id = 1"),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Notification> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });
        var result = dao.getNotificationById(1);
        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void notificationExistGetNotificationByIdReturnsListOfNotifications() throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("notification_id")).thenReturn(1, 2);
        when(rs.getInt("user_id")).thenReturn(1, 2);
        when(rs.getInt("task_id")).thenReturn(1, 2);
        when(rs.getInt("is_seen")).thenReturn(0, 1);
        //when

        when(mockJdbcTemplate.query(eq("SELECT *FROM Notifications ;"),
                any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<Notification> rowMapper = invocation.getArgument(1);
                    List<Notification> notifications = new ArrayList<>();
                    while (rs.next()) {
                        Notification notification = rowMapper.mapRow(rs, rs.getRow());
                        notifications.add(notification);
                    }
                    return notifications;
                });
        var result = dao.getAllNotifications();
        //then
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void notificationDoesNotExistGetNotificationByIdReturnsOptionalEmpty() {

        //when
        when(mockJdbcTemplate.query(eq("SELECT *FROM Notifications ;"),
                any(RowMapper.class))).thenThrow(new EmptyResultDataAccessException(1));
        var result = dao.getAllNotifications();
        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void tmExistGetTimeManagementByIdReturnsTm() throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("TimeManagement_id")).thenReturn(1);
        when(rs.getInt("user_id")).thenReturn(1);
        when(rs.getString("date_time")).thenReturn("datetime");
        when(rs.getInt("task_id")).thenReturn(1);
        when(rs.getString("description")).thenReturn("description");
        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM TimeManagement WHERE TimeManagement_id = 1"),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<TimeManagement> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });
        var result = dao.getTimeManagementById(1);
        //then
        assertTrue(result.isPresent());
        assertEquals("datetime", result.get().getDateTime());
    }

    @Test
    void tmDoesNotExistGetTimeManagementByIdReturnsOptionalEmpty() throws Exception {

        //when
        when(mockJdbcTemplate.query(eq("SELECT * FROM TimeManagement WHERE TimeManagement_id = 1"),
                any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<TimeManagement> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });
        var result = dao.getTimeManagementById(1);
        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void validUpdateTmRequestUpdateUserTimeManagementExecutesSuccessfully() {
        int id = 1;
        String dateTime = "dateTime";
        int taskId = 1;
        String description = "description";
        Object[] params = {dateTime, taskId, description, id};
        dao.updateUserTimeManagement(id, dateTime, taskId, description);
        verify(mockJdbcTemplate).update(eq("UPDATE TimeManagement SET date_time = ?, task_id = ?, description = ? WHERE TimeManagement_id = ?"),
                eq(params));
    }

    @Test
    void validNotificationIdDeleteNotificationExecutesSuccessfully() {

        dao.deleteNotification(1);
        verify(mockJdbcTemplate).update(eq("DELETE FROM Notifications WHERE notification_id = ?"),
                eq(1));
    }

    @Test
    void validUpdateUserRequestUpdateUserTimeManagementExecutesSuccessfully() {

        dao.updateUser(1, "username", "firstName", "lastName", 1);
        verify(mockJdbcTemplate).update(eq("UPDATE Users " +
                "SET username = 'username', " +
                "first_name = 'firstName', " +
                "last_name = 'lastName', " +
                "is_active = '1' " +
                "WHERE user_id = 1"));
    }

    @Test
    void validTagIdActivateTagExecutesSuccessfully() {
        int tagId = 1;
        Object[] params = {tagId};
        dao.activateTag(tagId);
        verify(mockJdbcTemplate).update(eq("UPDATE Tags SET is_active = 1 WHERE tag_id = ?"),
                eq(params));
    }

    @Test
    void validBoardIdArchiveBoardExecutesSuccessfully() {
        int boardId = 1;
        Object[] params = {boardId};
        dao.archiveBoard(boardId);
        verify(mockJdbcTemplate).update(eq("UPDATE Boards SET is_archived = 1 WHERE board_id = ?"),
                eq(params));
    }

    @Test
    void validTaskIdAndStatusIdArchiveBoardExecutesSuccessfully() {
        int taskId = 1;
        String statusName = "To Do";
        Object[] params = {statusName, taskId};
        dao.updateTaskStatus(taskId, statusName);
        verify(mockJdbcTemplate).update(eq("UPDATE Tasks SET status = ? WHERE task_id = ?"),
                eq(params));
    }

    @Test
    void taskExistGetTaskTagsIdReturnsListOfTagIds() throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("tag_id")).thenReturn(1, 2);
        //when

        when(mockJdbcTemplate.query(eq("SELECT * FROM TaskTags WHERE task_id = ?"),
                any(RowMapper.class), eq(1)))
                .thenAnswer(invocation -> {
                    RowMapper<Integer> rowMapper = invocation.getArgument(1);
                    List<Integer> tagIds = new ArrayList<>();
                    while (rs.next()) {
                        Integer id = rowMapper.mapRow(rs, rs.getRow());
                        tagIds.add(id);
                    }
                    return tagIds;
                });
        var result = dao.getTaskTagsId(1);
        //then
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    void validTaskIdDeleteTaskTimeManagementExecutesSuccessfully() {

        dao.deleteTaskTimeManagement(1);
        verify(mockJdbcTemplate).update(eq("DELETE FROM TimeManagement WHERE task_id = ?"),
                eq(1));
    }

    @Test
    void validTaskIdDeleteTaskNotificationExecutesSuccessfully() {

        dao.deleteTaskNotification(1);
        verify(mockJdbcTemplate).update(eq("DELETE FROM Notifications WHERE task_id = ?"),
                eq(1));
    }

    @Test
    void validRequestGetTotalLoggedTimeForTaskOnDateExecutesSuccessfully() throws Exception {
        //given

        //when
        when(mockJdbcTemplate.queryForObject(eq("SELECT SUM(strftime('%H', date_time) * 60 + strftime('%M', date_time)) as totalMinutes " +
                "FROM TimeManagement " +
                "WHERE user_id = ? AND date(date_time) = date(?) AND TimeManagement_id <> ?"), eq(new Object[]{1, "date", 1}), any(RowMapper.class)))
                .thenReturn(10);
        var result = dao.getTotalLoggedTimeForTaskOnDate(1, "date", 1);
        //then
        assertEquals(10, result);
    }
}
