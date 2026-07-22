package com.practice.todoApp.repository;

import com.practice.todoApp.model.TodoModel;
import com.practice.todoApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<TodoModel, Long> {

    List<TodoModel> findByCompleted(boolean completed);

    List<TodoModel> findByUser(User user);

    Optional<TodoModel> findByIdAndUser(Long id, User user);
}
