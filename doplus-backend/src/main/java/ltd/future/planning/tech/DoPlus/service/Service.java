package ltd.future.planning.tech.DoPlus.service;

import lombok.RequiredArgsConstructor;

import ltd.future.planning.tech.DoPlus.api.*;
import ltd.future.planning.tech.DoPlus.dao.DAO;
import ltd.future.planning.tech.DoPlus.entity.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {
    private final DAO DAO;

    public Optional<UserView> getUserById(int id) {
        Optional<User> user = DAO.getUserById(id);
        if (user.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(user.map(value -> UserView.builder()
                .id(value.getUserId())
                .name(value.getFirstName() + " " + value.getLastName())
                .username(value.getUsername())
                .build()).orElse(null));
    }


    public Optional<List<AdminUserView>> getAllUsersForAdmin() {
        var optTasks = DAO.getAllUsers();
        if (optTasks.isEmpty())
            return Optional.empty();
        List<User> list = optTasks.get();
        if (!list.isEmpty()) {
            List<AdminUserView> userViewList = new ArrayList<>();
            for (User user : list) {
                if (!user.getUsername().equals("rootadmin")) {
                    AdminUserView userView = AdminUserView.builder().id(user.getUserId())
                            .username(user.getUsername())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .isActive(user.getIsActive() == 1)
                            .build();
                    userViewList.add(userView);
                }
            }
            return Optional.of(userViewList);
        }
        return Optional.empty();
    }

    public Optional<List<UserView>> getAllUsers() {
        var optTasks = DAO.getAllUsers();
        if (optTasks.isEmpty())
            return Optional.empty();
        List<User> list = optTasks.get();
        List<UserView> userViewList = new ArrayList<>();
        if (!list.isEmpty()) {
            for (User user : list) {
                if (!user.getUsername().equals("rootadmin") && (user.getIsActive() == 1)) {
                    UserView userView = UserView.builder().id(user.getUserId())
                            .username(user.getUsername())
                            .name(user.getFirstName() + " " + user.getLastName())
                            .build();
                    userViewList.add(userView);
                }
            }
        }
        return Optional.of(userViewList);
    }

    public Optional<List<BoardView>> getUserBoards(int id) {
        var optTasks = DAO.getUserBoards(id);
        if (optTasks.isEmpty())
            return Optional.empty();
        List<Board> boards = optTasks.get();
        List<BoardView> views = new ArrayList<>();
        for (Board board : boards) {
            views.add(BoardView.builder()
                    .id(board.getBoardId())
                    .name(board.getName())
                    .isArchived(board.getIsArchived() == 1)
                    .build());
        }
        return Optional.of(views);

    }

    public Optional<BoardView> getBoard(int boardId) {
        var optTasks = DAO.getBoard(boardId);
        if (optTasks.isEmpty())
            return Optional.empty();
        Board board = optTasks.get();
        return Optional.of(BoardView.builder()
                .id(board.getBoardId())
                .name(board.getName())
                .isArchived(board.getIsArchived() == 1)
                .build());
    }

    public Optional<List<TaskView>> getBoardTasks(int boardId) {
        var optTasks = DAO.getBoardTasks(boardId);
        if (optTasks.isEmpty())
            return Optional.empty();
        List<Task> tasks = optTasks.get();
        List<TaskView> taskViews = new ArrayList<>();

        for (Task task : tasks) {
            var assigneeNullable = DAO.getUserById(task.getAssigneeId());
            var assigneeName = assigneeNullable.isEmpty() ? "" : assigneeNullable.get().getUsername();
            var reporterName = DAO.getUserById(task.getReporterId()).get().getUsername();
            List<Integer> tagIds = DAO.getTaskTagsId(task.getTaskId());
            taskViews.add(TaskView.builder()
                    .title(task.getTitle())
                    .id(task.getTaskId())
                    .assignee(assigneeName)
                    .reporter(reporterName)
                    .priority(task.getPriority())
                    .severity(task.getSeverity())
                    .estimation(task.getEstimation())
                    .status(task.getStatus())
                    .description(task.getDescription())
                    .tags(tagIds)
                    .creationTime(task.getCreationTime())
                    .boardId(task.getBoardId())
                    .build());
        }
        return Optional.of(taskViews);
    }

    public Optional<UserView> getBoardOwner(int boardId) {
        var optVal = DAO.getBoardOwner(boardId);
        if (optVal.isEmpty())
            return Optional.empty();
        User owner = optVal.get();
        return Optional.of(UserView.builder()
                .id(owner.getUserId())
                .username(owner.getUsername())
                .name(owner.getFirstName() + " " + owner.getLastName())
                .build());
    }

    public Optional<List<UserView>> getBoardManagers(int boardId) {
        var optVal = DAO.getBoardManagers(boardId);
        if (optVal.isEmpty())
            return Optional.empty();
        List<User> managers = optVal.get();
        List<UserView> managerViews = new ArrayList<>();
        for (User manager : managers) {
            if (manager.getIsActive() == 1)
                managerViews.add(UserView.builder()
                        .id(manager.getUserId())
                        .username(manager.getUsername())
                        .name(manager.getFirstName() + " " + manager.getLastName())
                        .build());
        }
        return Optional.of(managerViews);
    }

    public Optional<List<UserView>> getBoardEmployees(int boardId) {
        var optVal = DAO.getBoardEmployees(boardId);
        if (optVal.isEmpty())
            return Optional.empty();
        List<User> employees = optVal.get();
        List<UserView> employeeViews = new ArrayList<>();
        for (User employee : employees) {
            if (employee.getIsActive() == 1)
                employeeViews.add(UserView.builder()
                        .id(employee.getUserId())
                        .username(employee.getUsername())
                        .name(employee.getFirstName() + " " + employee.getLastName())
                        .build());
        }
        return Optional.of(employeeViews);
    }

    public Optional<List<TagView>> getBoardTags(int boardId) {
        var optVal = DAO.getAllBoardTags(boardId);
        if (optVal.isEmpty())
            return Optional.empty();
        List<Tag> tags = optVal.get();
        List<TagView> tagViews = new ArrayList<>();
        for (Tag tag : tags) {
            tagViews.add(TagView.builder()
                    .id(tag.getTagId())
                    .name(tag.getName())
                    .isActive(tag.getIsActive() == 1)
                    .build());
        }
        return Optional.of(tagViews);
    }


    public Optional<TaskView> getTaskById(int taskId) {
        Optional<Task> optionalTask = DAO.getTaskById(taskId);

        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            var assigneeName = DAO.getUserById(task.getAssigneeId()).map(User::getUsername).orElse("");
            var reporterName = DAO.getUserById(task.getReporterId()).map(User::getUsername).orElse("");
            List<Integer> tagIds = DAO.getTaskTagsId(taskId);
            TaskView taskView = TaskView.builder()
                    .title(task.getTitle())
                    .id(task.getTaskId())
                    .assignee(assigneeName)
                    .reporter(reporterName)
                    .priority(task.getPriority())
                    .severity(task.getSeverity())
                    .estimation(task.getEstimation())
                    .status(task.getStatus())
                    .description(task.getDescription())
                    .tags(tagIds)
                    .creationTime(task.getCreationTime())
                    .boardId(task.getBoardId())
                    .build();

            return Optional.of(taskView);
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<NotificationView>> getUserNotifications(int userId) {
        var optVal = DAO.getUserNotifications(userId);
        if (optVal.isEmpty())
            return Optional.empty();
        List<Notification> notifications = optVal.get();
        List<NotificationView> notificationViews = new ArrayList<>();
        for (Notification notification : notifications) {
            notificationViews.add(NotificationView.builder()
                    .id(notification.getNotificationId())
                    .taskId(notification.getTaskId())
                    .seen(notification.getIsSeen() == 1)
                    .build());
        }
        return Optional.of(notificationViews);
    }

    public Optional<List<TimeManagementView>> getTimeManagementData(int userId) {
        Optional<List<TimeManagement>> timeManagementDataOptional = DAO.getTimeManagementData(userId);
        if (timeManagementDataOptional.isEmpty())
            return Optional.empty();

        List<TimeManagementView> timeManagementViews = new ArrayList<>();

        timeManagementDataOptional.ifPresent(timeManagementData -> {
            for (TimeManagement timeManagement : timeManagementData) {
                timeManagementViews.add(TimeManagementView.builder()
                        .id(timeManagement.getTimeManagementId())
                        .dateTime(timeManagement.getDateTime())
                        .taskId(timeManagement.getTaskId())
                        .description(timeManagement.getDescription())
                        .build());
            }
        });

        return Optional.of(timeManagementViews);
    }


    public ResponseEntity<?> updateUser(int id, String username, String firstName, String lastName, int isActive) {
        var user = DAO.getUserById(id);
        if (user.isPresent()) {
            var userNameCheck = DAO.getUserByUsername(username);
            if (userNameCheck.isPresent() && (userNameCheck.get().getUserId() != id))
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            DAO.updateUser(id, username, firstName, lastName, isActive);
            return ResponseEntity.ok().body("Updated user");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> updateBoard(int boardId, String name) {
        var board = DAO.getBoard(boardId);

        if (board.isPresent()) {
            if (name != null && !name.trim().isEmpty() && name.trim().length() <= 140) {
                DAO.updateBoard(boardId, name.trim());
                return ResponseEntity.ok().body("Updated board");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name must be non-empty and up to 140 characters long.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> activateTag(int tagId) {
        var tag = DAO.getTagById(tagId);

        if (tag.isPresent()) {
            DAO.activateTag(tagId);
            return ResponseEntity.ok().body("Updated tag");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> deactivateTag(int tagId) {
        var tag = DAO.getTagById(tagId);

        if (tag.isPresent()) {
            DAO.deactivateTag(tagId);
            return ResponseEntity.ok().body("Updated tag");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> archiveBoard(int boardId) {
        var board = DAO.getBoard(boardId);

        if (board.isPresent()) {
            DAO.archiveBoard(boardId);
            return ResponseEntity.ok().body("Updated board");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> updateTask(int taskId, UpdateTaskRequest request) {
        var taskExists = DAO.getTaskById(taskId);

        if (taskExists.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (request.description().length() >= 1440) {
            return ResponseEntity.badRequest().body("Invalid request data");
        }

        Task task = Task.builder()
                .taskId(taskExists.get().getTaskId())
                .title(request.title())
                .assigneeId(request.assigneeId())
                .reporterId(request.reporterId())
                .boardId(taskExists.get().getBoardId())
                .priority(request.priority())
                .estimation(request.estimation())
                .status(request.status())
                .severity(request.severity())
                .description(request.description())
                .creationTime(taskExists.get().getCreationTime())
                .build();
        DAO.updateTask(task);

        DAO.deleteTaskTags(task.getTaskId());
        DAO.addTaskTags(task.getTaskId(), request.tags());

        return ResponseEntity.ok().body("Updated task");
    }

    public ResponseEntity<?> updateTaskStatus(int taskId, String statusName) {
        Set<String> validStatuses = Set.of("Postponed", "To Do", "In progress", "Feedback", "Done");
        var task = DAO.getTaskById(taskId);

        if (task.isPresent() && validStatuses.contains(statusName)) {
            DAO.updateTaskStatus(taskId, statusName);
            return ResponseEntity.ok().body("Updated task");
        }

        return task.isPresent()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    public ResponseEntity<?> setSeenNotification(int notificationId) {
        var notification = DAO.getNotificationById(notificationId);

        if (notification.isPresent()) {
            DAO.setSeenNotification(notificationId);
            return ResponseEntity.ok().body("Updated notification");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> updateUserTimeManagement(int id, String dateTime, int taskId, String description) {
        var timeManagement = DAO.getTimeManagementById(id);

        if (timeManagement.isPresent()) {
            if (description.length() > 140) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Description length exceeds 140 characters");
            }

            var task = DAO.getTaskById(taskId);
            if (task.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task does not exist");
            }

            LocalDateTime ldt = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            int newLoggedTime = ldt.getHour() * 60 + ldt.getMinute();
            var totalLoggedTime = DAO.getTotalLoggedTimeForTaskOnDate(timeManagement.get().getUserId(), dateTime.split(" ")[0], id);

            int time = newLoggedTime + totalLoggedTime;

            if (totalLoggedTime + newLoggedTime > 24 * 60) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Total logged time exceeds 24 hours for the day: " + time);
            }

            DAO.updateUserTimeManagement(id, dateTime, taskId, description);
            return ResponseEntity.ok().body("Updated time management record");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> addUserTimeManagement(int id, String dateTime, int taskId, String description) {
        TimeManagement timeManagementNew = TimeManagement.builder().
                userId(id).
                dateTime(dateTime).
                taskId(taskId).
                description(description).
                build();

        if (description.length() > 140)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Description length exceeds 140 characters");


        var task = DAO.getTaskById(taskId);
        if (task.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task does not exist");
        }

        LocalDateTime ldt = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        int newLoggedTime = ldt.getHour() * 60 + ldt.getMinute();
        var totalLoggedTime = DAO.getTotalLoggedTimeForTaskOnDate(timeManagementNew.getUserId(), dateTime.split(" ")[0], id);

        int time = newLoggedTime + totalLoggedTime;

        if (totalLoggedTime + newLoggedTime > 24 * 60)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Total logged time exceeds 24 hours for the day: " + time);

        DAO.addUserTimeManagement(timeManagementNew);
        return ResponseEntity.ok().body("Added time management record");

    }

    public boolean deleteUserTimeManagement(int timeId) {
        return DAO.deleteUserTimeManagement(timeId) == 1;
    }

    public boolean addNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.userId())
                .taskId(request.taskId())
                .isSeen(0)
                .build();
        var notifications = DAO.getAllNotifications();

        Optional<Integer> halfduplicate = notifications
                .map(list -> list.stream()
                        .filter(notif -> (notif.getTaskId() == request.taskId()))
                        .findFirst()
                        .map(Notification::getNotificationId))
                .orElse(Optional.empty());

        Optional<Integer> duplicate = notifications
                .map(list -> list.stream()
                        .filter(notif -> (notif.getTaskId() == request.taskId()) && (notif.getUserId() == request.userId()))
                        .findFirst()
                        .map(Notification::getNotificationId))
                .orElse(Optional.empty());

        if (duplicate.isPresent())
            return true;

        if (halfduplicate.isPresent()) {
            DAO.deleteNotification(halfduplicate.get());
            DAO.addNotification(notification);
            return false;
        }

        DAO.addNotification(notification);
        return true;
    }

    public ResponseEntity<?> addTagToBoard(int boardId, String name) {
        if (name.length() <= 140) {
            DAO.addTagToBoard(boardId, name);
            return ResponseEntity.ok().body("Added tag to board");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tag length should be not more than 140 characters");
    }

    @Transactional
    public Optional<Integer> addTaskToBoard(CreateTaskRequest request) {

        if (request.description().length() >= 1440) {
            return Optional.empty();
        }

        Task task = Task.builder()
                .title(request.title())
                .assigneeId(request.assigneeId())
                .reporterId(request.reporterId())
                .boardId(request.boardId())
                .priority(request.priority())
                .estimation(request.estimation())
                .status(request.status())
                .severity(request.severity())
                .description(request.description())
                .creationTime(request.creationTime())
                .build();

        var taskId = DAO.addTaskToBoard(task);
        if (!request.tagIds().isEmpty())
            DAO.addTaskTags(taskId, request.tagIds());

        return Optional.of(taskId);
    }


    @Transactional
    public boolean deleteTask(int taskId) {
        DAO.deleteTaskTimeManagement(taskId);
        DAO.deleteTaskNotification(taskId);
        DAO.deleteTaskTags(taskId);
        return DAO.deleteTask(taskId) == 1;
    }

    @Transactional
    public Optional<Integer> addBoard(String name, int userId) {
        if (name != null && !name.trim().isEmpty() && name.trim().length() <= 140) {
            var boardId = DAO.addBoard(name.trim());
            DAO.addUserToBoardOwner(boardId, userId);
            return Optional.of(boardId);
        }
        return Optional.empty();
    }


    public boolean addUserToBoardManagers(int boardId, int userId) {
        var alreadyOnTheBoard = DAO.checkIfUserOnABoard(userId, boardId);
        if (!alreadyOnTheBoard) {
            DAO.addUserToBoardManagers(boardId, userId);
            return true;
        }
        return false;
    }

    public boolean removeUserFromBoardManagers(int boardId, int userId) {
        return DAO.removeUserFromBoardManagers(boardId, userId) == 1;
    }

    public boolean addUserToBoardEmployees(int boardId, int userId) {
        var alreadyOnTheBoard = DAO.checkIfUserOnABoard(userId, boardId);
        if (!alreadyOnTheBoard) {
            DAO.addUserToBoardEmployees(boardId, userId);
            return true;
        }
        return false;
    }

    public boolean removeUserFromBoardEmployees(int boardId, int userId) {
        return DAO.removeUserFromBoardEmployees(boardId, userId) == 1;
    }

}
