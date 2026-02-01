package com.github.cdraz.todoapi.repository

import com.github.cdraz.todoapi.entity.TodoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TodoRepository : JpaRepository<TodoEntity, Long> {
    // underscore to demonstrate relationship traversal; we want todo.user.id not todo.userId
    fun findByIdAndUser_Id(id: Long, userId: Long): TodoEntity?

    fun findAllProjectedByUser_IdOrderByCreatedAtDesc(userId: Long): List<TodoView>

    @Query(
        """
        select 
            t.id as id,
            t.title as title,
            t.completed as completed,
            t.createdAt as createdAt,
            t.user.id as userId
        from TodoEntity t
        where t.id = :id and t.user.id = :userId
        """
    )
    fun findViewByIdAndUserId(
        @Param("id") id: Long,
        @Param("userId") userId: Long
    ): TodoView?
}
