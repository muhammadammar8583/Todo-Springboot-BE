package com.practice.todoApp.service;

import com.practice.todoApp.exception.ResourceNotFoundException;
import com.practice.todoApp.model.TodoModel;
import com.practice.todoApp.model.User;
import com.practice.todoApp.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoModel> findAll(User user) {
        return todoRepository.findByUser(user);
    }

    public Optional<TodoModel> findById(Long id, User user) {
        return todoRepository.findByIdAndUser(id, user);
    }

    public TodoModel save(TodoModel todo, User user) {
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    public void deleteById(Long id, User user) {
        Optional<TodoModel> todoOptional = todoRepository.findByIdAndUser(id, user);
        if (todoOptional.isEmpty()) {
            throw new ResourceNotFoundException("Todo", id);
        }
        todoRepository.deleteById(id);
    }

    public TodoModel toggleComplete(Long id, User user) {
        Optional<TodoModel> todoOptional = todoRepository.findByIdAndUser(id, user);
        if (todoOptional.isEmpty()) {
            throw new ResourceNotFoundException("Todo", id);
        }
        TodoModel todo = todoOptional.get();
        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }

    public List<TodoModel> findByCompleted(boolean completed, User user) {
        return todoRepository.findByUser(user).stream()
                .filter(todo -> todo.isCompleted() == completed)
                .toList();
    }
}
