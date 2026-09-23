package ltd.future.planning.tech.DoPlus.controller;

import lombok.RequiredArgsConstructor;
import ltd.future.planning.tech.DoPlus.api.*;
import ltd.future.planning.tech.DoPlus.service.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final Service service;

    @GetMapping("/user/getbyid/{id}")
    public ResponseEntity<UserView> getUserById(@PathVariable int id) {
        var response = service.getUserById(id);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/admin/getallusers")
    public ResponseEntity<List<AdminUserView>> getAllUsersForAdmin() {
        var response = service.getAllUsersForAdmin();
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/user/all")
    public ResponseEntity<List<UserView>> getAllUsers() {
        var response = service.getAllUsers();
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/user/getuserboards/{userId}")
    public ResponseEntity<List<BoardView>> getUserBoards(@PathVariable int userId) {
        var response = service.getUserBoards(userId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/board/{boardId}/info")
    public ResponseEntity<BoardView> getBoard(@PathVariable int boardId) {
        var response = service.getBoard(boardId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/board/{boardId}/tasks")
    public ResponseEntity<List<TaskView>> getBoardTasks(@PathVariable int boardId) {
        var response = service.getBoardTasks(boardId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/board/{boardId}/owner")
    public ResponseEntity<UserView> getBoardOwner(@PathVariable int boardId) {
        var response = service.getBoardOwner(boardId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/board/{boardId}/managers")
    public ResponseEntity<List<UserView>> getBoardManagers(@PathVariable int boardId) {
        var response = service.getBoardManagers(boardId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/board/{boardId}/employees")
    public ResponseEntity<List<UserView>> getBoardEmployees(@PathVariable int boardId) {
        var response = service.getBoardEmployees(boardId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/board/{boardId}/tags")
    public ResponseEntity<List<TagView>> getAllBoardTags(@PathVariable int boardId) {
        var response = service.getBoardTags(boardId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/task/getbyid/{taskId}")
    public ResponseEntity<TaskView> getTaskById(@PathVariable int taskId) {
        var response = service.getTaskById(taskId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/notification/{userId}/all")
    public ResponseEntity<List<NotificationView>> getUserNotifications(@PathVariable int userId) {
        var response = service.getUserNotifications(userId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/tm/{userId}")
    public ResponseEntity<List<TimeManagementView>> getTimeManagementData(@PathVariable int userId) {
        var response = service.getTimeManagementData(userId);
        return response.map(taskViews ->
                ResponseEntity.status(HttpStatus.OK).body(taskViews)).orElseGet(()
                -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/tm/add")
    public ResponseEntity<?> addUserTimeManagement(@RequestBody CreateTimeManagementRequest request) {
        return service.addUserTimeManagement(request.userId(), request.dateTime(), request.taskId(), request.description());
    }

    @DeleteMapping("/tm/{timeId}/delete")
    public ResponseEntity<?> deleteUserTimeManagement(@PathVariable int timeId) {
        if (service.deleteUserTimeManagement(timeId)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("The time management record was deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete time management entry.");
        }
    }

    @PostMapping("/notification/sent")
    public ResponseEntity<?> addNotification(@RequestBody CreateNotificationRequest request) {
        boolean isAdded = service.addNotification(request);
        if (isAdded) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Created notification.");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("Notification recreated");
        }
    }

    @PostMapping("/board/tag/add")
    public ResponseEntity<?> addTagToBoard(@RequestBody CreateTagRequest request) {
        return service.addTagToBoard(request.boardId(), request.name());
    }

    @PostMapping("/task/add")
    public ResponseEntity<?> addTaskToBoard(@RequestBody CreateTaskRequest request) {
        var taskId = service.addTaskToBoard(request);
        if (taskId.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(taskId);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add task");
        }
    }

    @DeleteMapping("/task/{taskId}/delete")
    public ResponseEntity<?> deleteTask(@PathVariable int taskId) {
        if (service.deleteTask(taskId)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted task");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete task");
        }
    }

    @PostMapping("/board/add/{userId}")
    public ResponseEntity<?> addBoard(@RequestBody String boardName, @PathVariable int userId) {
        var boardId = service.addBoard(boardName, userId);
        if (boardId.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(boardId.get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name must be non-empty and up to 140 characters long.");
        }
    }

    @PostMapping("/board/{boardId}/managers/{userId}/add")
    public ResponseEntity<?> addUserToBoardManagers(@PathVariable int boardId, @PathVariable int userId) {
        boolean isAdded = service.addUserToBoardManagers(boardId, userId);
        if (isAdded) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User added to managers");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already on the board");
        }
    }

    @DeleteMapping("/board/{boardId}/managers/{userId}/remove")
    public ResponseEntity<?> removeUserFromBoardManagers(@PathVariable int boardId, @PathVariable int userId) {
        if (service.removeUserFromBoardManagers(boardId, userId)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User removed from managers");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete user from managers");
        }
    }

    @PostMapping("/board/{boardId}/employees/{userId}/add")
    public ResponseEntity<?> addUserToBoardEmployees(@PathVariable int boardId, @PathVariable int userId) {
        boolean isAdded = service.addUserToBoardEmployees(boardId, userId);
        if (isAdded) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User added to employees");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already on the board");
        }
    }

    @DeleteMapping("/board/{boardId}/employees/{userId}/remove")
    public ResponseEntity<?> removeUserFromBoardEmployees(@PathVariable int boardId, @PathVariable int userId) {
        if (service.removeUserFromBoardEmployees(boardId, userId)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User removed from employees");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete user from employees");
        }
    }

    @PutMapping("/admin/updateuser/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody UpdateUserRequest request) {
        return service.updateUser(userId, request.username(), request.firstName(), request.lastName(), request.isActive());
    }

    @PutMapping("/board/{boardId}/update")
    public ResponseEntity<?> updateBoard(@PathVariable int boardId, @RequestBody String name) {
        return service.updateBoard(boardId, name);
    }

    @PutMapping("/board/tag/{tagId}/activate")
    public ResponseEntity<?> activateTag(@PathVariable int tagId) {
        return service.activateTag(tagId);
    }

    @PutMapping("/board/tag/{tagId}/deactivate")
    public ResponseEntity<?> deactivateTag(@PathVariable int tagId) {
        return service.deactivateTag(tagId);
    }

    @PutMapping("/board/{boardId}/archive")
    public ResponseEntity<?> archiveBoard(@PathVariable int boardId) {
        return service.archiveBoard(boardId);
    }

    @PutMapping("/task/{taskId}/update")
    public ResponseEntity<?> updateTask(@PathVariable int taskId, @RequestBody UpdateTaskRequest request) {
        return service.updateTask(taskId, request);
    }

    @PutMapping("/task/{taskId}/changestatus/{statusName}")
    public ResponseEntity<?> updateTaskStatus(@PathVariable int taskId, @PathVariable String statusName) {
        return service.updateTaskStatus(taskId, statusName);
    }

    @PutMapping("/notification/{notificationId}/setseen")
    public ResponseEntity<?> setSeenNotification(@PathVariable int notificationId) {
        return service.setSeenNotification(notificationId);
    }

    @PutMapping("/tm/{timeId}/update")
    public ResponseEntity<?> updateUserTimeManagement(@PathVariable int timeId, @RequestBody UpdateTimeManagementRequest request) {
        return service.updateUserTimeManagement(timeId, request.dateTime(), request.taskId(), request.description());
    }

}
