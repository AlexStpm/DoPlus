package ltd.future.planning.tech.DoPlus.service;

import ltd.future.planning.tech.DoPlus.api.AdminUserView;
import ltd.future.planning.tech.DoPlus.api.CreateTaskRequest;
import ltd.future.planning.tech.DoPlus.api.*;
import ltd.future.planning.tech.DoPlus.dao.DAO;
import ltd.future.planning.tech.DoPlus.entity.*;
import ltd.future.planning.tech.DoPlus.entity.Board;
import ltd.future.planning.tech.DoPlus.entity.Notification;
import ltd.future.planning.tech.DoPlus.entity.TimeManagement;
import ltd.future.planning.tech.DoPlus.entity.User;
import ltd.future.planning.tech.DoPlus.entity.Tag;
import ltd.future.planning.tech.DoPlus.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class ServiceTests {
    @Mock
    private DAO mockDAO;
    @InjectMocks
    private Service service;

    private List<User> prepareUsers() {
        return Arrays.asList(
                new User(1, "user1", "pass1", "User", "One", 2, 1),
                new User(2, "user2", "pass2", "User", "Two", 3, 1),
                new User(3, "rootadmin", "rootpass", "Root", "Admin", 1, 1),
                new User(4, "inactiveUser", "pass3", "Inactive", "User", 2, 0)
        );
    }

    /*************** GET METHOD TESTS ***************/
    //1 - /admin/getallusers
    @Test
    void usersExistsGetAllUsersForAdminReturnsListOfUsersViews() {
        //given
        List<User> mockUsersList = List.of(new User(1, "username1", "password1", "firstName1", "lastName1", 1, 1), new User(2, "username2", "password2", "firstName2", "lastName2", 1, 1));
        //should return this
        List<AdminUserView> mockUserViews = List.of(new AdminUserView(1, "username1", "firstName1", "lastName1", true), new AdminUserView(2, "username2", "firstName2", "lastName2", true));
        //when
        when(mockDAO.getAllUsers()).thenReturn(Optional.of(mockUsersList));
        Optional<List<AdminUserView>> result = service.getAllUsersForAdmin();
        //then
        assertFalse(result.isEmpty());
        assertEquals(result, Optional.of(mockUserViews));
    }

    @Test
    void usersDoesNotExistsGetAllUsersForAdminReturnsEmptyOptional() {
        //given no users
        //when
        when(mockDAO.getAllUsers()).thenReturn(Optional.empty());
        Optional<List<AdminUserView>> result = service.getAllUsersForAdmin();
        //then
        assertTrue(result.isEmpty());
    }

    //2 - /user/all
    @Test
    public void usersExistGetAllUsersReturnsListOfUserViews() {
        List<User> users = prepareUsers();
        when(mockDAO.getAllUsers()).thenReturn(Optional.of(users));

        Optional<List<UserView>> result = service.getAllUsers();

        assertTrue(result.isPresent());
        List<UserView> userViews = result.get();

        assertEquals(2, userViews.size());
        assertFalse(userViews.stream().anyMatch(u -> u.username().equals("rootadmin")));
        assertFalse(userViews.stream().anyMatch(u -> u.username().equals("inactiveUser")));

        Map<Integer, String[]> expectedValues = new HashMap<>();
        expectedValues.put(1, new String[]{"user1", "User One"});
        expectedValues.put(2, new String[]{"user2", "User Two"});

        userViews.forEach(userView -> {
            assertTrue(expectedValues.containsKey(userView.id()));
            assertArrayEquals(expectedValues.get(userView.id()), new String[]{userView.username(), userView.name()});
        });
    }

    @Test
    public void usersDoNotExistGetAllUsersReturnsEmptyOptional() {
        when(mockDAO.getAllUsers()).thenReturn(Optional.empty());

        Optional<List<UserView>> result = service.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isPresent());

    }

      //3 - /user/getbyid/{userId}
    @Test
    void userExistGetUserByIdReturnsOptionOfUserView() {

        var mockUser = new User(1, "username", "password", "firstName", "lastName", 1, 1);
        when(mockDAO.getUserById(anyInt())).thenReturn(Optional.of(mockUser));
        var result = service.getUserById(1);

        assertFalse(result.isEmpty());
        var resultUser = result.get();
        assertEquals(resultUser.id(), mockUser.getUserId());
    }

    @Test
    void userDoesNotExistGetUserByIdReturnsEmpty() {

        when(mockDAO.getUserById(anyInt())).thenReturn(Optional.empty());
        var result = service.getUserById(1);

        assertTrue(result.isEmpty());
    }

    //4 - /user/getuserboards/{userId}
    @Test
    void userIdGivenGetUserBoardsListOfBoardsReturnsListOfBoardView() {
        //given
        List<Board> mockBoardsList = List.of(
                new Board(1, "board1",1),
                new Board(2, "board1", 0));
        //when
        when(mockDAO.getUserBoards(1)).thenReturn(Optional.of(mockBoardsList));
        var result = service.getUserBoards(1);
        //then
        assertFalse(result.isEmpty());
        assertEquals(result.get().size(), mockBoardsList.size());
    }

    @Test
    void boardsDoesNotExistsGetUserBoardsReturnsOptionalEmpty() {
        //given no boards
        //when
        when(mockDAO.getUserBoards(1)).thenReturn(Optional.empty());
        var result = service.getUserBoards(1);
        //then
        assertTrue(result.isEmpty());
    }

    //5 - /board/{boardId}/info
    @Test
    void boardExistsGetBoardReturnsBoardView() {
        //given
        Board mockBoard = new Board(1, "name", 0);
        //when
        when(mockDAO.getBoard(1)).thenReturn(Optional.of(mockBoard));
        var result = service.getBoard(1);
        //then
        assertFalse(result.isEmpty());
        assertEquals(result.get().name(), mockBoard.getName());
    }

    @Test
    void boardDoesNotExistsGetBoardReturnsEmptyOptional() {
        //given no board
        //when
        when(mockDAO.getBoard(1)).thenReturn(Optional.empty());
        var result = service.getBoard(1);
        //then
        assertTrue(result.isEmpty());
    }
    //7 /board/{boardId}/owner
    @Test
    void ownerExistGetBoardOwnerReturnsOptionOfUser() {

        var mockUser = new User(1, "username", "password", "firstName", "lastName", 1, 1);
        when(mockDAO.getBoardOwner(anyInt())).thenReturn(Optional.of(mockUser));
        var result = service.getBoardOwner(1);

        assertFalse(result.isEmpty());
        var resultUser = result.get();
        assertEquals(resultUser.id(), mockUser.getUserId());
    }

    @Test
    void ownerDoesNotExistGetBoardOwnerReturnsOptionOfEmpty() {

        when(mockDAO.getBoardOwner(anyInt())).thenReturn(Optional.empty());
        var result = service.getBoardOwner(1);

        assertTrue(result.isEmpty());
    }

    //6 - /board/{boardId}/tasks
    @Test
    void tasksExistForBoardGetBoardTasksReturnsListOfTaskViews() {
        Task task1 = new Task(1, 2, 3, 1, "High", "Critical", 5, "Open", "Task 1 description", "Task 1", "2022-01-01");
        Task task2 = new Task(2, 3, 4, 1, "Medium", "Major", 8, "In Progress", "Task 2 description", "Task 2", "2022-01-02");

        TaskView taskView1 = TaskView.builder()
                .id(1).title("Task 1").assignee("User2").reporter("User3")
                .priority("High").severity("Critical").estimation(5)
                .status("Open").description("Task 1 description")
                .tags(Arrays.asList(1, 2)).creationTime("2022-01-01").boardId(1)
                .build();

        TaskView taskView2 = TaskView.builder()
                .id(2).title("Task 2").assignee("User3").reporter("User4")
                .priority("Medium").severity("Major").estimation(8)
                .status("In Progress").description("Task 2 description")
                .tags(Arrays.asList(2, 3)).creationTime("2022-01-02").boardId(1)
                .build();

        when(mockDAO.getBoardTasks(1)).thenReturn(Optional.of(Arrays.asList(task1, task2)));
        when(mockDAO.getUserById(2)).thenReturn(Optional.of(new User(2, "User2", "pass2", "User", "Two", 2, 1)));
        when(mockDAO.getUserById(3)).thenReturn(Optional.of(new User(3, "User3", "pass3", "User", "Three", 2, 1)));
        when(mockDAO.getUserById(4)).thenReturn(Optional.of(new User(4, "User4", "pass4", "User", "Four", 2, 1)));
        when(mockDAO.getTaskTagsId(1)).thenReturn(Arrays.asList(1, 2));
        when(mockDAO.getTaskTagsId(2)).thenReturn(Arrays.asList(2, 3));

        Optional<List<TaskView>> result = service.getBoardTasks(1);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals(Arrays.asList(taskView1, taskView2), result.get());
    }

    @Test
    void tasksDoNotExistForBoardGetBoardTasksReturnsEmptyList() {
        when(mockDAO.getBoardTasks(1)).thenReturn(Optional.empty());

        Optional<List<TaskView>> result = service.getBoardTasks(1);

        assertFalse(result.isPresent(), "Expected an empty Optional");
    }

    //8 - /board/{boarId}/managers
    @Test
    void managersExistsGetBoardManagersReturnsListOfUserView() {
        //given
        List<User> mockUsersList = List.of(new User(1, "username1", "password1", "firstName1", "lastName1", 1, 1), new User(2, "username2", "password2", "firstName2", "lastName2", 1, 1));
        //when
        when(mockDAO.getBoardManagers(1)).thenReturn(Optional.of(mockUsersList));
        var result = service.getBoardManagers(1);
        //then
        assertFalse(result.isEmpty());
        assertEquals(result.get().size(), mockUsersList.size());
    }

    @Test
    void managersDoesNotExistsGetBoardManagersReturnsOptionalEmpty() {
        //given no users
        //when
        when(mockDAO.getBoardManagers(1)).thenReturn(Optional.empty());
        var result = service.getBoardManagers(1);
        //then
        assertTrue(result.isEmpty());
    }

    //9 - /board/{boardId}/employees
    @Test
    void employeesExistsGetBoardEmployeesReturnsListOfUserView() {
        //given
        List<User> mockUsersList = List.of(new User(1, "username1", "password1", "firstName1", "lastName1", 1, 1), new User(2, "username2", "password2", "firstName2", "lastName2", 1, 1));
        //when
        when(mockDAO.getBoardEmployees(1)).thenReturn(Optional.of(mockUsersList));
        var result = service.getBoardEmployees(1);
        //then
        assertFalse(result.isEmpty());
        assertEquals(result.get().size(), mockUsersList.size());
    }

    @Test
    void employeesDoesNotExistsGetBoardEmployeesReturnsOptionalEmpty() {
        //given no users
        //when
        when(mockDAO.getBoardEmployees(1)).thenReturn(Optional.empty());
        var result = service.getBoardEmployees(1);
        //then
        assertTrue(result.isEmpty());
    }

    //10 - /board/{boardId}/tags
    @Test
    void tagsExistForBoardGetBoardTagsReturnsListOfTagViews() {
        int boardId = 1;
        Tag tag1 = Tag.builder().tagId(1).boardId(1).name("Tag1").isActive(1).build();
        Tag tag2 = Tag.builder().tagId(2).boardId(1).name("Tag2").isActive(0).build();

        when(mockDAO.getAllBoardTags(boardId)).thenReturn(Optional.of(Arrays.asList(tag1, tag2)));

        Optional<List<TagView>> result = service.getBoardTags(boardId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("Tag1", result.get().get(0).name());
        assertTrue(result.get().get(0).isActive());
        assertEquals("Tag2", result.get().get(1).name());
        assertFalse(result.get().get(1).isActive());
    }

    @Test
    void tagsDoNotExistForBoardGetBoardTagsReturnsEmptyOptional() {
        int boardId = 1;

        when(mockDAO.getAllBoardTags(boardId)).thenReturn(Optional.empty());

        Optional<List<TagView>> result = service.getBoardTags(boardId);

        assertNotNull(result, "The result should not be null.");
        assertTrue(result.isEmpty(), "Expected an empty Optional for board with no tags");
    }


      //11 - /task/getbyid/{taskId}
    @Test
    void taskExistsGetTaskByIdReturnsOptionOfTask() {

        var mockTask = new Task(1,1,1,1,"high","high",1,"high","description","title","01.01.99");
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.of(mockTask));
        var result = service.getTaskById(1);

        assertFalse(result.isEmpty());
        var resultUser = result.get();
        assertEquals(resultUser.id(), mockTask.getTaskId());
    }

    @Test
    void taskDoesnNotExistsGetTaskByIdReturnsOptionOfEmpty() {

        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.empty());
        var result = service.getTaskById(1);
    }

    //12 - /notification/{userId}/all
    @Test
    void notificationsExistsGetUserNotificationsReturnsListOfTNotificationViews() {
        //given
        List<Notification> mockNotififcationList = List.of(new Notification(1, 1, 1, 1),
        new Notification(2, 1, 2, 0));
        //when
        when(mockDAO.getUserNotifications(1)).thenReturn(Optional.of(mockNotififcationList));
        var result = service.getUserNotifications(1);
        //then
        assertFalse(result.isEmpty());
        assertEquals(result.get().size(), mockNotififcationList.size());
    }

    @Test
    void notificationDoesNotExistsGetUserNotificationReturnsOptionalEmpty() {
        //given no notifications
        //when
        when(mockDAO.getUserNotifications(1)).thenReturn(Optional.empty());
        var result = service.getUserNotifications(1);
        //then
        assertTrue(result.isEmpty());
    }

    //13 - /tm/{userId}
    @Test
    void tmExistsGetTimeManagementDataReturnsListOfTmViews() {
        //given
        List<TimeManagement> mockTimeManagementList = List.of(new TimeManagement(1, 1, "dateTime", 1, "description"), new TimeManagement(2, 1, "dateTime", 2, "description"));
        //when
        when(mockDAO.getTimeManagementData(1)).thenReturn(Optional.of(mockTimeManagementList));
        var result = service.getTimeManagementData(1);
        //then
        assertFalse(result.isEmpty());
        assertEquals(result.get().size(), mockTimeManagementList.size());
    }

    @Test
    void tmDoesNotExistsGetTimeManagementDataReturnsOptionalEmpty() {
        //given no tm records
        //when
        when(mockDAO.getTimeManagementData(1)).thenReturn(Optional.empty());
        var result = service.getTimeManagementData(1);
        //then
        assertTrue(result.isEmpty());
    }

    /*************** POST METHOD TESTS ***************/

    //3 - /board/add/{userId}
    @Test
    void boardNameUnder140AddBoardReturnsOptionOfBoardId() {

        when(mockDAO.addBoard(anyString())).thenReturn(1);
        var result = service.addBoard("name", 1);

        assertTrue(result.isPresent());
    }

    @Test
    void boardNameExceeds140AddBoardReturnsOptionOfEmpty() {

        var result = service.addBoard("namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamen", 1);

        assertTrue(result.isEmpty());
    }

    //4 - /board/{boardId}/managers/{userId}/add
    @Test
    void userIsNotOnBoardAddUserToBoardManagersReturnsTrue() {
        //given user does not have role on board
        //when
        when(mockDAO.checkIfUserOnABoard(anyInt(), anyInt())).thenReturn(false);
        boolean result = service.addUserToBoardManagers(1, 1);
        //then
        assertTrue(result);
    }

    @Test
    void userIsOnBoardAddUserToBoardManagersReturnsFalse() {
        //given user does not have role on board
        //when
        when(mockDAO.checkIfUserOnABoard(anyInt(), anyInt())).thenReturn(true);
        boolean result = service.addUserToBoardManagers(1, 1);
        //then
        assertFalse(result);
    }

    //5 - /board/{boardId}/employees/{userId}/add
    @Test
    void userIsNotOnBoardAddUserToBoardEmployeesReturnsTrue() {
        //given user does not have role on board
        //when
        when(mockDAO.checkIfUserOnABoard(anyInt(), anyInt())).thenReturn(false);
        boolean result = service.addUserToBoardEmployees(1, 1);
        //then
        assertTrue(result);
    }

    @Test
    void userIsOnBoardAddUserToBoardEmployeesReturnsFalse() {
        //given user does not have role on board
        //when
        when(mockDAO.checkIfUserOnABoard(anyInt(), anyInt())).thenReturn(true);
        boolean result = service.addUserToBoardEmployees(1, 1);
        //then
        assertFalse(result);
    }


    //6 - /board/tag/add
    @Test
    void tagDataIsValidAddTagToBoardReturnsSuccessMessage() {
        String response = service.addTagToBoard(1, "New Tag").getBody().toString();
        assertEquals("Added tag to board", response);
        verify(mockDAO, times(1)).addTagToBoard(1, "New Tag");
    }

    @Test
    void tagLengthExceedsLimitAddTagToBoardReturnsFailureMessage() {
        String longName = "d".repeat(141);
        String response = service.addTagToBoard(1, longName).getBody().toString();
        assertEquals("Tag length should be not more than 140 characters", response);
        verify(mockDAO, never()).addTagToBoard(anyInt(), anyString());
    }

    //8 - /notification/sent
    @Test
    void newNotificationAddNotificationReturnsTrue() {

        CreateNotificationRequest request = new CreateNotificationRequest(1, 1);
        when(mockDAO.getAllNotifications()).thenReturn(Optional.empty());


        boolean result = service.addNotification(request);


        assertTrue(result);
        verify(mockDAO, times(1)).addNotification(any(Notification.class));
    }


    @Test
    void duplicateNotificationAddNotificationReturnsTrue() {

        int userId = 1;
        int taskId = 1;
        CreateNotificationRequest request = new CreateNotificationRequest(userId, taskId);
        Notification existingNotification = new Notification(1, userId, taskId, 0);
        when(mockDAO.getAllNotifications()).thenReturn(Optional.of(Collections.<Notification>singletonList(existingNotification)));


        boolean result = service.addNotification(request);


        assertTrue(result);
        verify(mockDAO, never()).addNotification(any(Notification.class));
    }
    @Test
    void halfDuplicateNotificationAddNotificationReturnsFalseAndDeletesHalfDuplicate() {

        int userId = 1;
        int taskId = 1;
        CreateNotificationRequest request = new CreateNotificationRequest(userId, taskId);
        Notification halfDuplicateNotification = new Notification(1, 2, taskId, 0);


        when(mockDAO.getAllNotifications()).thenReturn(Optional.of(Collections.<Notification>singletonList(halfDuplicateNotification)));


        boolean result = service.addNotification(request);


        assertFalse(result);
        verify(mockDAO, times(1)).deleteNotification(halfDuplicateNotification.getNotificationId());
        verify(mockDAO, times(1)).addNotification(any(Notification.class));
    }

    //9 - /tm/add
    @Test
    void tmDataIsValidAddUserTimeManagementReturnsOk() {
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.of(new Task()));
        when(mockDAO.getTotalLoggedTimeForTaskOnDate(anyInt(), anyString(), anyInt())).thenReturn(0);

        ResponseEntity<?> response = service.addUserTimeManagement(1, "2023-01-01 10:00", 1, "Worked on task");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Added time management record", response.getBody());
    }

    @Test
    void tmDescriptionExceedsLimitAddUserTimeManagementReturnsBadRequest() {
        String longDescription = "d".repeat(141);
        ResponseEntity<?> response = service.addUserTimeManagement(1, "2023-01-01 10:00", 1, longDescription);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Description length exceeds 140 characters", response.getBody());
    }

    @Test
    void taskDoesNotExistAddUserTimeManagementReturnsBadRequest() {
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.addUserTimeManagement(1, "2023-01-01 10:00", 999, "Attempt to log time for a non-existent task.");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Task does not exist", response.getBody());
    }

    @Test
    void totalLoggedTimeExceedsLimitAddUserTimeManagementReturnsBadRequest() {
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.of(new Task()));
        when(mockDAO.getTotalLoggedTimeForTaskOnDate(anyInt(), anyString(), anyInt())).thenReturn(24 * 60);

        ResponseEntity<?> response = service.addUserTimeManagement(1, "2023-01-01 10:00", 1, "Worked on task");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(true, response.getBody().toString().contains("Total logged time exceeds 24 hours for the day"));
    }

      //7 - /task/add/

    @Test
    void taskDescrUnder1440AddBoardReturnsOptionOfTaskId() {

        CreateTaskRequest mockTaskReq = new CreateTaskRequest("title",1,1,1,"high"
        ,"high",1,"IN_PROGRESS","description",List.of(1),"01.01.99");
        when(mockDAO.addTaskToBoard(any())).thenReturn(1);
        var result = service.addTaskToBoard(mockTaskReq);

        assertTrue(result.isPresent());
        var resultMock = result.get();
        assertEquals(resultMock, 1);
    }

    @Test
    void taskDescrExceeds1440AddBoardReturnsOptionOfEmpty() {

        var description = "namenamenamenamenamennamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenaamenamenamenamenamenamenamenamenamenamenamenanamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamena" +
                "menamenamenamenamenamnamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenaenanamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamnamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenam" +
                "namenamenamenamenamennamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenanamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenaamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamnamenam" +
                "namenamenamenamenamennamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenanamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenaamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenanamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenammenam" +
                "namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenanamnamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenaenamenamenamenamenamenamenamenamenamenamenamenamnamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenam" +
                "namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenanamenamenamenamenamenamenamenamenamenamenamenamenanamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenammenamenamenamenam" +
                "namenamenamenamenamenamnamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenaenamenamenamenamenamenamenamenamenamenamenamennamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamamenamenamenamenam" +
                "namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamennamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamamenamenamenamenam" +
                "namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenanamen";
        CreateTaskRequest mockTaskReq = new CreateTaskRequest("title",1,1,1,"high"
                ,"high",1,"IN_PROGRESS",description,List.of(1),"01.01.99");
        var result = service.addTaskToBoard(mockTaskReq);

        assertTrue(result.isEmpty());
    }

    /*************** PUT METHOD TESTS ***************/
    //1 - /admin/updateuser/{userId}
    @Test
    void userExistAndUniqueUsernameGivenUpdateUserReturnsOK() {
        //given user exists and new username is not present in database
        //when
        when(mockDAO.getUserById(anyInt())).thenReturn(Optional.of(new User(1, "username", "password", "firstName", "lastName", 1, 1)));
        when(mockDAO.getUserByUsername(anyString())).thenReturn(Optional.empty());
        var result = service.updateUser(1, "username", "password", "firstName", 1);
        //then
        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void userExistAndIsNotUniqueUsernameGivenUpdateUserReturnsConflict() {
        //given user exists and new username is present in database
        //when
        when(mockDAO.getUserById(anyInt())).thenReturn(Optional.of(new User(1, "username1", "password1", "firstName1", "lastName1", 1, 1)));
        when(mockDAO.getUserByUsername(anyString())).thenReturn(Optional.of(new User(2, "username2", "password2", "firstName2", "lastName2", 1, 1)));
        var result = service.updateUser(1, "username", "password", "firstName", 1);
        //then
        assertEquals(ResponseEntity.status(HttpStatus.CONFLICT).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void userDoesNotExistUpdateUserReturnsNotFound() {
        //given user does not exist
        //when
        when(mockDAO.getUserById(anyInt())).thenReturn(Optional.empty());
        var result = service.updateUser(1, "username", "password", "firstName", 1);
        //then
        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), result.getStatusCode());
    }
    //3 - /board/tag/{tagId}/activate
    @Test
    void tagExistActivateTagReturnsOK() throws Exception {

        when(mockDAO.getTagById(anyInt())).thenReturn(Optional.of(new Tag(1,1,"name",1)));
        var result = service.activateTag(1);

        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void tagDoesNotExistActivateTagNotFound() throws Exception {

        when(mockDAO.getTagById(anyInt())).thenReturn(Optional.empty());
        var result = service.activateTag(1);

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), result.getStatusCode());
    }

    //2 - /board/{boardId}/update
    @Test
    void boardDataIsValidUpdateBoardReturnsUpdatedMessage() {
        int boardId = 1;
        String name = "Updated Board Name";
        Board existingBoard = new Board(boardId, "Old Board Name", 0);

        when(mockDAO.getBoard(boardId)).thenReturn(Optional.of(existingBoard));
        doNothing().when(mockDAO).updateBoard(boardId, name);

        ResponseEntity<?> response = service.updateBoard(boardId, name);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated board", response.getBody());
        verify(mockDAO).updateBoard(boardId, name);
    }

    @Test
    void boardNameIsInvalidUpdateBoardReturnsBadRequest() {
        int boardId = 1;
        String invalidName = "";

        Board existingBoard = new Board(boardId, "Old Board Name", 0);
        when(mockDAO.getBoard(boardId)).thenReturn(Optional.of(existingBoard));

        ResponseEntity<?> response = service.updateBoard(boardId, invalidName);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((String)response.getBody()).contains("Name must be non-empty and up to 140 characters long."));
    }

    @Test
    void boardDoesNotExistUpdateBoardReturnsNotFound() {
        int invalidBoardId = 999;
        String name = "New Board Name";

        when(mockDAO.getBoard(invalidBoardId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.updateBoard(invalidBoardId, name);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    //4- board/tag/{tagId}/deactivate
    @Test
    void tagExistDeactivateTagReturnsOK() throws Exception {

        when(mockDAO.getTagById(anyInt())).thenReturn(Optional.of(new Tag(1,1,"name",1)));
        var result = service.deactivateTag(1);

        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void tagDoesNotExistDeactivateTagNotFound() throws Exception {

        when(mockDAO.getTagById(anyInt())).thenReturn(Optional.empty());
        var result = service.deactivateTag(1);

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), result.getStatusCode());
    }

    //5 - /board/{boardId}/archive
    @Test
    void boardExistArchiveBoardReturnsOK() throws Exception {
        //given board exist
        //when
        when(mockDAO.getBoard(anyInt())).thenReturn(Optional.of(new Board(1, "name", 0)));
        doNothing().when(mockDAO).archiveBoard(anyInt());
        var result = service.archiveBoard(1);
        //then
        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void boardDoesNotExistArchiveBoardReturnsNotFound() throws Exception {
        //given board does not exist
        //when
        when(mockDAO.getBoard(anyInt())).thenReturn(Optional.empty());
        var result = service.archiveBoard(1);
        //then
        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), result.getStatusCode());
    }

    //6 - /task/{taskId}/update
    @Test
    void taskDataIsValidUpdateTaskReturnsOk() {
        int taskId = 1;
        Task existingTask = new Task(1, 1, 1, 1, "Medium", "Moderate", 8, "Open", "Valid description", "Task Title", "2020-01-01T00:00");
        UpdateTaskRequest request = new UpdateTaskRequest("Updated Title", 2, 2, "Low", "Minor", 3, "Done", "Updated description", List.of(3, 4));

        when(mockDAO.getTaskById(taskId)).thenReturn(Optional.of(existingTask));
        doNothing().when(mockDAO).updateTask(any(Task.class));
        doNothing().when(mockDAO).deleteTaskTags(taskId);
        doNothing().when(mockDAO).addTaskTags(eq(taskId), anyList());

        ResponseEntity<?> response = service.updateTask(taskId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Updated task"));
    }

    @Test
    void taskDoesNotExistUpdateTaskReturnsNotFound() {
        int nonExistentTaskId = 999;
        UpdateTaskRequest request = new UpdateTaskRequest("Task Title", 1, 1, "High", "Critical", 5, "To Do", "Task description", List.of(1, 2));

        when(mockDAO.getTaskById(nonExistentTaskId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.updateTask(nonExistentTaskId, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void taskDescriptionExceedsLimitUpdateTaskReturnsBadRequest() {
        int taskId = 1;
        String longDescription = "d".repeat(1441);
        UpdateTaskRequest request = new UpdateTaskRequest("Task Title", 1, 1, "High", "Critical", 5, "To Do", longDescription, List.of(1, 2));

        when(mockDAO.getTaskById(taskId)).thenReturn(Optional.of(new Task()));

        ResponseEntity<?> response = service.updateTask(taskId, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid request data"));
    }



      //7 - /task/{taskId}/changestatus/{statusname}

    @Test
    void validStatusTaskExistsUpdateTaskStatusReturnsOK() throws Exception {

        var mockTask = new Task(1,1,1,1,"high","high",1,"high","description","title","01.01.99");
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.of(mockTask));
        doNothing().when(mockDAO).updateTaskStatus(anyInt(),anyString());
        var result = service.updateTaskStatus(1,"Postponed");

        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void invalidStatusTaskExistsUpdateTaskStatusReturnsBadRequest() throws Exception {

        var mockTask = new Task(1,1,1,1,"high","high",1,"high","description","title","01.01.99");
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.of(mockTask));
        var result = service.updateTaskStatus(1,"no");

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void validStatusTaskDoesNotExistsUpdateTaskStatusReturnsNotFound() throws Exception {

        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.empty());
        var result = service.updateTaskStatus(1,"Postponed");

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), result.getStatusCode());
    }

    //8 - /notification/{notificationId}/setseen
    @Test
    void validSeenNotificationExistsSetSeenNotificationReturnsOK() throws Exception {

        var mockNotification = new Notification(1, 1, 1, 0);

        when(mockDAO.getNotificationById(anyInt())).thenReturn(Optional.of(mockNotification));

        var result = service.setSeenNotification(1);

        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void validSeenNotificationDoesNotExistsSetSeenNotificationReturnsNotFound() throws Exception {

        when(mockDAO.getNotificationById(anyInt())).thenReturn(Optional.empty());
        var result = service.setSeenNotification(0);

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), result.getStatusCode());
    }

    //9 - /tm/{timeId}/update

    @Test
    void tmDoesNotExistUpdateUserTimeManagementReturnsNotFound() throws Exception {

        when(mockDAO.getTimeManagementById(anyInt())).thenReturn(Optional.empty());
        var result = service.updateUserTimeManagement(1,"01.01.99",1,"description");

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void tmExistUpdateUserTimeManagementReturnsOk() throws Exception {

        TimeManagement mockTm = new TimeManagement(1,1,"2019-09-01 11:11",1,"description");
        var mockTask = new Task(1,1,1,1,"high","high",1,"high","description","title","2019-09-01 11:11");
        when(mockDAO.getTimeManagementById(anyInt())).thenReturn(Optional.of(mockTm));
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.of(mockTask));
        when(mockDAO.getTotalLoggedTimeForTaskOnDate(anyInt(),any(),anyInt())).thenReturn(2*60);
        var result = service.updateUserTimeManagement(1,"2019-09-01 11:11",1,"description");

        assertEquals(ResponseEntity.status(HttpStatus.OK).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void descrExceeds140UpdateUserTimeManagementReturnsBadRequest() throws Exception {

        var description = "namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamename";
        TimeManagement mockTm = new TimeManagement(1,1,"01.01.99",1,"description");
        when(mockDAO.getTimeManagementById(anyInt())).thenReturn(Optional.of(mockTm));
        var result = service.updateUserTimeManagement(1,"01.01.99",1,description);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void taskDoesNotExistsUpdateUserTimeManagementReturnsBadRequest() throws Exception {

        TimeManagement mockTm = new TimeManagement(1,1,"01.01.99",1,"description");
        when(mockDAO.getTimeManagementById(anyInt())).thenReturn(Optional.of(mockTm));
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.empty());
        var result = service.updateUserTimeManagement(1,"01.01.99",1,"description");

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build().getStatusCode(), result.getStatusCode());
    }

    @Test
    void logTimeExceed24HoursUpdateUserTimeManagementReturnsBadRequest() throws Exception {

        TimeManagement mockTm = new TimeManagement(1,1,"2019-09-01 11:11",1,"description");
        var mockTask = new Task(1,1,1,1,"high","high",1,"high","description","title","2019-09-01 11:11");
        when(mockDAO.getTimeManagementById(anyInt())).thenReturn(Optional.of(mockTm));
        when(mockDAO.getTaskById(anyInt())).thenReturn(Optional.of(mockTask));
        when(mockDAO.getTotalLoggedTimeForTaskOnDate(anyInt(),any(),anyInt())).thenReturn(25*60);
        var result = service.updateUserTimeManagement(1,"2019-09-01 11:11",1,"description");

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build().getStatusCode(), result.getStatusCode());
    }

    /*************** DELETE METHOD TESTS ***************/
    //1 - /board/{boardId}/managers/{userId}/remove
    @Test
    void userRemovedSuccessfullyRemoveUserFromBoardManagersReturnsTrue() throws Exception {
        //when
        when(mockDAO.removeUserFromBoardManagers(anyInt(), anyInt())).thenReturn(1);
        boolean result = service.removeUserFromBoardManagers(1, 1);
        //then
        assertTrue(result);
    }

    @Test
    void failedToRemoveUserRemoveUserFromBoardManagersReturnsFalse() throws Exception {
        //when
        when(mockDAO.removeUserFromBoardManagers(anyInt(), anyInt())).thenReturn(0);
        boolean result = service.removeUserFromBoardManagers(1, 1);
        //then
        assertFalse(result);
    }

    //2 - /board/{boardId}/employees/{userId}/remove
    @Test
    void userIsOnBoardRemoveUserFromBoardEmployeesReturnsTrue() {
        int boardId = 1;
        int userId = 1;

        when(mockDAO.removeUserFromBoardEmployees(boardId, userId)).thenReturn(1);

        assertTrue(service.removeUserFromBoardEmployees(boardId, userId));

        verify(mockDAO, times(1)).removeUserFromBoardEmployees(boardId, userId);
    }

    @Test
    void userDoesNotExistOnBoardRemoveUserFromBoardEmployeesReturnsFalse() {
        int boardId = 1;
        int userId = 1;

        when(mockDAO.removeUserFromBoardEmployees(boardId, userId)).thenReturn(0);

        assertFalse(service.removeUserFromBoardEmployees(boardId, userId));

        verify(mockDAO, times(1)).removeUserFromBoardEmployees(boardId, userId);
    }


      //3 - /task/{taskId}/delete
    @Test
    void taskRemovedSuccessfullyDeleteTaskReturnsTrue() throws Exception {

        when(mockDAO.deleteTask(anyInt())).thenReturn(1);
        boolean result = service.deleteTask( 1);

        assertTrue(result);
    }

    //4 - /tm/{timeId}/delete
    @Test
    void timeDeleteSuccessfullyDeleteUserTimeManagementReturnsTrue() throws Exception {

        when(mockDAO.deleteUserTimeManagement(anyInt())).thenReturn(1);
        boolean result = service.deleteUserTimeManagement(1);

        assertTrue(result);
    }

    @Test
    void failedToRemoveTaskDeleteTaskReturnsTrue() throws Exception {

        when(mockDAO.deleteTask(anyInt())).thenReturn(0);
        boolean result = service.deleteTask( 1);

        assertFalse(result);
    }

    @Test
    void failedToDeleteDeleteUserTimeManagementReturnsTrue() throws Exception {

        when(mockDAO.deleteUserTimeManagement(anyInt())).thenReturn(0);
        boolean result = service.deleteUserTimeManagement(1);

        assertFalse(result);
    }

}
