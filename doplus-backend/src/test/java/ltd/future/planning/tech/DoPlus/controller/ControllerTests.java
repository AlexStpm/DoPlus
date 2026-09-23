package ltd.future.planning.tech.DoPlus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ltd.future.planning.tech.DoPlus.api.*;

import ltd.future.planning.tech.DoPlus.entity.Board;
import ltd.future.planning.tech.DoPlus.service.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ltd.future.planning.tech.DoPlus.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@ExtendWith(MockitoExtension.class)
public class ControllerTests {

    private MockMvc mockMvc;

    @Mock
    private Service mockService;
    @InjectMocks
    private Controller controller;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /*************** GET METHOD TESTS ***************/
    //1 - /admin/getallusers
    @Test
    void usersExistsGetAllUsersForAdminReturnsOK() throws Exception {
        //given
        List<AdminUserView> mockUserViews = List.of(
                new AdminUserView(1, "username", "firstName", "lastName", true),
                new AdminUserView(2, "username", "firstName", "lastName", true)
        );
        //when
        when(mockService.getAllUsersForAdmin()).thenReturn(Optional.of(mockUserViews));
        mockMvc.perform(get("/admin/getallusers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockUserViews.size()));
        //then
        verify(mockService, times(1)).getAllUsersForAdmin();
    }

    @Test
    void usersDoesNotExistsGetAllUsersForAdminReturnsNotFound() throws Exception {
        //given no users
        //when
        when(mockService.getAllUsersForAdmin()).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/getallusers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).getAllUsersForAdmin();
    }

    //2 - /user/all
    @Test
    public void usersExistGetAllUsersReturnsListOfUsersViews() throws Exception {
        UserView userView1 = UserView.builder().id(1).username("user1").name("User One").build();
        UserView userView2 = UserView.builder().id(2).username("user2").name("User Two").build();
        List<UserView> userViews = Arrays.asList(userView1, userView2);

        when(mockService.getAllUsers()).thenReturn(Optional.of(userViews));

        mockMvc.perform(get("/user/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(userViews.size()));

        verify(mockService, times(1)).getAllUsers();
    }

    @Test
    public void usersDoNotExistGetAllUsersReturnsNotFound() throws Exception {
        when(mockService.getAllUsers()).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/all").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).getAllUsers();
    }



     //3 - /user/getbyid/{userId}
    @Test
    void userExistsGetByIdReturnsOk() throws Exception {

        UserView mockUserView = new UserView(1,"username","name");

        when(mockService.getUserById(1)).thenReturn(Optional.of(mockUserView));
        mockMvc.perform(get("/user/getbyid/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockService, times(1)).getUserById(1);
    }

    @Test
    void userDoesNotExistsGetByIdReturnsNotFound() throws Exception {


        when(mockService.getUserById(1)).thenReturn(Optional.empty());
        mockMvc.perform(get("/user/getbyid/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).getUserById(1);
    }

    //4 - /board/getuserboards/{userId}
    @Test
    void BoardsExistsGetUserBoardsReturnsOK() throws Exception {
        List<BoardView> mockBoardViews = List.of(
                new BoardView(1, "board1",false),
                new BoardView(2, "board1", true));

        when(mockService.getUserBoards(anyInt())).thenReturn(Optional.of(mockBoardViews));

        mockMvc.perform(get("/user/getuserboards/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockBoardViews.size()));

        verify(mockService, times(1)).getUserBoards(1);
    }

    @Test
    void boardsDoesNotExistsGetUserBoardsReturnsNotFound() throws Exception {
        int userId = 1;

        when(mockService.getUserBoards(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/getuserboards/{userId}", userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).getUserBoards(userId);
    }

    //5 - /board/{boardId}/info
    @Test
    void boardExistsGetBoardReturnsOK() throws Exception {
        //given
        BoardView mockBoardView = new BoardView(1, "name", false);
        //when
        when(mockService.getBoard(1)).thenReturn(Optional.of(mockBoardView));
        mockMvc.perform(get("/board/1/info").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //then
        verify(mockService, times(1)).getBoard(1);
    }

    @Test
    void boardDoesNotExistsGetBoardReturnsNotFound() throws Exception {
        //given no board
        //when
        when(mockService.getBoard(anyInt())).thenReturn(Optional.empty());
        mockMvc.perform(get("/board/1/info").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).getBoard(anyInt());
    }


    //6 - /board/{boardId}/tasks
    @Test
    public void tasksExistForBoardGetBoardTasksReturnsListOfTasksViews() throws Exception {
        int boardId = 1;
        TaskView task1 = TaskView.builder().id(1).title("Task 1").assignee("User 1").reporter("User 2").priority("High").severity("Medium").estimation(5).status("Open").description("Description 1").tags(Arrays.asList(1, 2)).creationTime("2021-01-01").boardId(boardId).build();
        TaskView task2 = TaskView.builder().id(2).title("Task 2").assignee("User 3").reporter("User 4").priority("Medium").severity("Low").estimation(3).status("Closed").description("Description 2").tags(Arrays.asList(3, 4)).creationTime("2021-02-01").boardId(boardId).build();
        List<TaskView> taskViews = Arrays.asList(task1, task2);

        when(mockService.getBoardTasks(boardId)).thenReturn(Optional.of(taskViews));

        mockMvc.perform(get("/board/{boardId}/tasks", boardId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(taskViews.size()));

        verify(mockService, times(1)).getBoardTasks(boardId);
    }

    @Test
    public void tasksDoNotExistForBoardGetBoardTasksReturnsNotFound() throws Exception {
        int boardId = 1;

        when(mockService.getBoardTasks(boardId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/board/{boardId}/tasks", boardId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).getBoardTasks(boardId);
    }

      //7 /board/{boardId}/owner
    @Test
    void ownerExistsGetBoardOwnerReturnsOk() throws Exception {

        UserView mockOwnerView = new UserView(1,"username","name");

        when(mockService.getBoardOwner(1)).thenReturn(Optional.of(mockOwnerView));
        mockMvc.perform(get("/board/1/owner").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockService, times(1)).getBoardOwner(1);
    }

    @Test
    void ownerDoesNot0ExistsGetBoardOwnerReturnsNotFound() throws Exception {


        when(mockService.getBoardOwner(1)).thenReturn(Optional.empty());
        mockMvc.perform(get("/board/1/owner").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).getBoardOwner(1);
    }

    //8 - /board/{boardId}/managers
    @Test
    void managersExistsGetBoardManagersReturnsOK() throws Exception {
        //given
        List<UserView> mockUserViews = List.of(
                new UserView(1, "username1", "firstName1 lastName1"),
                new UserView(2, "username2", "firstName2 lastName2")
        );
        //when
        when(mockService.getBoardManagers(1)).thenReturn(Optional.of(mockUserViews));
        mockMvc.perform(get("/board/1/managers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockUserViews.size()));
        //then
        verify(mockService, times(1)).getBoardManagers(1);
    }

    @Test
    void managersDoesNotExistsGetBoardManagersReturnsNotFound() throws Exception {
        //given no user
        //when
        when(mockService.getBoardManagers(1)).thenReturn(Optional.empty());
        mockMvc.perform(get("/board/1/managers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).getBoardManagers(1);
    }

    //9 - /board/{boardId}/employees
    @Test
    void employeesExistsGetBoardEmployeesReturnsOK() throws Exception {
        //given
        List<UserView> mockUserViews = List.of(
                new UserView(1, "username1", "firstName1 lastName1"),
                new UserView(2, "username2", "firstName2 lastName2")
        );
        //when
        when(mockService.getBoardEmployees(1)).thenReturn(Optional.of(mockUserViews));
        mockMvc.perform(get("/board/1/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockUserViews.size()));
        //then
        verify(mockService, times(1)).getBoardEmployees(1);
    }

    @Test
    void employeesDoesNotExistsGetBoardEmployeesReturnsNotFound() throws Exception {
        //given no user
        //when
        when(mockService.getBoardEmployees(1)).thenReturn(Optional.empty());
        mockMvc.perform(get("/board/1/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).getBoardEmployees(1);
    }


    //10 - /board/{boardId}/tags
    @Test
    void boardIdGivenGetBoardTagsReturnsListOfTagsViews() throws Exception {
        int boardId = 1;
        List<TagView> tags = Arrays.asList(new TagView(1, "Tag1", true), new TagView(2, "Tag2", true));

        when(mockService.getBoardTags(boardId)).thenReturn(Optional.of(tags));

        mockMvc.perform(get("/board/{boardId}/tags", boardId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(tags.size()));
    }

    @Test
    void boardIdWithNoTagsGivenGetBoardTagsReturnsNotFound() throws Exception {
        int boardId = 1;

        when(mockService.getBoardTags(boardId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/board/{boardId}/tags", boardId))
                .andExpect(status().isNotFound());
      }
      //11 - /task/getbyid/{taskId}
    @Test
    void taskExistsGetTaskByIdReturnsOk() throws Exception {

        TaskView mockTaskView = new TaskView(1,"title","name","reporter","high",
                "high",1,"IN WORK","descr",List.of(1),"01.01.99",1);

        when(mockService.getTaskById(1)).thenReturn(Optional.of(mockTaskView));
        mockMvc.perform(get("/task/getbyid/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockService, times(1)).getTaskById(1);
    }

    @Test
    void taskDoesNotExistsGetTaskByIdReturnsNotFound() throws Exception {


        when(mockService.getTaskById(1)).thenReturn(Optional.empty());
        mockMvc.perform(get("/task/getbyid/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).getTaskById(1);
    }

        //12 - /notification/{userId}/all
        @Test
        void notificationsExistsGetUserNotificationsReturnsOK() throws Exception {
            //given
            List<NotificationView> mockNotificationViews = List.of(
                    new NotificationView(1, 1, true),
                    new NotificationView(2, 2, false)
            );
            //when
            when(mockService.getUserNotifications(1)).thenReturn(Optional.of(mockNotificationViews));
            mockMvc.perform(get("/notification/1/all").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(mockNotificationViews.size()));
            //then
            verify(mockService, times(1)).getUserNotifications(1);
        }

        @Test
        void notificationDoesNotExistsGetUserNotificationReturnsNotFound() throws Exception {
            //given no notifications
            //when
            when(mockService.getUserNotifications(anyInt())).thenReturn(Optional.empty());
            mockMvc.perform(get("/notification/1/all").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
            //then
            verify(mockService, times(1)).getUserNotifications(anyInt());
        }

    //13 - /tm/{userId}
    @Test
    void tmExistsGetTimeManagementDataReturnsOK() throws Exception {
        //given
        List<TimeManagementView> mockTimeManagementViews = List.of(
                new TimeManagementView(1, "dateTime", 1, "description"),
                new TimeManagementView(1, "dateTime", 1, "description")
        );
        //when
        when(mockService.getTimeManagementData(1)).thenReturn(Optional.of(mockTimeManagementViews));
        mockMvc.perform(get("/tm/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockTimeManagementViews.size()));
        //then
        verify(mockService, times(1)).getTimeManagementData(1);
    }

    @Test
    void tmDoesNotExistsGetTimeManagementDataReturnsNotFound() throws Exception {
        //given no board
        //when
        when(mockService.getTimeManagementData(anyInt())).thenReturn(Optional.empty());
        mockMvc.perform(get("/tm/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).getTimeManagementData(anyInt());
    }

    /*************** POST METHOD TESTS ***************/

    //3 - /board/add/{userId}
    @Test
    void boardNameUnder140AddBoardReturnsCreated() throws Exception {

        when(mockService.addBoard("name", 1)).thenReturn(Optional.of(1));
        mockMvc.perform(post("/board/add/1").contentType(MediaType.APPLICATION_JSON).content("name"))
                .andExpect(status().isCreated());

        verify(mockService, times(1)).addBoard(anyString(), anyInt());
    }

    @Test
    void boardNameOverflows140AddBoardReturnsBadRequest() throws Exception {

        String longName = "namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamen";
        when(mockService.addBoard(anyString(), anyInt())).thenReturn(Optional.empty());
        mockMvc.perform(post("/board/add/1").contentType(MediaType.APPLICATION_JSON).content(longName))
                .andExpect(status().isBadRequest());

        verify(mockService, times(1)).addBoard(anyString(), anyInt());
    }


    //4 - /board/{boardId}/managers/{userId}/add
    @Test
    void userIsNotOnBoardAddUserToBoardManagersReturnsCreated() throws Exception {
        //given user does not have role on board
        //when
        when(mockService.addUserToBoardManagers(1, 1)).thenReturn(true);
        mockMvc.perform(post("/board/1/managers/1/add").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        //then
        verify(mockService, times(1)).addUserToBoardManagers(anyInt(), anyInt());
    }

    @Test
    void userIsOnBoardAddUserToBoardManagersReturnsConflict() throws Exception {
        //given user does not have role on board
        //when
        when(mockService.addUserToBoardManagers(anyInt(), anyInt())).thenReturn(false);
        mockMvc.perform(post("/board/1/managers/1/add").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        //then
        verify(mockService, times(1)).addUserToBoardManagers(anyInt(), anyInt());
    }

    //5 - /board/{boardId}/employees/{userId}/add
    @Test
    void userIsNotOnBoardAddUserToBoardEmployeesReturnsCreated() throws Exception {
        //given user does not have role on board
        //when
        when(mockService.addUserToBoardEmployees(1, 1)).thenReturn(true);
        mockMvc.perform(post("/board/1/employees/1/add").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        //then
        verify(mockService, times(1)).addUserToBoardEmployees(anyInt(), anyInt());
    }

    @Test
    void userIsOnBoardAddUserToBoardEmployeesReturnsConflict() throws Exception {
        //given user does not have role on board
        //when
        when(mockService.addUserToBoardEmployees(anyInt(), anyInt())).thenReturn(false);
        mockMvc.perform(post("/board/1/employees/1/add").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        //then
        verify(mockService, times(1)).addUserToBoardEmployees(anyInt(), anyInt());
    }

    //6 - /board/tag/add
    @Test
    public void validTagAndBoardAddTagToBoardReturnsOk() throws Exception {
        // Создание успешного ответа
        ResponseEntity<String> responseEntity = ResponseEntity.ok("Added tag to board");
        doReturn(responseEntity).when(mockService).addTagToBoard(anyInt(), anyString());

        mockMvc.perform(post("/board/tag/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardId\":1,\"name\":\"New Tag\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Added tag to board"));
    }

    @Test
    public void tagLengthExceedsLimitAddTagToBoardReturnsBadRequest() throws Exception {
        ResponseEntity<String> responseEntity = ResponseEntity.badRequest().body("Tag length should be not more than 140 characters");
        doReturn(responseEntity).when(mockService).addTagToBoard(anyInt(), anyString());

        mockMvc.perform(post("/board/tag/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardId\":1,\"name\":\"" + "a".repeat(141) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tag length should be not more than 140 characters"));
    }

    //8 - /notification/sent
    @Test
    void createdNotificationAddNotificationReturnsCreated() throws Exception {

        CreateNotificationRequest mockNotificationRequest = new CreateNotificationRequest(1, 1);
        when(mockService.addNotification(mockNotificationRequest)).thenReturn(true);
        mockMvc.perform(post("/notification/sent").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockNotificationRequest)))
                .andExpect(status().isCreated());

        verify(mockService, times(1)).addNotification(any());
    }

    @Test
    void notificationRecreatedAddNotificationReturnsOk() throws Exception {

        CreateNotificationRequest mockNotificationRequest = new CreateNotificationRequest(1 , 1);
        when(mockService.addNotification(mockNotificationRequest)).thenReturn(false);
        mockMvc.perform(post("/notification/sent").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockNotificationRequest)))
                .andExpect(status().isOk());

        verify(mockService, times(1)).addNotification(any());
    }
    //9 - /tm/add
    @Test
    public void validRequestAddUserTimeManagementReturnsOk() throws Exception {
        String jsonRequest = """
            {
                "userId": 1,
                "dateTime": "2023-01-01 10:00",
                "taskId": 1,
                "description": "Worked on task"
            }
            """;

        doReturn(ResponseEntity.ok().body("Added time management record"))
                .when(mockService).addUserTimeManagement(anyInt(), anyString(), anyInt(), anyString());

        mockMvc.perform(post("/tm/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Added time management record"));
    }

    @Test
    public void descriptionExceedsLimitAddUserTimeManagementReturnsBadRequest() throws Exception {
        String jsonRequest = """
            {
                "userId": 1,
                "dateTime": "2023-01-01 10:00",
                "taskId": 1,
                "description": "This description is way too long and should cause the server to return a BadRequest response because it exceeds the 140 characters limit set by the application logic."
            }
            """;

        doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Description length exceeds 140 characters"))
                .when(mockService).addUserTimeManagement(anyInt(), anyString(), anyInt(), anyString());

        mockMvc.perform(post("/tm/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Description length exceeds 140 characters"));
    }

    @Test
    public void taskDoesNotExistAddUserTimeManagementReturnsBadRequest() throws Exception {
        String jsonRequest = """
            {
                "userId": 1,
                "dateTime": "2023-01-01 10:00",
                "taskId": 999,
                "description": "Attempt to log time for a non-existent task."
            }
            """;

        doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task does not exist"))
                .when(mockService).addUserTimeManagement(anyInt(), anyString(), anyInt(), anyString());

        mockMvc.perform(post("/tm/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Task does not exist"));
    }

    @Test
    public void totalTimeExceeds24HoursAddUserTimeManagementReturnsBadRequest() throws Exception {
        String jsonRequest = """
        {
            "userId": 1,
            "dateTime": "2023-01-01 10:00",
            "taskId": 1,
            "description": "Attempt to log time that exceeds total 24 hours for the day."
        }
        """;

        doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Total logged time exceeds 24 hours for the day"))
                .when(mockService).addUserTimeManagement(anyInt(), anyString(), anyInt(), anyString());

        mockMvc.perform(post("/tm/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Total logged time exceeds 24 hours for the day"));
    }

    //7 - /task/add/
    @Test
    void fetchedTaskAddTaskToBoardReturnsCreated() throws Exception {

        CreateTaskRequest mockTaskRequest = new CreateTaskRequest("title",1,1,1,
                "high","high",1,"IN_WORK","description",List.of(1),"01.01.99");
        when(mockService.addTaskToBoard(mockTaskRequest)).thenReturn(Optional.of(1));
        mockMvc.perform(post("/task/add/").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockTaskRequest)))
                .andExpect(status().isCreated());

        verify(mockService, times(1)).addTaskToBoard(any());
    }

    @Test
    void failedToFetchTaskAddTaskToBoardReturnsBadRequest() throws Exception {

        CreateTaskRequest mockTaskRequest = new CreateTaskRequest("title",1,1,1,
                "high","high",1,"IN_WORK","description",List.of(1),"01.01.99");
        when(mockService.addTaskToBoard(mockTaskRequest)).thenReturn(Optional.empty());
        mockMvc.perform(post("/task/add/").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockTaskRequest)))
                .andExpect(status().isBadRequest());

        verify(mockService, times(1)).addTaskToBoard(any());
    }

    /*************** PUT METHOD TESTS ***************/
    //1 - /admin/updateuser/{userId}
    @Test
    void userExistAndUniqueUsernameGivenUpdateUserReturnsOK() throws Exception {
        //given user exists and new username is not present in database
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("username", "firstName", "lastName", 1);
        //when
        when(mockService.updateUser(anyInt(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        mockMvc.perform(put("/admin/updateuser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk());
        //then
        verify(mockService, times(1)).updateUser(anyInt(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void userExistAndIsNotUniqueUsernameGivenUpdateUserReturnsConflict() throws Exception {
        //given user exists and new username is present in database
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("username", "firstName", "lastName", 1);
        //when
        when(mockService.updateUser(anyInt(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).build());
        mockMvc.perform(put("/admin/updateuser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                .andExpect(status().isConflict());
        //then
        verify(mockService, times(1)).updateUser(anyInt(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void userDoesNotExistUpdateUserReturnsNotFound() throws Exception {
        //given user does not exist
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("username", "firstName", "lastName", 1);
        //when
        when(mockService.updateUser(anyInt(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        mockMvc.perform(put("/admin/updateuser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateUserRequest)))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).updateUser(anyInt(), anyString(), anyString(), anyString(), anyInt());
    }

    //2 - /board/{boardId}/update
    @Test
    void validBoardIdAndNameGivenUpdateBoardReturnsOk() throws Exception {
        int boardId = 1;
        String name = "New Board Name";

        doReturn(ResponseEntity.ok().body("Updated board")).when(mockService).updateBoard(eq(1), eq("New Board Name"));


        mockMvc.perform(put("/board/{boardId}/update", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(name))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated board"));
    }

    @Test
    void nameExceedsLengthUpdateBoardReturnsBadRequest() throws Exception {
        int boardId = 1;
        String longName = "a".repeat(141);

        doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Name must be non-empty and up to 140 characters long."))
                .when(mockService).updateBoard(eq(boardId), eq(longName));

        mockMvc.perform(put("/board/{boardId}/update", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(longName))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name must be non-empty and up to 140 characters long."));
    }

    @Test
    void boardDoesNotExistUpdateBoardReturnsNotFound() throws Exception {
        int boardId = 999;
        String name = "Non-existent Board";

        doReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build()).when(mockService).updateBoard(eq(boardId), eq(name));

        mockMvc.perform(put("/board/{boardId}/update", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(name))
                .andExpect(status().isNotFound());
    }
      //3 - /board/tag/{tagId}/activate
    @Test
    void tagExistActivateTagReturnsOK() throws Exception {

        when(mockService.activateTag(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        mockMvc.perform(put("/board/tag/1/activate").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockService, times(1)).activateTag(anyInt());
    }

    @Test
    void tagDoesNotExistActivateTagReturnsNotFound() throws Exception {

        when(mockService.activateTag(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        mockMvc.perform(put("/board/tag/1/activate").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).activateTag(anyInt());
    }

    //4- board/tag/{tagId}/deactivate
    @Test
    void tagExistDeactivateTagReturnsOK() throws Exception {

        when(mockService.deactivateTag(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        mockMvc.perform(put("/board/tag/1/deactivate").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockService, times(1)).deactivateTag(anyInt());
    }

    @Test
    void tagDoesNotExistDeactivateTagReturnsNotFound() throws Exception {

        when(mockService.deactivateTag(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        mockMvc.perform(put("/board/tag/1/deactivate").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).deactivateTag(anyInt());
    }

    //5 - /board/{boardId}/archive
    @Test
    void boardExistArchiveBoardReturnsOK() throws Exception {
        //given board exist
        //when
        when(mockService.archiveBoard(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        mockMvc.perform(put("/board/1/archive").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //then
        verify(mockService, times(1)).archiveBoard(anyInt());
    }

    @Test
    void boardDoesNotExistArchiveBoardReturnsNotFound() throws Exception {
        //given board does not exist
        //when
        when(mockService.archiveBoard(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        mockMvc.perform(put("/board/1/archive").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).archiveBoard(anyInt());
    }

    //6 - /task/{taskId}/update
    @Test
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

        doReturn(ResponseEntity.ok().body("Task updated successfully"))
                .when(mockService)
                .updateTask(eq(1), any(UpdateTaskRequest.class));

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Task updated successfully"));
    }

    @Test
    public void descriptionExceedsLimitUpdateTaskReturnsBadRequest() throws Exception {
        String jsonRequest = """
        {
            "title": "Valid Title",
            "assigneeId": 2,
            "reporterId": 3,
            "priority": "Medium",
            "severity": "Moderate",
            "estimation": 10,
            "status": "Open",
            "description": "Description over 1440 characters",
            "tags": [1, 2]
        }
        """;

        doReturn(ResponseEntity.badRequest().body("Invalid request data"))
                .when(mockService)
                .updateTask(eq(1), any(UpdateTaskRequest.class));

        mockMvc.perform(put("/task/{taskId}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request data"));
    }

    @Test
    public void taskDoesNotExistUpdateTaskReturnsNotFound() throws Exception {
        String jsonRequest = """
        {
            "title": "Nonexistent Task",
            "assigneeId": 2,
            "reporterId": 3,
            "priority": "Low",
            "severity": "Minor",
            "estimation": 8,
            "status": "Completed",
            "description": "This task does not exist in the database.",
            "tags": [4, 5, 6]
        }
        """;

        doReturn(ResponseEntity.notFound().build())
                .when(mockService)
                .updateTask(eq(999), any(UpdateTaskRequest.class));

        mockMvc.perform(put("/task/{taskId}/update", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

      //7 - /task/{taskId}/changestatus/{statusname}
    @Test
    void taskExistChangeStatusReturnsOK() throws Exception {

        when(mockService.updateTaskStatus(anyInt(),anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        mockMvc.perform(put("/task/1/changestatus/name").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(mockService, times(1)).updateTaskStatus(anyInt(),anyString());
    }

    @Test
    void taskDoesNotExistChangeStatusReturnsNotFound() throws Exception {

        when(mockService.updateTaskStatus(anyInt(),anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        mockMvc.perform(put("/task/1/changestatus/name").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).updateTaskStatus(anyInt(),anyString());
    }

    //8 - /notification/{notificationId}/setseen
    @Test
    void setSeenNotificationExistSetSeenNotificationReturnsOK() throws Exception {
        String jsonRequest = """
        {
            "notification_id": 1,
            "user_id": 1,
            "task_id": 1,
            "is_seen": "0",
            
        """;
        //when
        when(mockService.setSeenNotification(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        mockMvc.perform(put("/notification/1/setseen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
        //then
        verify(mockService, times(1)).setSeenNotification(anyInt());
    }

    @Test
    void setSeenNotificationDoesNotExistSetSeenNotificationReturnsNotFound() throws Exception {
        //given board does not exist
        //when
        when(mockService.setSeenNotification(anyInt()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        mockMvc.perform(put("/notification/1/setseen").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(mockService, times(1)).setSeenNotification(anyInt());
    }

    //9 - /tm/{timeId}/update

    @Test
    void tmExistsUpdateTmReturnsOK() throws Exception {


        UpdateTimeManagementRequest mockUpdateTmReq = new UpdateTimeManagementRequest("01.01.99",1,"descr");
        when(mockService.updateUserTimeManagement(anyInt(),anyString(),anyInt(),anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
        mockMvc.perform(put("/tm/1/update").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(mockUpdateTmReq)))
                .andExpect(status().isOk());

        verify(mockService, times(1)).updateUserTimeManagement(anyInt(),anyString(),anyInt(),anyString());
    }

    @Test
    void tmDoesNotExistUpdateTmReturnsNotFound() throws Exception {

        UpdateTimeManagementRequest mockUpdateTmReq = new UpdateTimeManagementRequest("01.01.99",1,"descr");
        when(mockService.updateUserTimeManagement(anyInt(),anyString(),anyInt(),anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        mockMvc.perform(put("/tm/1/update").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(mockUpdateTmReq)))
                .andExpect(status().isNotFound());

        verify(mockService, times(1)).updateUserTimeManagement(anyInt(),anyString(),anyInt(),anyString());
    }

    /*************** DELETE METHOD TESTS ***************/
    //1 - /board/{boardId}/managers/{userId}/remove
    @Test
    void userRemovedSuccessfullyRemoveUserFromBoardManagersReturnsNoContent() throws Exception {
        when(mockService.removeUserFromBoardManagers(anyInt(), anyInt())).thenReturn(true);
        mockMvc.perform(delete("/board/1/managers/1/remove").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(mockService, times(1)).removeUserFromBoardManagers(anyInt(), anyInt());
    }

    @Test
    void failedToRemoveUserRemoveUserFromBoardManagersReturnsBadRequest() throws Exception {
        when(mockService.removeUserFromBoardManagers(anyInt(), anyInt())).thenReturn(false);
        mockMvc.perform(delete("/board/1/managers/1/remove").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(mockService, times(1)).removeUserFromBoardManagers(anyInt(), anyInt());
    }

    //2 - /board/{boardId}/employees/{userId}/remove
    @Test
    void validBoardIdAndUserIdRemoveUserFromBoardEmployeesReturnsNoContent() throws Exception {
        int boardId = 1;
        int userId = 1;

        when(mockService.removeUserFromBoardEmployees(boardId, userId)).thenReturn(true);

        mockMvc.perform(delete("/board/{boardId}/employees/{userId}/remove", boardId, userId))
                .andExpect(status().isNoContent())
                .andExpect(content().string("User removed from employees"));

        verify(mockService, times(1)).removeUserFromBoardEmployees(boardId, userId);
    }

    @Test
    void validBoardIdAndInvalidUserIdRemoveUserFromBoardEmployeesReturnsBadRequest() throws Exception {
        int boardId = 1;
        int userId = 1;

        when(mockService.removeUserFromBoardEmployees(boardId, userId)).thenReturn(false);

        mockMvc.perform(delete("/board/{boardId}/employees/{userId}/remove", boardId, userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to delete user from employees"));

        verify(mockService, times(1)).removeUserFromBoardEmployees(boardId, userId);

    }

      //3 - /task/{taskId}/delete

    @Test
    void taskRemovedSuccessfullyDeleteTaskReturnsNoContent() throws Exception {
        when(mockService.deleteTask(anyInt())).thenReturn(true);
        mockMvc.perform(delete("/task/1/delete").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(mockService, times(1)).deleteTask(anyInt());
    }

    @Test
    void failedToRemoveTaskDeleteTaskReturnsBadRequest() throws Exception {
        when(mockService.deleteTask(anyInt())).thenReturn(false);
        mockMvc.perform(delete("/task/1/delete").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(mockService, times(1)).deleteTask(anyInt());
    }

    //4 - /tm/{timeId}/delete
    @Test
    void timeDeleteSuccessfullyDeleteUserTimeManagementReturnsNoContent() throws Exception {
        when(mockService.deleteUserTimeManagement(anyInt())).thenReturn(true);
        mockMvc.perform(delete("/tm/1/delete").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(mockService, times(1)).deleteUserTimeManagement(anyInt());
    }

    @Test
    void failedToDeleteDeleteUserTimeManagementReturnsBadRequest() throws Exception {
        when(mockService.deleteUserTimeManagement(anyInt())).thenReturn(false);
        mockMvc.perform(delete("/tm/1/delete").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(mockService, times(1)).deleteUserTimeManagement(anyInt());
    }
}
