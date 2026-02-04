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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService - Edge Cases and Validation Tests")
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
    @DisplayName("createTask - Edge Cases & Validation")
    class CreateTaskEdgeCases {

        @Test
        @DisplayName("Should throw exception when title is only whitespace")
        void shouldThrowExceptionWhenTitleIsOnlyWhitespace() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("   ");
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );
            assertEquals("Title can't be empty or whitespace", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when title is empty string")
        void shouldThrowExceptionWhenTitleIsEmpty() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("");
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );
            assertEquals("Title can't be empty or whitespace", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when title exceeds 255 characters")
        void shouldThrowExceptionWhenTitleTooLong() {
            // Given
            String veryLongTitle = "a".repeat(300);
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(veryLongTitle);
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            // When / Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );

            assertEquals("Title cannot exceed 255 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when description exceeds 5000 characters")
        void shouldThrowExceptionWhenDescriptionTooLong() {
            // Given
            String veryLongDescription = "a".repeat(5001);
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Title");
            request.setDescription(veryLongDescription);
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );
            assertEquals("Description cannot exceed 5000 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Title");
            request.setDescription(null);
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            Task savedTask = new Task(taskId, "Title", null,
                    request.getStart(), request.getEnd(), user);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            TaskResponse response = taskService.createTask(request, userId);

            // Then
            assertNotNull(response);
            assertNull(response.getDescription());
        }

        @Test
        @DisplayName("Should allow task with start date equal to end date")
        void shouldAllowWhenStartEqualsEnd() {
            // Given
            LocalDate sameDate = LocalDate.now().plusDays(5);
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Title");
            request.setDescription("Description");
            request.setStart(sameDate);
            request.setEnd(sameDate);

            Task savedTask = new Task(taskId, "Title", "Description",
                    sameDate, sameDate, user);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            TaskResponse response = taskService.createTask(request, userId);

            // Then
            assertNotNull(response);
            assertEquals(sameDate, response.getStart());
            assertEquals(sameDate, response.getEnd());
        }

        @Test
        @DisplayName("Should throw exception when end date is too far in the future")
        void shouldThrowExceptionForVeryDistantFutureDate() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Future Task");
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusYears(100));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );
            assertEquals("End date cannot be more than 10 years in the future", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when start date is null")
        void shouldThrowExceptionWhenStartDateIsNull() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Title");
            request.setDescription("Description");
            request.setStart(null);
            request.setEnd(LocalDate.now().plusDays(5));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );
            assertEquals("Start date cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when end date is null")
        void shouldThrowExceptionWhenEndDateIsNull() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Title");
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(null);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createTask(request, userId)
            );
            assertEquals("End date cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle special characters in title")
        void shouldHandleSpecialCharactersInTitle() {
            // Given
            String titleWithSpecialChars = "Task @#$%^&*()_+-=[]{}|;':\",./<>?";
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(titleWithSpecialChars);
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            Task savedTask = new Task(taskId, titleWithSpecialChars, "Description",
                    request.getStart(), request.getEnd(), user);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            TaskResponse response = taskService.createTask(request, userId);

            // Then
            assertNotNull(response);
            assertEquals(titleWithSpecialChars, response.getTitle());
        }

        @Test
        @DisplayName("Should handle emoji and unicode in title")
        void shouldHandleEmojiInTitle() {
            // Given
            String titleWithEmoji = "Task 🚀 📝 ✅ 中文";
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(titleWithEmoji);
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            Task savedTask = new Task(taskId, titleWithEmoji, "Description",
                    request.getStart(), request.getEnd(), user);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            TaskResponse response = taskService.createTask(request, userId);

            // Then
            assertNotNull(response);
            assertEquals(titleWithEmoji, response.getTitle());
        }

        @Test
        @DisplayName("Should throw exception with invalid UUID")
        void shouldThrowExceptionWithInvalidUserId() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Title");
            request.setDescription("Description");
            request.setStart(LocalDate.now());
            request.setEnd(LocalDate.now().plusDays(5));

            UUID invalidUserId = UUID.randomUUID();
            when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.createTask(request, invalidUserId)
            );
            assertEquals("User not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updateTask - Edge Cases & Validation")
    class UpdateTaskEdgeCases {

        @Test
        @DisplayName("Should throw exception when updating title to whitespace only")
        void shouldThrowExceptionWhenUpdatingTitleToWhitespace() {
            // Given
            Task task = createTask("Original Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("   ");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.updateTask(taskId, request, userId)
            );
            assertEquals("Title can't be empty or whitespace", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating title to empty string")
        void shouldThrowExceptionWhenUpdatingTitleToEmpty() {
            // Given
            Task task = createTask("Original Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.updateTask(taskId, request, userId)
            );
            assertEquals("Title can't be empty or whitespace", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle not updating the description with null")
        void shouldHandleNotUpdatingDescription() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            task.setDescription("Original Description");
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setDescription(null);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertNotNull(response);
            assertEquals("Original Description", response.getDescription());
        }

        @Test
        @DisplayName("Should handle updating only start date without changing end")
        void shouldHandleUpdatingOnlyStartDate() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(10));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setStart(LocalDate.now().plusDays(2));  // Solo actualiza start

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertNotNull(response);
            assertEquals(LocalDate.now().plusDays(2), response.getStart());
            assertEquals(LocalDate.now().plusDays(10), response.getEnd());
        }

        @Test
        @DisplayName("Should throw exception when updating start to be after end")
        void shouldThrowExceptionWhenUpdatingStartToBeAfterEnd() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setStart(LocalDate.now().plusDays(10));  // Después del end actual

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.updateTask(taskId, request, userId)
            );
            assertEquals("End date must be after start date", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle updating task with all fields null (no changes)")
        void shouldHandleUpdateWithAllFieldsNull() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            // Todos los campos null = no hay cambios

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertNotNull(response);
            assertEquals("Title", response.getTitle());
            verify(taskRepository, times(1)).save(task);
        }

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "IN_PROGRESS", "COMPLETED"})
        @DisplayName("Should update to all valid status values")
        void shouldUpdateToAllValidStatuses(String statusString) {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setStatus(TaskStatus.valueOf(statusString));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertEquals(TaskStatus.valueOf(statusString), response.getStatus());
        }

        @Test
        @DisplayName("Should handle updating completed task back to pending")
        void shouldHandleUpdatingCompletedTaskBackToPending() {
            // Given
            Task task = createTask("Title", LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            task.setStatus(TaskStatus.COMPLETED);
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setStatus(TaskStatus.PENDING);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertEquals(TaskStatus.PENDING, response.getStatus());
            assertTrue(response.isExpired());  // Debería estar vencida
        }

        @Test
        @DisplayName("Should handle updating both dates simultaneously")
        void shouldHandleUpdatingBothDatesSimultaneously() {
            // Given
            Task task = createTask("Title", LocalDate.now(), LocalDate.now().plusDays(5));
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setStart(LocalDate.now().plusDays(1));
            request.setEnd(LocalDate.now().plusDays(3));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            TaskResponse response = taskService.updateTask(taskId, request, userId);

            // Then
            assertEquals(LocalDate.now().plusDays(1), response.getStart());
            assertEquals(LocalDate.now().plusDays(3), response.getEnd());
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent task")
        void shouldThrowExceptionWhenUpdatingNonExistentTask() {
            // Given
            UUID nonExistentTaskId = UUID.randomUUID();
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("New Title");

            when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.updateTask(nonExistentTaskId, request, userId)
            );
            assertEquals("Task not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteTask - Edge Cases")
    class DeleteTaskEdgeCases {

        @Test
        @DisplayName("Should throw exception when deleting non-existent task")
        void shouldThrowExceptionWhenDeletingNonExistentTask() {
            // Given
            UUID nonExistentTaskId = UUID.randomUUID();
            when(taskRepository.findById(nonExistentTaskId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> taskService.deleteTask(nonExistentTaskId, userId)
            );
            assertEquals("Task not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should allow deleting completed task")
        void shouldAllowDeletingCompletedTask() {
            // Given
            Task task = createTask("Completed Task", LocalDate.now().minusDays(10), LocalDate.now().minusDays(5));
            task.setStatus(TaskStatus.COMPLETED);
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            assertDoesNotThrow(() -> taskService.deleteTask(taskId, userId));
            verify(taskRepository, times(1)).delete(task);
        }

        @Test
        @DisplayName("Should allow deleting expired task")
        void shouldAllowDeletingExpiredTask() {
            // Given
            Task task = createTask("Expired Task", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
            task.setStatus(TaskStatus.PENDING);
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When & Then
            assertDoesNotThrow(() -> taskService.deleteTask(taskId, userId));
            verify(taskRepository, times(1)).delete(task);
        }
    }

    @Nested
    @DisplayName("getTaskById - Edge Cases")
    class GetTaskByIdEdgeCases {

        @Test
        @DisplayName("Should correctly calculate expired for task ending today at midnight")
        void shouldCalculateExpiredForTaskEndingToday() {
            // Given
            Task task = createTask("Task ending today", LocalDate.now().minusDays(5), LocalDate.now());
            task.setStatus(TaskStatus.PENDING);
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            TaskResponse response = taskService.getTaskById(taskId, userId);

            // Then
            // Una tarea que termina "hoy" NO debería estar vencida aún
            assertFalse(response.isExpired());
        }

        @Test
        @DisplayName("Should correctly calculate expired for IN_PROGRESS task")
        void shouldCalculateExpiredForInProgressTask() {
            // Given
            Task task = createTask("In Progress Task", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
            task.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            TaskResponse response = taskService.getTaskById(taskId, userId);

            // Then
            assertTrue(response.isExpired());  // IN_PROGRESS y vencida
        }

        @Test
        @DisplayName("Should handle task with null description")
        void shouldHandleTaskWithNullDescription() {
            // Given
            Task task = new Task(taskId, "Title", null,
                    LocalDate.now(), LocalDate.now().plusDays(5), user);
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            TaskResponse response = taskService.getTaskById(taskId, userId);

            // Then
            assertNotNull(response);
            assertNull(response.getDescription());
        }
    }

    // Helper method
    private Task createTask(String title, LocalDate start, LocalDate end) {
        return new Task(taskId, title, "Description", start, end, user);
    }
}