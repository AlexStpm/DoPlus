package ltd.future.planning.tech.DoPlus.dao;

import lombok.RequiredArgsConstructor;
import ltd.future.planning.tech.DoPlus.entity.*;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DAO {
    private final JdbcTemplate jdbcTemplate;

    public boolean checkIfUserOnABoard(int userId, int boardId) {
        return jdbcTemplate.query("SELECT * FROM BoardMembers WHERE user_id = ? AND board_id = ?",
                (resultSet) -> {
                    if (!resultSet.next()) {
                        return false;
                    }
                    return true;
                }, userId, boardId);
    }

    public Optional<User> getUserById(int id) {
        return this.jdbcTemplate.query("SELECT * " +
                        "FROM Users " +
                        "WHERE user_id = " + id,
                (resultSet) -> {
                    if (resultSet.next()) {
                        return Optional.of(new User(
                                resultSet.getInt("user_id"),
                                resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("first_name"),
                                resultSet.getString("last_name"),
                                resultSet.getInt("role_id"),
                                resultSet.getInt("is_active")));
                    }
                    return Optional.empty();
                });
    }

    @Transactional
    public Optional<User> getUserByUsername(String username) {
        return this.jdbcTemplate.query("SELECT * " +
                        "FROM Users " +
                        "WHERE username = '" + username + "'",
                (resultSet) -> {
                    if (resultSet.next()) {
                        return Optional.of(new User(
                                resultSet.getInt("user_id"),
                                resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("first_name"),
                                resultSet.getString("last_name"),
                                resultSet.getInt("role_id"),
                                resultSet.getInt("is_active")));
                    }
                    return Optional.empty();
                });
    }

    @Transactional
    public void addUser(User user) {
        String sql = "INSERT INTO Users (username, password, first_name, last_name, role_id, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                user.getUsername(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoleId(),
                user.getIsActive()
        );
    }

    @Transactional
    public void addNotification(Notification notification) {
        String sql = "INSERT INTO Notifications (user_id, task_id, is_seen) VALUES (?, ?, 0)";
        jdbcTemplate.update(sql, notification.getUserId(), notification.getTaskId());
    }

    @Transactional
    public void addTagToBoard(int boardId, String name) {
        String sql = "INSERT INTO Tags (board_id, name, is_active) VALUES (?, ?, 1)";
        jdbcTemplate.update(sql, boardId, name);
    }

    @Transactional
    public int addTaskToBoard(Task task) {
        String sql = "INSERT INTO Tasks (title, assignee_id, reporter_id, board_id, priority, severity, estimation, status, description, creation_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                task.getTitle(),
                task.getAssigneeId(),
                task.getReporterId(),
                task.getBoardId(),
                task.getPriority(),
                task.getSeverity(),
                task.getEstimation(),
                task.getStatus(),
                task.getDescription(),
                task.getCreationTime());
        String queryLastId = "SELECT last_insert_rowid()";
        return jdbcTemplate.queryForObject(queryLastId, Integer.class);
    }

    @Transactional
    public int deleteTask(int taskId) {
        String sql = "DELETE FROM Tasks WHERE task_id = ?";
        return jdbcTemplate.update(sql, taskId);
    }

    @Transactional
    public int addBoard(String boardName) {
        String insertSql = "INSERT INTO Boards (name, is_archived) VALUES (?, 0)";
        jdbcTemplate.update(insertSql, boardName);

        String queryLastId = "SELECT last_insert_rowid()";
        return jdbcTemplate.queryForObject(queryLastId, Integer.class);
    }

    @Transactional
    public void addUserToBoardManagers(int boardId, int userId) {
        String sql = "INSERT INTO BoardMembers (board_id, user_id, role_on_board) VALUES (?, ?, 'Manager')";
        jdbcTemplate.update(sql, boardId, userId);
    }

    @Transactional
    public int removeUserFromBoardManagers(int boardId, int userId) {
        String sql = "DELETE FROM BoardMembers WHERE board_id = ? AND user_id = ? AND role_on_board = 'Manager';";
        return jdbcTemplate.update(sql, boardId, userId);
    }

    @Transactional
    public void addUserToBoardEmployees(int boardId, int userId) {
        String sql = "INSERT INTO BoardMembers (board_id, user_id, role_on_board) VALUES (?, ?, 'Employee')";
        jdbcTemplate.update(sql, boardId, userId);
    }

    @Transactional
    public int removeUserFromBoardEmployees(int boardId, int userId) {
        String sql = "DELETE FROM BoardMembers WHERE board_id = ? AND user_id = ? AND role_on_board = 'Employee';";
        return jdbcTemplate.update(sql, boardId, userId);
    }

    @Transactional
    public void addUserToBoardOwner(int boardId, int userId) {
        String sql = "INSERT INTO BoardMembers (board_id, user_id, role_on_board) VALUES (?, ?, 'Owner')";
        jdbcTemplate.update(sql, boardId, userId);
    }

    public Optional<List<User>> getAllUsers() {
        try {
            return Optional.ofNullable(this.jdbcTemplate.query("SELECT * " +
                            "FROM Users ",
                    (resultSet) -> {
                        List<User> list = new ArrayList<>();
                        while (resultSet.next()) {
                            list.add(new User(
                                    resultSet.getInt("user_id"),
                                    resultSet.getString("username"),
                                    resultSet.getString("password"),
                                    resultSet.getString("first_name"),
                                    resultSet.getString("last_name"),
                                    resultSet.getInt("role_id"),
                                    resultSet.getInt("is_active"))
                            );
                        }
                        return list;
                    }));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    @Transactional
    public Optional<Tag> getTagById(int tagId) {
        return this.jdbcTemplate.query("SELECT * " +
                        "FROM Tags " +
                        "WHERE tag_id = " + tagId,
                (resultSet) -> {
                    if (resultSet.next()) {
                        return Optional.of(new Tag(
                                resultSet.getInt("tag_id"),
                                resultSet.getInt("board_id"),
                                resultSet.getString("name"),
                                resultSet.getInt("is_active")));
                    }
                    return Optional.empty();
                });
    }

    @Transactional
    public Optional<Notification> getNotificationById(int notificationId) {
        return this.jdbcTemplate.query("SELECT *" +
                        "FROM Notifications " +
                        "WHERE Notifications.notification_id = " + notificationId,
                (resultSet) -> {
                    if (resultSet.next()) {
                        return Optional.of(new Notification(
                                resultSet.getInt("notification_id"),
                                resultSet.getInt("user_id"),
                                resultSet.getInt("task_id"),
                                resultSet.getInt("is_seen")
                        ));
                    }
                    return Optional.empty();
                });
    }

    @Transactional
    public Optional<List<Notification>> getAllNotifications() {
        try {
            String sql = "SELECT *" +
                    "FROM Notifications ;";
            List<Notification> notificationList = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                Notification notification = new Notification();
                notification.setNotificationId(resultSet.getInt("notification_id"));
                notification.setUserId(resultSet.getInt("user_id"));
                notification.setTaskId(resultSet.getInt("task_id"));
                notification.setIsSeen(resultSet.getInt("is_seen"));
                return notification;
            });

            return Optional.ofNullable(notificationList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<TimeManagement> getTimeManagementById(int id) {
        return this.jdbcTemplate.query("SELECT * " +
                        "FROM TimeManagement " +
                        "WHERE TimeManagement_id = " + id,
                (resultSet) -> {
                    if (resultSet.next()) {
                        return Optional.of(new TimeManagement(
                                resultSet.getInt("TimeManagement_id"),
                                resultSet.getInt("user_id"),
                                resultSet.getString("date_time"),
                                resultSet.getInt("task_id"),
                                resultSet.getString("description")));
                    }
                    return Optional.empty();
                });
    }

    @Transactional
    public Integer getTotalLoggedTimeForTaskOnDate(int userId, String date, int excludeTimeManagementId) {
        String sql = "SELECT SUM(strftime('%H', date_time) * 60 + strftime('%M', date_time)) as totalMinutes " +
                "FROM TimeManagement " +
                "WHERE user_id = ? AND date(date_time) = date(?) AND TimeManagement_id <> ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{
                userId, date, excludeTimeManagementId}, (rs, rowNum) -> rs.getInt("totalMinutes"));
    }

    @Transactional
    public void updateUser(int id, String username, String firstName, String lastName, int isActive) {
        this.jdbcTemplate.update("UPDATE Users " +
                "SET username = '" + username + "', " +
                "first_name = '" + firstName + "', " +
                "last_name = '" + lastName + "', " +
                "is_active = '" + isActive + "' " +
                "WHERE user_id = " + id
        );
    }

    @Transactional
    public void updateBoard(int boardId, String name) {
        String sql = "UPDATE Boards SET name = ? WHERE board_id = ?";
        Object[] params = {name, boardId};
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void activateTag(int tagId) {
        String sql = "UPDATE Tags SET is_active = 1 WHERE tag_id = ?";
        Object[] params = {tagId};
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void deactivateTag(int tagId) {
        String sql = "UPDATE Tags SET is_active = 0 WHERE tag_id = ?";
        Object[] params = {tagId};
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void archiveBoard(int boardId) {
        String sql = "UPDATE Boards SET is_archived = 1 WHERE board_id = ?";
        Object[] params = {boardId};
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void updateTask(Task task) {
        String sql = "UPDATE Tasks SET " +
                "title = ?, " +
                "assignee_id = ?, " +
                "reporter_id = ?, " +
                "priority = ?, " +
                "severity = ?, " +
                "estimation = ?, " +
                "status = ?, " +
                "description = ? " +
                "WHERE task_id = ?";

        Object[] params = {
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

        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void updateTaskStatus(int taskId, String statusName) {
        String sql = "UPDATE Tasks SET status = ? WHERE task_id = ?";
        Object[] params = {statusName, taskId};
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void setSeenNotification(int notificationId) {
        String sql = "UPDATE Notifications SET is_seen = 1 WHERE notification_id = ?";
        Object[] params = {notificationId};
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void updateUserTimeManagement(int id, String dateTime, int taskId, String description) {
        String sql = "UPDATE TimeManagement SET date_time = ?, task_id = ?, description = ? WHERE TimeManagement_id = ?";
        Object[] params = {dateTime, taskId, description, id};
        this.jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void deleteNotification(int nId) {
        String deleteSql = "DELETE FROM Notifications WHERE notification_id = ?";
        jdbcTemplate.update(deleteSql, nId);
    }

    @Transactional
    public void deleteTaskTags(int taskId) {
        String deleteSql = "DELETE FROM TaskTags WHERE task_id = ?";
        jdbcTemplate.update(deleteSql, taskId);
    }

    @Transactional
    public void addTaskTags(int taskId, List<Integer> tagIds) {
        String insertSql = "INSERT INTO TaskTags (task_id, tag_id) VALUES (?, ?)";
        for (Integer tagId : tagIds) {
            jdbcTemplate.update(insertSql, taskId, tagId);
        }
    }

    public Optional<List<Board>> getUserBoards(int userId) {
        try {
            String sql = "SELECT b.* FROM Boards b " +
                    "JOIN BoardMembers bm ON b.board_id = bm.board_id " +
                    "WHERE bm.user_id = ?";
            List<Board> boards = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                Board board = new Board();
                board.setBoardId(resultSet.getInt("board_id"));
                board.setName(resultSet.getString("name"));
                board.setIsArchived(resultSet.getInt("is_archived"));

                return board;
            }, userId);

            return Optional.ofNullable(boards);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<Board> getBoard(int boardId) {
        try {
            String sql = "SELECT * FROM Boards WHERE board_id = ?";
            Board board = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
                Board resultBoard = new Board();
                resultBoard.setBoardId(resultSet.getInt("board_id"));
                resultBoard.setName(resultSet.getString("name"));
                resultBoard.setIsArchived(resultSet.getInt("is_archived"));

                return resultBoard;
            }, boardId);

            return Optional.ofNullable(board);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<List<Task>> getBoardTasks(int boardId) {
        String sql = "SELECT * FROM Tasks WHERE board_id = ?";
        try {
            List<Task> tasks = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                Task task = new Task();
                task.setTitle(resultSet.getString("title"));
                task.setTaskId(resultSet.getInt("task_id"));
                task.setAssigneeId(resultSet.getInt("assignee_id"));
                task.setReporterId(resultSet.getInt("reporter_id"));
                task.setBoardId(resultSet.getInt("board_id"));
                task.setPriority(resultSet.getString("priority"));
                task.setSeverity(resultSet.getString("severity"));
                task.setEstimation(resultSet.getInt("estimation"));
                task.setStatus(resultSet.getString("status"));
                task.setDescription(resultSet.getString("description"));
                task.setCreationTime(resultSet.getString("creation_time"));
                return task;
            }, boardId);

            return Optional.of(tasks);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<User> getBoardOwner(int boardId) {
        String sql = "SELECT u.*  " +
                "FROM Users u " +
                "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                "JOIN Boards b ON b.board_id = bm.board_id " +
                "WHERE bm.role_on_board = 'Owner' AND b.board_id = ?; ?";
        try {


            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setRoleId(resultSet.getInt("role_id"));
                user.setIsActive(resultSet.getInt("is_active"));

                return user;
            }, boardId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<List<User>> getBoardManagers(int boardId) {
        String sql = "SELECT u.* FROM Users u " +
                "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                "WHERE bm.board_id = ? AND bm.role_on_board = 'Manager'";
        try {
            List<User> managers = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setRoleId(resultSet.getInt("role_id"));
                user.setIsActive(resultSet.getInt("is_active"));
                return user;
            }, boardId);

            return Optional.of(managers);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<List<User>> getBoardEmployees(int boardId) {
        String sql = "SELECT u.* FROM Users u " +
                "JOIN BoardMembers bm ON u.user_id = bm.user_id " +
                "WHERE bm.board_id = ? AND bm.role_on_board = 'Employee'";
        try {
            List<User> employees = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                User user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setRoleId(resultSet.getInt("role_id"));
                user.setIsActive(resultSet.getInt("is_active"));
                return user;
            }, boardId);

            return Optional.of(employees);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<List<Tag>> getAllBoardTags(int boardId) {
        String sql = "SELECT t.* FROM Tags t WHERE t.board_id = ?";
        try {
            List<Tag> tags = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                Tag tag = new Tag();
                tag.setTagId(resultSet.getInt("tag_id"));
                tag.setBoardId(resultSet.getInt("board_id"));
                tag.setName(resultSet.getString("name"));
                tag.setIsActive(resultSet.getInt("is_active"));
                return tag;
            }, boardId);

            return Optional.of(tags);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Task> getTaskById(int taskId) {
        String sql = "SELECT * FROM Tasks WHERE task_id = ?";
        try {
            Task task = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
                Task fetchedTask = new Task();
                fetchedTask.setTitle(resultSet.getString("title"));
                fetchedTask.setTaskId(resultSet.getInt("task_id"));
                fetchedTask.setAssigneeId(resultSet.getInt("assignee_id"));
                fetchedTask.setReporterId(resultSet.getInt("reporter_id"));
                fetchedTask.setBoardId(resultSet.getInt("board_id"));
                fetchedTask.setPriority(resultSet.getString("priority"));
                fetchedTask.setSeverity(resultSet.getString("severity"));
                fetchedTask.setEstimation(resultSet.getInt("estimation"));
                fetchedTask.setStatus(resultSet.getString("status"));
                fetchedTask.setDescription(resultSet.getString("description"));
                fetchedTask.setCreationTime(resultSet.getString("creation_time"));
                return fetchedTask;
            }, taskId);

            return Optional.ofNullable(task);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<List<Notification>> getUserNotifications(int userId) {
        try {
            String sql = "SELECT *" +
                    "FROM Notifications " +
                    "WHERE Notifications.user_id = ?;";
            List<Notification> notificationList = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                Notification notification = new Notification();
                notification.setNotificationId(resultSet.getInt("notification_id"));
                notification.setUserId(resultSet.getInt("user_id"));
                notification.setTaskId(resultSet.getInt("task_id"));
                notification.setIsSeen(resultSet.getInt("is_seen"));
                return notification;
            }, userId);

            return Optional.ofNullable(notificationList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public Optional<List<TimeManagement>> getTimeManagementData(int userId) {
        try {
            String sql = "SELECT * FROM TimeManagement WHERE user_id = ?";
            List<TimeManagement> timeManagementList = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                TimeManagement timeManagement = new TimeManagement();
                timeManagement.setTimeManagementId(resultSet.getInt("TimeManagement_id"));
                timeManagement.setUserId(resultSet.getInt("user_id"));
                timeManagement.setDateTime(resultSet.getString("date_time"));
                timeManagement.setTaskId(resultSet.getInt("task_id"));
                timeManagement.setDescription(resultSet.getString("description"));

                return timeManagement;
            }, userId);

            return Optional.ofNullable(timeManagementList);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public List<Integer> getTaskTagsId(int taskId) {
        String sql = "SELECT * FROM TaskTags WHERE task_id = ?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("tag_id"), taskId);
    }

    @Transactional
    public void addUserTimeManagement(TimeManagement timeManagement) {
        String sql = "INSERT INTO TimeManagement (user_id, date_time, task_id, description) " +
                "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                timeManagement.getUserId(),
                timeManagement.getDateTime(),
                timeManagement.getTaskId(),
                timeManagement.getDescription()
        );
    }

    @Transactional
    public int deleteUserTimeManagement(int timeId) {
        String sql = "DELETE FROM TimeManagement WHERE TimeManagement_id = ?";
        return jdbcTemplate.update(sql, timeId);
    }

    @Transactional
    public int deleteTaskTimeManagement(int taskId) {
        String sql = "DELETE FROM TimeManagement WHERE task_id = ?";
        return jdbcTemplate.update(sql, taskId);
    }

    @Transactional
    public int deleteTaskNotification(int taskId) {
        String sql = "DELETE FROM Notifications WHERE task_id = ?";
        return jdbcTemplate.update(sql, taskId);
    }

}
