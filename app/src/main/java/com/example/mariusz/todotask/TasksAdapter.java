package com.example.mariusz.todotask;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mariusz.todotask.Activities.EditTaskActivity;
import com.example.mariusz.todotask.Activities.TasksListActivity;
import com.example.mariusz.todotask.Models.Task;
import com.example.mariusz.todotask.database.TaskDatabase;
import com.example.mariusz.todotask.database.TasksProvider;

import java.text.SimpleDateFormat;

/**
 * Created by mariusz on 24/05/16.
 */
public class TasksAdapter extends BaseAdapter {
    private TasksProvider taskDatabase;
    private Context context;
    private String facebookId;

    public TasksAdapter(Context context, String facebookId) {
        this.context = context;
        taskDatabase = new TaskDatabase(context,facebookId);
        this.facebookId = facebookId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View taskView = getTaskView(convertView, parent);
        Task task = getItem(position);
        bindTaskToView(task, taskView, position);
        return taskView;
    }

    private View getTaskView(View convertView, ViewGroup parent) {
        View taskView;
        if (convertView == null) {
            taskView = LayoutInflater.from(context).inflate(R.layout.task_row, parent, false);
        }
        else {
            taskView = convertView;
        }
        return taskView;
    }

    @Override
    public Task getItem(int position) {
        Task task = taskDatabase.getTaskByPosition(position);
        return task;
    }

    private void bindTaskToView(final Task task, View taskView, final int position) {
        TextView taskName = (TextView) taskView.findViewById(R.id.nameTextView);
        TextView taskDate = (TextView) taskView.findViewById(R.id.dateTextView);
        TextView taskDescription = (TextView) taskView.findViewById(R.id.descriptionTextView);
        TextView taskCreatedAtLabel = (TextView) taskView.findViewById(R.id.taskCreatedAtLabel);
        ImageButton deleteTaskButton = (ImageButton) taskView.findViewById(R.id.deleteTaskButton);
        final ImageButton editTaskButton = (ImageButton) taskView.findViewById(R.id.editTaskButton);

        taskName.setText(task.getName());
        String endDate = new SimpleDateFormat("yyyy-MM-dd").format(task.getEndDate());
        String createdDate = new SimpleDateFormat("yyyy-MM-dd").format(task.getCreatedAt());
        taskDate.setText(endDate);
        taskDescription.setText(task.getDescription());
        taskCreatedAtLabel.setText(createdDate);
        deleteTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask(position);
            }
        });
        editTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTask(position);
            }
        });

        setCorrectExtendedInfoVisibilityForTask(taskView, task);
    }

    private void deleteTask(int position) {
        ((TasksListActivity) context).deleteTask(position);
    }

    public interface OnDeleteTaskClicked {
        void deleteTask(int position);
    }

    public void deleteAllTasks() {
        taskDatabase.deleteAllTasks();
        notifyDataSetChanged();
    }

    private void editTask(int position) {
        Intent i = new Intent(context, EditTaskActivity.class);
        i.putExtra("position", position);
        context.startActivity(i);
    }

    private void setCorrectExtendedInfoVisibilityForTask(View taskView, Task task) {
        if (task.isOpen()) {
            setVisibilityVisibleForTask(taskView);
        } else {
            setVisibilityGoneForTask(taskView);
        }
    }

    private void setVisibilityGoneForTask(View taskView) {
        View extendedInfoView = taskView.findViewById(R.id.taskRowExtendedInfo);
        extendedInfoView.setVisibility(View.GONE);
    }

    private void setVisibilityVisibleForTask(View taskView) {
        View extendedInfoView = taskView.findViewById(R.id.taskRowExtendedInfo);
        extendedInfoView.setVisibility(View.VISIBLE);
    }

    public void updateTaskInDatabase(Task task, int position) {
        taskDatabase.updateTask(task, getItemId(position));
    }

    @Override
    public int getCount() {
        return taskDatabase.getTasksNumber();
    }

    @Override
    public long getItemId(int position) {
        return taskDatabase.getTaskIdByPosition(position);
    }

    public void deleteTaskByPosition(int position) {
        taskDatabase.deleteTaskByItsId(getItemId(position));
    }

}
