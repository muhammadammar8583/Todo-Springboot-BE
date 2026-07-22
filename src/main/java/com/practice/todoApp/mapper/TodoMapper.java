package com.practice.todoApp.mapper;

import com.practice.todoApp.dto.TodoDto;
import com.practice.todoApp.model.TodoModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    TodoDto toDto(TodoModel entity);

    @Mapping(target = "user", ignore = true)
    TodoModel toEntity(TodoDto dto);

    List<TodoDto> toDtoList(List<TodoModel> entities);

    List<TodoModel> toEntityList(List<TodoDto> dtos);
}
