package com.uxelf.TasksApp.service;

import com.uxelf.TasksApp.dto.tasks.CreateTaskRequest;
import com.uxelf.TasksApp.dto.tasks.TaskResponse;
import com.uxelf.TasksApp.dto.tasks.UpdateTaskRequest;
import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.User;
import com.uxelf.TasksApp.entity.enums.TaskStatus;
import com.uxelf.TasksApp.exception.BusinessException;
import com.uxelf.TasksApp.repository.TaskRepository;
import com.uxelf.TasksApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID userId;
    private User user;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        user = new User(userId, "testuser", "password123");
    }

    @Nested
    @DisplayName("createTask Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task successfully with valid data")
        void shouldCreateTaskSuccessfully() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test Task");
            request.setDescription("Test Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            Task savedTask = new Task(
                    taskId,  // ← Usa el constructor con ID
                    request.getTitle(),
                    request.getDescription(),
                    request.getStart(),
                    request.getEnd(),
                    user
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            TaskResponse response = taskService.createTask(request, userId);

            // Then
            assertNotNull(response);
            assertEquals(taskId, response.getId());
            assertEquals("Test Task", response.getTitle());
            assertEquals("Test Description", response.getDescription());
            assertEquals(TaskStatus.PENDING, response.getStatus());
            assertFalse(response.isExpired());

            verify(userRepository, times(1)).findById(userId);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should create task that ends today")
        void shouldCreateTaskThatEndsToday() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Task ending today");
            request.setDescription("Description");
            request.setStart(LocalDate.now().minusDays(2));
            request.setEnd(LocalDate.now());

            Task savedTask = new Task(
                    taskId,  // ← Usa el constructor con ID
                    request.getTitle(),
                    request.getDescription(),
                    request.getStart(),
                    request.getEnd(),
                    user
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            TaskResponse response = taskService.createTask(request, userId);

            // Then
            assertNotNull(response);
            assertEquals(LocalDate.now(), response.getEnd());
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when end date is before start date")
        void shouldThrowExceptionWhenEndBeforeStart() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Invalid Task");
            request.setDescription("Description");
            request.setStart(LocalDate.now().plusDays(5));
            request.setEnd(LocalDate.now());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );

            assertEquals("End date must be after start date", exception.getMessage());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when end date is in the past")
        void shouldThrowExceptionWhenEndDateInPast() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Past Task");
            request.setDescription("Description");
            request.setStart(LocalDate.now().minusDays(10));
            request.setEnd(LocalDate.now().minusDays(5));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );

            assertEquals("End date must be in the future", exception.getMessage());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test Task");
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.createTask(request, userId)
            );

            assertEquals("User not found", exception.getMessage());
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("getTaskById Tests")
    class GetTaskByIdTests {

        @Test
        @DisplayName("Should get task by id successfully")
        void shouldGetTaskByIdSuccessfully() {
            // Given
            Task task = createTask("Test Task", LocalDate.now(), LocalDate.now().plusDays(5));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            TaskResponse response = taskService.getTaskById(taskId, userId);

            // Then
            assertNotNull(response);
            assertEquals(taskId, response.getId());
            assertEquals("Test Task", response.getTitle());
            verify(taskRepository, times(1)).findById(taskId);
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.getTaskById(taskId, userId)
            );

            assertEquals("Task not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user doesn't have permission")
        void shouldThrowExceptionWhenUserDoesntHavePermission() {
            // Given
            UUID differentUserId = UUID.randomUUID();
            Task task = createTask("Test Task", LocalDate.now(), LocalDate.now().plusDays(5));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.getTaskById(taskId, differentUserId)
            );

            assertEquals("You don't have permission to see this task", exception.getMessage());
        }

        @Test
        @DisplayName("Should mark task as expired when end date is in past and not completed")
        void shouldMarkTaskAsExpired() {
            // Given
            Task task = createTask("Expired Task", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
            task.setStatus(TaskStatus.PENDING);
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            TaskResponse response = taskService.getTaskById(taskId, userId);

            // Then
            assertTrue(response.isExpired());
        }

        @Test
        @DisplayName("Should not mark completed task as expired")
        void shouldNotMarkCompletedTaskAsExpired() {
            // Given
            Task task = createTask("Completed Task", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
            task.setStatus(TaskStatus.COMPLETED);
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            TaskResponse response = taskService.getTaskById(taskId, userId);

            // Then
            assertFalse(response.isExpired());
        }
    }

    @Nested
    @DisplayName("getTasksByUser Tests")
    class GetTasksByUserTests {

        @Test
        @DisplayName("Should get all tasks for user")
        void shouldGetAllTasksForUser() {
            // Given
            Task task1 = createTask("Task 1", LocalDate.now(), LocalDate.now().plusDays(5));
            Task task2 = createTask("Task 2", LocalDate.now(), LocalDate.now().plusDays(10));
            List<Task> tasks = Arrays.asList(task1, task2);

            when(taskRepository.findByAuthorId(userId)).thenReturn(tasks);

            // When
            List<TaskResponse> responses = taskService.getTasksByUser(userId);

            // Then
            assertNotNull(responses);
            assertEquals(2, responses.size());
            assertEquals("Task 1", responses.get(0).getTitle());
            assertEquals("Task 2", responses.get(1).getTitle());
            verify(taskRepository, times(1)).findByAuthorId(userId);
        }

        @Test
        @DisplayName("Should return empty list when user has no tasks")
        void shouldReturnEmptyListWhenNoTasks() {
            // Given
            when(taskRepository.findByAuthorId(userId)).thenReturn(List.of());

            // When
            List<TaskResponse> responses = taskService.getTasksByUser(userId);

            // Then
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTaskForDay Tests")
    class GetTaskForDayTests {

        @Test
        @DisplayName("Should get tasks for specific day")
        void shouldGetTasksForSpecificDay() {
            // Given
            LocalDate targetDate = LocalDate.now().plusDays(3);
            Task task1 = createTask("Task 1", LocalDate.now(), LocalDate.now().plusDays(5));
            Task task2 = createTask("Task 2", LocalDate.now().plusDays(2), LocalDate.now().plusDays(4));
            List<Task> tasks = Arrays.asList(task1, task2);

            when(taskRepository.findTasksOverlappingDay(userId, targetDate)).thenReturn(tasks);

            // When
            List<TaskResponse> responses = taskService.getTaskForDay(userId, targetDate);

            // Then
            assertNotNull(responses);
            assertEquals(2, responses.size());
            verify(taskRepository, times(1)).findTasksOverlappingDay(userId, targetDate);
        }

        @Test
        @DisplayName("Should return empty list when no tasks for day")
        void shouldReturnEmptyListWhenNoTasksForDay() {
            // Given
            LocalDate targetDate = LocalDate.now().plusDays(100);
            when(taskRepository.findTasksOverlappingDay(userId, targetDate)).thenReturn(List.of());

            // When
            List<TaskResponse> responses = taskService.getTaskForDay(userId, targetDate);

            // Then
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTaskForMonth Tests")
    class GetTaskForMonthTests {

        @Test
        @DisplayName("Should get tasks for specific month")
        void shouldGetTasksForSpecificMonth() {
            // Given
            YearMonth yearMonth = YearMonth.now();
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();

            Task task1 = createTask("Task 1", monthStart, monthStart.plusDays(5));
            Task task2 = createTask("Task 2", monthStart.plusDays(10), monthEnd);
            List<Task> tasks = Arrays.asList(task1, task2);

            when(taskRepository.findTasksOverlappingMonth(userId, monthStart, monthEnd)).thenReturn(tasks);

            // When
            List<TaskResponse> responses = taskService.getTaskForMonth(userId, yearMonth);

            // Then
            assertNotNull(responses);
            assertEquals(2, responses.size());
            verify(taskRepository, times(1)).findTasksOverlappingMonth(userId, monthStart, monthEnd);
        }

        @Test
        @DisplayName("Should return empty list when no tasks for month")
        void shouldReturnEmptyListWhenNoTasksForMonth() {
            // Given
            YearMonth yearMonth = YearMonth.now().plusMonths(6);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();

            when(taskRepository.findTasksOverlappingMonth(userId, monthStart, monthEnd)).thenReturn(List.of());

            // When
            List<TaskResponse> responses = taskService.getTaskForMonth(userId, yearMonth);

            // Then
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateTask Tests")
    class UpdateTaskTests {

        @Test
        @DisplayName("Should update task title successfully")
        void shouldUpdateTaskTitleSuccessfully() {
            // Given
            Task task = createTask("Old Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("New Title");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertNotNull(response);
            assertEquals("New Title", response.getTitle());
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("Should update task description successfully")
        void shouldUpdateTaskDescriptionSuccessfully() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setDescription("New Description");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertNotNull(response);
            assertEquals("New Description", response.getDescription());
        }

        @Test
        @DisplayName("Should update task status successfully")
        void shouldUpdateTaskStatusSuccessfully() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setStatus(TaskStatus.COMPLETED);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertEquals(TaskStatus.COMPLETED, response.getStatus());
        }

        @Test
        @DisplayName("Should update start date successfully")
        void shouldUpdateStartDateSuccessfully() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(10));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setStart(LocalDate.now().plusDays(2));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertEquals(LocalDate.now().plusDays(2), response.getStart());
        }

        @Test
        @DisplayName("Should update end date successfully")
        void shouldUpdateEndDateSuccessfully() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setEnd(LocalDate.now().plusDays(15));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertEquals(LocalDate.now().plusDays(15), response.getEnd());
        }

        @Test
        @DisplayName("Should update multiple fields successfully")
        void shouldUpdateMultipleFieldsSuccessfully() {
            // Given
            Task task = createTask("Old Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("New Title");
            request.setDescription("New Description");
            request.setStatus(TaskStatus.IN_PROGRESS);
            request.setStart(LocalDate.now().plusDays(1));
            request.setEnd(LocalDate.now().plusDays(6));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertNotNull(response);
            assertEquals("New Title", response.getTitle());
            assertEquals("New Description", response.getDescription());
            assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when title is blank")
        void shouldThrowExceptionWhenTitleIsBlank() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("   ");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.updateTask(taskId, request, userId)
            );

            assertEquals("Title can't be empty", exception.getMessage());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when end date is before start date after update")
        void shouldThrowExceptionWhenEndBeforeStartAfterUpdate() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setEnd(LocalDate.now().minusDays(1));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.updateTask(taskId, request, userId)
            );

            assertEquals("End date must be after start date", exception.getMessage());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("New Title");

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.updateTask(taskId, request, userId)
            );

            assertEquals("Task not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user doesn't have permission to update")
        void shouldThrowExceptionWhenUserDoesntHavePermissionToUpdate() {
            // Given
            UUID differentUserId = UUID.randomUUID();
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("New Title");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.updateTask(taskId, request, differentUserId)
            );

            assertEquals("You don't have permission to modify this task", exception.getMessage());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should allow updating task to end in the past")
        void shouldAllowUpdatingTaskToEndInPast() {
            // Given
            Task task = createTask("Title", LocalDate.now().minusDays(10), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setEnd(LocalDate.now().minusDays(1));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertNotNull(response);
            assertEquals(LocalDate.now().minusDays(1), response.getEnd());
            verify(taskRepository, times(1)).save(task);
        }
    }

    @Nested
    @DisplayName("deleteTask Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should delete task successfully")
        void shouldDeleteTaskSuccessfully() {
            // Given
            Task task = createTask("Task to delete", LocalDate.now(), LocalDate.now().plusDays(5));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            taskService.deleteTask(taskId, userId);

            // Then
            verify(taskRepository, times(1)).delete(task);
        }

        @Test
        @DisplayName("Should throw exception when task not found for deletion")
        void shouldThrowExceptionWhenTaskNotFoundForDeletion() {
            // Given
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.deleteTask(taskId, userId)
            );

            assertEquals("Task not found", exception.getMessage());
            verify(taskRepository, never()).delete(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when user doesn't have permission to delete")
        void shouldThrowExceptionWhenUserDoesntHavePermissionToDelete() {
            // Given
            UUID differentUserId = UUID.randomUUID();
            Task task = createTask("Task", LocalDate.now(), LocalDate.now().plusDays(5));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.deleteTask(taskId, differentUserId)
            );

            assertEquals("You don't have permission to modify this task", exception.getMessage());
            verify(taskRepository, never()).delete(any(Task.class));
        }
    }

    // ⬇️⬇️⬇️ HELPER METHOD - VA AL FINAL DE LA CLASE, DESPUÉS DE TODOS LOS @Nested ⬇️⬇️⬇️
    /**
     * Helper method para crear Tasks en los tests
     * Usa el constructor package-private de Task que permite setear el ID
     */
    private Task createTask(String title, LocalDate start, LocalDate end) {
        return new Task(taskId, title, "Description", start, end, user);
    }
    // ⬆️⬆️⬆️ FIN DEL HELPER METHOD ⬆️⬆️⬆️
}