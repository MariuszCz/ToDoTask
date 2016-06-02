package com.example.mariusz.todotask.database;

import com.example.mariusz.todotask.Models.Task;

/**
 * Created by mariusz on 24/05/16.
 */
public interface TasksProvider {
    int getTasksNumber();
    long addTaskAndReturnItsId(Task task);
 //   Task getTaskById(String id);
    Task getTaskByPosition(int position);
    void deleteTaskByItsId(long id);
    void deleteAllTasks();
    long getTaskIdByPosition(int position);
    void updateTask(Task task, long id);
    }
