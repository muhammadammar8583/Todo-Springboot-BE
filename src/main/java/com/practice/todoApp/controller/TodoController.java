package com.practice.todoApp.controller;

import com.practice.todoApp.dto.ApiResponse;
import com.practice.todoApp.dto.TodoDto;
import com.practice.todoApp.mapper.TodoMapper;
import com.practice.todoApp.model.TodoModel;
import com.practice.todoApp.model.User;
import com.practice.todoApp.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todo Management", description = "APIs for managing todo items")
@SecurityRequirement(name = "bearerAuth")
public class TodoController {

    private final TodoService todoService;
    private final TodoMapper todoMapper;

    public TodoController(TodoService todoService, TodoMapper todoMapper) {
        this.todoService = todoService;
        this.todoMapper = todoMapper;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalStateException("Invalid principal type");
        }
        return (User) principal;
    }

    // GET /api/todos - Retrieves all todos
    @GetMapping
    @Operation(summary = "Get all todos", description = "Retrieves all todos for the authenticated user")
    public ResponseEntity<ApiResponse<List<TodoDto>>> getAllTodos() {
        User user = getAuthenticatedUser();
        List<TodoModel> todos = todoService.findAll(user);
        List<TodoDto> todoDtos = todoMapper.toDtoList(todos);
        ApiResponse<List<TodoDto>> response = ApiResponse.success("Todos fetched successfully.", todoDtos);
        return ResponseEntity.ok(response);
    }

    // GET /api/todos/{id} - Retrieves a specific todo by its ID
    @GetMapping("/{id}")
    @Operation(summary = "Get todo by ID", description = "Retrieves a specific todo by its ID for the authenticated user")
    public ResponseEntity<ApiResponse<TodoDto>> getTodoById(
            @Parameter(description = "Todo ID", required = true)
            @PathVariable Long id) {
        User user = getAuthenticatedUser();
        return todoService.findById(id, user)
                .map(todo -> {
                    TodoDto todoDto = todoMapper.toDto(todo);
                    return ResponseEntity.ok(ApiResponse.success("Todo fetched successfully.", todoDto));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Todo not found with id: " + id)));
    }

    // POST /api/todos - Creates a new todo
    @PostMapping
    @Operation(summary = "Create a new todo", description = "Creates a new todo for the authenticated user")
    public ResponseEntity<ApiResponse<TodoDto>> createTodo(@Valid @RequestBody TodoDto todoDto) {
        User user = getAuthenticatedUser();
        TodoModel todoEntity = todoMapper.toEntity(todoDto);
        TodoModel createdTodo = todoService.save(todoEntity, user);
        TodoDto createdTodoDto = todoMapper.toDto(createdTodo);
        ApiResponse<TodoDto> response = ApiResponse.created("Todo created successfully.", createdTodoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/todos/{id} - Updates an existing todo by its ID
    @PutMapping("/{id}")
    @Operation(summary = "Update a todo", description = "Updates an existing todo by its ID for the authenticated user")
    public ResponseEntity<ApiResponse<TodoDto>> updateTodo(
            @Parameter(description = "Todo ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TodoDto todoDto) {
        User user = getAuthenticatedUser();
        if (!todoService.findById(id, user).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Todo not found with id: " + id));
        }
        todoDto.setId(id);
        TodoModel todoEntity = todoMapper.toEntity(todoDto);
        TodoModel updatedTodo = todoService.save(todoEntity, user);
        TodoDto updatedTodoDto = todoMapper.toDto(updatedTodo);
        ApiResponse<TodoDto> response = ApiResponse.success("Todo updated successfully.", updatedTodoDto);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/todos/{id} - Deletes a todo by its ID
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a todo", description = "Deletes a todo by its ID for the authenticated user")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(
            @Parameter(description = "Todo ID", required = true)
            @PathVariable Long id) {
        User user = getAuthenticatedUser();
        if (!todoService.findById(id, user).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Todo not found with id: " + id));
        }
        todoService.deleteById(id, user);
        ApiResponse<Void> response = ApiResponse.success("Todo deleted successfully.", null);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/todos/{id}/toggle - Toggles the completed status of a todo
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle todo completion status", description = "Toggles the completed status of a todo for the authenticated user")
    public ResponseEntity<ApiResponse<TodoDto>> toggleComplete(
            @Parameter(description = "Todo ID", required = true)
            @PathVariable Long id) {
        User user = getAuthenticatedUser();
        if (!todoService.findById(id, user).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Todo not found with id: " + id));
        }
        TodoModel updatedTodo = todoService.toggleComplete(id, user);
        TodoDto updatedTodoDto = todoMapper.toDto(updatedTodo);
        ApiResponse<TodoDto> response = ApiResponse.success("Todo status updated successfully.", updatedTodoDto);
        return ResponseEntity.ok(response);
    }

    // GET /api/todos/completed/{completed} - Retrieves todos filtered by completion status
    @GetMapping("/completed/{completed}")
    @Operation(summary = "Get todos by completion status", description = "Retrieves todos filtered by completion status for the authenticated user")
    public ResponseEntity<ApiResponse<List<TodoDto>>> getTodosByCompleted(
            @Parameter(description = "Completion status (true for completed, false for pending)", required = true)
            @PathVariable boolean completed) {
        User user = getAuthenticatedUser();
        List<TodoModel> todos = todoService.findByCompleted(completed, user);
        List<TodoDto> todoDtos = todoMapper.toDtoList(todos);
        String message = completed ? "Completed todos fetched successfully." : "Pending todos fetched successfully.";
        ApiResponse<List<TodoDto>> response = ApiResponse.success(message, todoDtos);
        return ResponseEntity.ok(response);
    }
}
