package ltd.future.planning.tech.DoPlus;

import com.fasterxml.jackson.databind.ObjectMapper;
import ltd.future.planning.tech.DoPlus.api.UpdateUserRequest;
import ltd.future.planning.tech.DoPlus.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Arrays;
import java.util.List;

import java.sql.ResultSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JdbcTemplate mockJdbcTemplate;

    //1 - /user/all - успешное получение списка всех активных пользователей с информацией о них
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void usersExistGetAllUsersReturnsOKAndListOfUsers() throws Exception {

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

        mockMvc.perform(get("/user/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[0].name", is("First1" + " " + "Last1")))
                .andExpect(jsonPath("$[1].username", is("user2")))
                .andExpect(jsonPath("$[1].name", is("First2" + " " + "Last2")));
    }

    //2 - /user/all - Ошибка при получении списка всех активных пользователей с информацией о них (NotFound)
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void usersDoNotExistGetAllUsersReturnsNotFound() throws Exception {

        when(mockJdbcTemplate.query(
                eq("SELECT * FROM Users"),
                any(RowMapper.class),
                eq(1)
        )).thenThrow(new EmptyResultDataAccessException(1));

        mockMvc.perform(get("/user/all", 1))
                .andExpect(status().isNotFound());
    }

    //3 - /board/{boardId}/tasks - успешное получение списка задач, связанных с конкретной доской
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void boardIdGivenGetBoardTasksReturnsOKAndListOfTasks() throws Exception {
        int boardId = 1;

        List<Task> mockedTasks = Arrays.asList(
                new Task(1, 2, 3, 1, "High", "Critical", 5, "Open", "Task 1 description", "Task 1", "2022-01-01"),
                new Task(2, 4, 5, 1, "Medium", "Major", 8, "In Progress", "Task 2 description", "Task 2", "2022-01-02")
        );

        when(mockJdbcTemplate.query(
                eq("SELECT * FROM Tasks WHERE board_id = ?"),
                any(RowMapper.class),
                eq(boardId)
        )).thenReturn(mockedTasks);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("user_id")).thenReturn(2);
        when(rs.getString("username")).thenReturn("user1");
        when(rs.getString("password")).thenReturn("pass1");
        when(rs.getString("first_name")).thenReturn("First1");
        when(rs.getString("last_name")).thenReturn("Last1");
        when(rs.getInt("role_id")).thenReturn(2);
        when(rs.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 2), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });

        ResultSet rs2 = mock(ResultSet.class);
        when(rs2.next()).thenReturn(true, false);
        when(rs2.getInt("user_id")).thenReturn(3);
        when(rs2.getString("username")).thenReturn("user2");
        when(rs2.getString("password")).thenReturn("pass2");
        when(rs2.getString("first_name")).thenReturn("First2");
        when(rs2.getString("last_name")).thenReturn("Last2");
        when(rs2.getInt("role_id")).thenReturn(2);
        when(rs2.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 3), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs2);
                });

        when(mockJdbcTemplate.query(eq("SELECT * FROM TaskTags WHERE task_id = ?"), any(RowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(10, 20));


        ResultSet rs3 = mock(ResultSet.class);
        when(rs3.next()).thenReturn(true, false);
        when(rs3.getInt("user_id")).thenReturn(4);
        when(rs3.getString("username")).thenReturn("user3");
        when(rs3.getString("password")).thenReturn("pass3");
        when(rs3.getString("first_name")).thenReturn("First3");
        when(rs3.getString("last_name")).thenReturn("Last3");
        when(rs3.getInt("role_id")).thenReturn(2);
        when(rs3.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 4), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs3);
                });

        ResultSet rs4 = mock(ResultSet.class);
        when(rs4.next()).thenReturn(true, false);
        when(rs4.getInt("user_id")).thenReturn(5);
        when(rs4.getString("username")).thenReturn("user4");
        when(rs4.getString("password")).thenReturn("pass4");
        when(rs4.getString("first_name")).thenReturn("First4");
        when(rs4.getString("last_name")).thenReturn("Last4");
        when(rs4.getInt("role_id")).thenReturn(2);
        when(rs4.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 5), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs4);
                });

        when(mockJdbcTemplate.query(eq("SELECT * FROM TaskTags WHERE task_id = ?"), any(RowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(10, 20));



        mockMvc.perform(get("/board/{boardId}/tasks", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(mockedTasks.size())))
                .andExpect(jsonPath("$[0].id", is(mockedTasks.get(0).getTaskId())))
                .andExpect(jsonPath("$[0].assignee", is("user1")))
                .andExpect(jsonPath("$[0].reporter", is("user2")))
                .andExpect(jsonPath("$[0].priority", is(mockedTasks.get(0).getPriority())))
                .andExpect(jsonPath("$[0].severity", is(mockedTasks.get(0).getSeverity())))
                .andExpect(jsonPath("$[0].estimation", is(mockedTasks.get(0).getEstimation())))
                .andExpect(jsonPath("$[0].status", is(mockedTasks.get(0).getStatus())))
                .andExpect(jsonPath("$[0].description", is(mockedTasks.get(0).getDescription())))
                .andExpect(jsonPath("$[1].id", is(mockedTasks.get(1).getTaskId())))
                .andExpect(jsonPath("$[1].assignee", is("user3")))
                .andExpect(jsonPath("$[1].reporter", is("user4")))
                .andExpect(jsonPath("$[1].priority", is(mockedTasks.get(1).getPriority())))
                .andExpect(jsonPath("$[1].severity", is(mockedTasks.get(1).getSeverity())))
                .andExpect(jsonPath("$[1].estimation", is(mockedTasks.get(1).getEstimation())))
                .andExpect(jsonPath("$[1].status", is(mockedTasks.get(1).getStatus())))
                .andExpect(jsonPath("$[1].description", is(mockedTasks.get(1).getDescription())));
    }

    //4 - /board/{boardId}/tasks - Ошибка при получении списка задач, связанных с конкретной доской (NotFound)
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void boardIdDoNotGivenGetBoardTasksReturnsNotFound() throws Exception {
        int boardId = 1;

        when(mockJdbcTemplate.query(
                eq("SELECT * FROM Tasks WHERE board_id = ?"),
                any(RowMapper.class),
                eq(boardId)
        )).thenThrow(new EmptyResultDataAccessException(1));

        mockMvc.perform(get("/board/{boardId}/tasks", boardId))
                .andExpect(status().isNotFound());
    }

    //5 - /board/{boardId}/managers - успешное получение списка менеджеров доски
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void managersExistGetBoardManagersReturnsOKAndEmployeeList() throws Exception {
        int boardId = 1;

        List<User> mockedManagers = Arrays.asList(
                new User(1, "manager1", "password", "Manager", "One", 2, 1),
                new User(2, "manager2", "password", "Manager", "Two", 2, 1)
        );

        when(mockJdbcTemplate.query(
                eq("SELECT u.* FROM Users u JOIN BoardMembers bm ON u.user_id = bm.user_id WHERE bm.board_id = ? AND bm.role_on_board = 'Manager'"),
                any(RowMapper.class),
                eq(boardId)
        )).thenReturn(mockedManagers);

        mockMvc.perform(get("/board/{boardId}/managers", boardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(mockedManagers.size())))
                .andExpect(jsonPath("$[0].username", is(mockedManagers.get(0).getUsername())))
                .andExpect(jsonPath("$[0].name", is(mockedManagers.get(0).getFirstName() + " " + mockedManagers.get(0).getLastName())))
                .andExpect(jsonPath("$[1].username", is(mockedManagers.get(1).getUsername())))
                .andExpect(jsonPath("$[1].name", is(mockedManagers.get(1).getFirstName() + " " + mockedManagers.get(1).getLastName())));
    }

    //6 - /board/{boardId}/managers - Ошибка при получении списка менеджеров доски (NotFound)
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void managersDoNotExistGetBoardManagersReturnsNotFound() throws Exception {
        int boardId = 1;

        when(mockJdbcTemplate.query(
                eq("SELECT u.* FROM Users u JOIN BoardMembers bm ON u.user_id = bm.user_id WHERE bm.board_id = ? AND bm.role_on_board = 'Manager'"),
                any(RowMapper.class),
                eq(boardId)
        )).thenThrow(new EmptyResultDataAccessException(1));

        mockMvc.perform(get("/board/{boardId}/managers", boardId))
                .andExpect(status().isNotFound());
    }

    //7 - /board/{boardId}/employees - Успешное получение списка сотрудников, привязанных к доске
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void employeesExistGetBoardEmployeesReturnsOKAndEmployeeList() throws Exception {
        int boardId = 1;

        List<User> mockedEmployees = Arrays.asList(
                new User(1, "employee1", "password", "Employee", "One", 2, 1),
                new User(2, "employee2", "password", "Employee", "Two", 2, 1)
        );

        when(mockJdbcTemplate.query(
                eq("SELECT u.* FROM Users u JOIN BoardMembers bm ON u.user_id = bm.user_id WHERE bm.board_id = ? AND bm.role_on_board = 'Employee'"),
                any(RowMapper.class),
                eq(boardId)
        )).thenReturn(mockedEmployees);

        mockMvc.perform(get("/board/{boardId}/employees", boardId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(mockedEmployees.size())))
                .andExpect(jsonPath("$[0].username", is(mockedEmployees.get(0).getUsername())))
                .andExpect(jsonPath("$[0].name", is(mockedEmployees.get(0).getFirstName() + " " + mockedEmployees.get(0).getLastName())))
                .andExpect(jsonPath("$[1].username", is(mockedEmployees.get(1).getUsername())))
                .andExpect(jsonPath("$[1].name", is(mockedEmployees.get(1).getFirstName() + " " + mockedEmployees.get(1).getLastName())));
    }

    //8 - /board/{boardId}/employees - Ошибка при получение списка сотрудников, привязанных к доске (NotFound)
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void employeesDoNotExistGetBoardEmployeesReturnsNotFound() throws Exception {
        int boardId = 1;

        when(mockJdbcTemplate.query(
                eq("SELECT u.* FROM Users u JOIN BoardMembers bm ON u.user_id = bm.user_id WHERE bm.board_id = ? AND bm.role_on_board = 'Employee'"),
                any(RowMapper.class),
                eq(boardId)
        )).thenThrow(new EmptyResultDataAccessException(1));

        mockMvc.perform(get("/board/{boardId}/employees", boardId))
                .andExpect(status().isNotFound());
    }

    //9 - /task/getbyid/{taskId} - успешное получение информации о задаче
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void taskExistsGetTaskByIdReturnsOKAndTaskInfo() throws Exception {
        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Tasks WHERE task_id = ?"), any(RowMapper.class), eq(1)))
                .thenReturn(new Task(1, 2, 3, 4, "High", "Medium", 5, "Open", "Test task description", "Test Task", "2023-01-01T12:00:00"));

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("user_id")).thenReturn(2);
        when(rs.getString("username")).thenReturn("user1");
        when(rs.getString("password")).thenReturn("pass1");
        when(rs.getString("first_name")).thenReturn("First1");
        when(rs.getString("last_name")).thenReturn("Last1");
        when(rs.getInt("role_id")).thenReturn(2);
        when(rs.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 2), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });

        ResultSet rs2 = mock(ResultSet.class);
        when(rs2.next()).thenReturn(true, false);
        when(rs2.getInt("user_id")).thenReturn(3);
        when(rs2.getString("username")).thenReturn("user2");
        when(rs2.getString("password")).thenReturn("pass2");
        when(rs2.getString("first_name")).thenReturn("First2");
        when(rs2.getString("last_name")).thenReturn("Last2");
        when(rs2.getInt("role_id")).thenReturn(2);
        when(rs2.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 3), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs2);
                });

        when(mockJdbcTemplate.query(eq("SELECT * FROM TaskTags WHERE task_id = ?"), any(RowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(10, 20));

        mockMvc.perform(get("/task/getbyid/{taskId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.assignee", is("user1")))
                .andExpect(jsonPath("$.reporter", is("user2")))
                .andExpect(jsonPath("$.tags", is(Arrays.asList(10, 20))))
                .andExpect(jsonPath("$.title", is("Test Task")))
                .andExpect(jsonPath("$.description", is("Test task description")))
                .andExpect(jsonPath("$.status", is("Open")));
    }

    //10 - /task/getbyid/{taskId} - Ошибка при получении информации о задаче (NotFound)
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void taskDoesNotExistGetTaskByIdReturnsNotFound() throws Exception {
        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Tasks WHERE task_id = ?"), any(RowMapper.class), eq(1)))
                .thenThrow(new EmptyResultDataAccessException(1));

        mockMvc.perform(get("/task/getbyid/{taskId}", 1))
                .andExpect(status().isNotFound());
    }

    // 11 - /notification/{userId}/all - успешное получение списка уведомлений для конкретного пользователя
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void notificationsExistGetUserNotificationsReturnsOk() throws Exception {

        List<Notification> mockedNotifications = Arrays.asList(
                new Notification(1, 1, 1, 0),
                new Notification(2, 1, 2, 1)
        );

        String sql = "SELECT *" +
                "FROM Notifications " +
                "WHERE Notifications.user_id = ?;";

        when(mockJdbcTemplate.query(
                eq(sql),
                any(RowMapper.class),
                eq(1)
        )).thenReturn(mockedNotifications);


        mockMvc.perform(get("/notification/{userId}/all", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(mockedNotifications.size())))
                .andExpect(jsonPath("$[0].id", is(mockedNotifications.get(0).getNotificationId())))
                .andExpect(jsonPath("$[0].taskId", is(mockedNotifications.get(0).getTaskId())))
                .andExpect(jsonPath("$[0].seen", is(mockedNotifications.get(0).getIsSeen() == 1)))
                .andExpect(jsonPath("$[1].id", is(mockedNotifications.get(1).getNotificationId())))
                .andExpect(jsonPath("$[1].taskId", is(mockedNotifications.get(1).getTaskId())))
                .andExpect(jsonPath("$[1].seen", is(mockedNotifications.get(1).getIsSeen() == 1)));
    }


    // 12 - /notification/{userId}/all -  Ошибка при получении списка уведомлений для конкретного пользователя (NotFound)

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void notificationsDoNotExistGetUserNotificationsReturnsNotFound() throws Exception {

        String sql = "SELECT *" +
                "FROM Notifications " +
                "WHERE Notifications.user_id = ?;";

        when(mockJdbcTemplate.query(eq(sql), any(RowMapper.class), eq(1)))
            .thenThrow(new EmptyResultDataAccessException(1));
        mockMvc.perform(get("/notification/{userId}/all", 1))
                .andExpect(status().isNotFound());
    }


    // 13 - /tm/{userId} - успешное получение данных о затраченном времени для конкретного пользователя

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void timeManagementExistsGetTimeManagementDataReturnsOk() throws Exception {

        List<TimeManagement> mockedTM = Arrays.asList(
                new TimeManagement(1,1,"2023-01-01T12:00:00",1, "description"),
                new TimeManagement(2,1,"2023-01-02T12:00:00",2, "description2")
        );

        String sql = "SELECT * FROM TimeManagement WHERE user_id = ?";

        when(mockJdbcTemplate.query(
                eq(sql),
                any(RowMapper.class),
                eq(1)
        )).thenReturn(mockedTM);


        mockMvc.perform(get("/tm/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(mockedTM.size())))
                .andExpect(jsonPath("$[0].id", is(mockedTM.get(0).getTimeManagementId())))
                .andExpect(jsonPath("$[0].dateTime", is(mockedTM.get(0).getDateTime())))
                .andExpect(jsonPath("$[0].taskId", is(mockedTM.get(0).getTaskId())))
                .andExpect(jsonPath("$[0].description", is(mockedTM.get(0).getDescription())))
                .andExpect(jsonPath("$[1].id", is(mockedTM.get(1).getTimeManagementId())))
                .andExpect(jsonPath("$[1].dateTime", is(mockedTM.get(1).getDateTime())))
                .andExpect(jsonPath("$[1].taskId", is(mockedTM.get(1).getTaskId())))
                .andExpect(jsonPath("$[1].description", is(mockedTM.get(1).getDescription())));
    }

    // 14 - /tm/{userId} - Ошибка при получении данных о затраченном времени для конкретного пользователя (NotFound)

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void timeManagementDoesNotExistGetTimeManagementDataReturnsNotFound() throws Exception {

        String sql = "SELECT * FROM TimeManagement WHERE user_id = ?";

        when(mockJdbcTemplate.query(eq(sql), any(RowMapper.class), eq(1)))
                .thenThrow(new EmptyResultDataAccessException(1));
        mockMvc.perform(get("/tm/{userId}", 1))
                .andExpect(status().isNotFound());
    }

    // 15 - /admin/updateuser/{userId} - успешное обновление информации о пользователе

    @Test
    @WithMockUser(username = "testRootadmin", roles = {"ROOTADMIN"})
    void userExistsUpdateUserReturnsOk() throws Exception {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("username", "firstName", "lastName", 1);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("user_id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("username");
        when(rs.getString("password")).thenReturn("pass1");
        when(rs.getString("first_name")).thenReturn("firstName");
        when(rs.getString("last_name")).thenReturn("lastName");
        when(rs.getInt("role_id")).thenReturn(1);
        when(rs.getInt("is_active")).thenReturn(1);

        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 1), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });


        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE username = '" + updateUserRequest.username()+  "'" ), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });

        when(mockJdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);


        mockMvc.perform(put("/admin/updateuser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated user"));
    }

    //16 /admin/updateuser/{userId} - При ошибке обновления информации о пользователе (NotFound)
    @Test
    @WithMockUser(username = "testRootadmin", roles = {"ROOTADMIN"})
    void userDoesNotExistUpdateUserReturnsNotFound() throws Exception {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("username", "firstName", "lastName", 1);
        when(mockJdbcTemplate.query(eq("SELECT * " +
                "FROM Users " +
                "WHERE user_id = " + 1), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<User> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });
        mockMvc.perform(put("/admin/updateuser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                .andExpect(status().isNotFound());
    }

    //17 /board/{boardId}/archive - успешное архивация доски (изменение статуса isArchived)
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void boardExistArchiveBoardReturnsOK() throws Exception {

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("board_id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("name");
        when(rs.getInt("is_archived")).thenReturn(0);
        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Boards WHERE board_id = ?"), any(RowMapper.class), anyInt()))
                .thenAnswer(invocation -> {
                    RowMapper<Board> rowMapper = invocation.getArgument(1);
                    if (rs.next()) {
                        return rowMapper.mapRow(rs, rs.getRow());
                    }
                    throw new Exception();
                });
        mockMvc.perform(put("/board/1/archive").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockJdbcTemplate).update(eq("UPDATE Boards SET is_archived = 1 WHERE board_id = ?"),
                any(Object.class));
    }

    //18 /board/{boardId}/archive- При ошибке архивации доски (изменение статуса isArchived) (NotFound)
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void boardDoesNotExistArchiveBoardReturnsNotFound() throws Exception {

        when(mockJdbcTemplate.queryForObject(eq("SELECT * FROM Boards WHERE board_id = ?"), any(RowMapper.class), eq(1)))
                .thenThrow(new EmptyResultDataAccessException(1));
        mockMvc.perform(put("/board/1/archive").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    //19 /task/{taskId}/update - успешное обновление информации о задаче
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void validRequestUpdateTaskReturnsOk() throws Exception {
        String jsonRequest = """
                {
                    "title": "Updated Task Title",
                    "assigneeId": 2,
                    "reporterId": 3,
                    "priority": "High",
                    "severity": "Critical",
                    "estimation": 5,
                    "status": "In Progress",
                    "description": "Updated task description",
                    "tags": [1, 2, 3]
                }
                """;

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
        when(rs.getString("status")).thenReturn("In Progress");
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
        when(mockJdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated task"));
    }

    //20 /task/{taskId}/update - граничные условия (==0, ==1, >1440, <1440, ==1440)
    // >1440
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void descriptionExceedsLimitUpdateTaskReturnsBadRequest() throws Exception {
        String longDescription = "d".repeat(1441);
        String jsonRequest = """
                {
                    "title": "Valid Title",
                    "assigneeId": 2,
                    "reporterId": 3,
                    "priority": "Medium",
                    "severity": "Moderate",
                    "estimation": 10,
                    "status": "Open",
                    "description": "%s",
                    "tags": [1, 2]
                }
                """.formatted(longDescription);

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
        when(rs.getString("status")).thenReturn("In Progress");
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

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request data"));
    }

    // <1440
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void descriptionLessThanLimitUpdateTaskReturnsOk() throws Exception {
        String notTatLongDescription = "d".repeat(1439);
        String jsonRequest = """
                {
                    "title": "Valid Title",
                    "assigneeId": 2,
                    "reporterId": 3,
                    "priority": "Medium",
                    "severity": "Moderate",
                    "estimation": 10,
                    "status": "Open",
                    "description": "%s",
                    "tags": [1, 2]
                }
                """.formatted(notTatLongDescription);

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
        when(rs.getString("status")).thenReturn("In Progress");
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

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated task"));
    }

    // ==1440
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void descriptionEqualsLimitUpdateTaskReturnsBadRequest() throws Exception {
        String notTatLongDescription = "d".repeat(1440);
        String jsonRequest = """
                {
                    "title": "Valid Title",
                    "assigneeId": 2,
                    "reporterId": 3,
                    "priority": "Medium",
                    "severity": "Moderate",
                    "estimation": 10,
                    "status": "Open",
                    "description": "%s",
                    "tags": [1, 2]
                }
                """.formatted(notTatLongDescription);

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
        when(rs.getString("status")).thenReturn("In Progress");
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

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request data"));
    }

    // ==0
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void emptyDescriptionUpdateTaskReturnsOk() throws Exception {
        String jsonRequest = """
                {
                    "title": "Valid Title",
                    "assigneeId": 2,
                    "reporterId": 3,
                    "priority": "Medium",
                    "severity": "Moderate",
                    "estimation": 10,
                    "status": "Open",
                    "description": "",
                    "tags": [1, 2]
                }
                """;

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
        when(rs.getString("status")).thenReturn("In Progress");
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

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated task"));
    }

    // ==1
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void descriptionOneLengthUpdateTaskReturnsOk() throws Exception {
        String jsonRequest = """
                {
                    "title": "Valid Title",
                    "assigneeId": 2,
                    "reporterId": 3,
                    "priority": "Medium",
                    "severity": "Moderate",
                    "estimation": 10,
                    "status": "Open",
                    "description": "1",
                    "tags": [1, 2]
                }
                """;

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
        when(rs.getString("status")).thenReturn("In Progress");
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

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated task"));
    }
}
