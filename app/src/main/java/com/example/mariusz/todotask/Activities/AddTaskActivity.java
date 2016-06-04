package com.example.mariusz.todotask.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mariusz.todotask.Models.Task;
import com.example.mariusz.todotask.Models.User;
import com.example.mariusz.todotask.PrefUtils;
import com.example.mariusz.todotask.R;
import com.example.mariusz.todotask.database.TaskDatabase;
import com.example.mariusz.todotask.database.TasksProvider;
import com.example.mariusz.todotask.exceptions.NotEnoughInformationException;
import com.example.mariusz.todotask.exceptions.UnauthorizedException;
import com.example.mariusz.todotask.fragments.DatePickerFragment;
import com.example.mariusz.todotask.restManagement.ServerConnector;
import com.facebook.FacebookSdk;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mariusz on 24/05/16.
 */
public class AddTaskActivity extends AppCompatActivity implements View.OnClickListener, DatePickerFragment.OnDateSelectedListener{
    private EditText taskName;
    private EditText taskDescription;
  //  private TextView taskCreatedAtLabel;
    private TextView taskEndDateLabel;
    private User user;
    private ImageButton dateButton;
    private Button addTaskButton;
    private Date dateSelected = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);
        FacebookSdk.sdkInitialize(getApplicationContext());
        user= PrefUtils.getCurrentUser(AddTaskActivity.this);
        setupViews();
    }


    private void setupViews() {
        taskName = (EditText) findViewById(R.id.taskNameEditText);
        taskDescription = (EditText) findViewById(R.id.taskDescriptionEditText);
        taskEndDateLabel = (TextView) findViewById(R.id.taskEndDateLabel);
        dateButton = (ImageButton) findViewById(R.id.calendarDateButton);
        dateButton.setOnClickListener(this);
        addTaskButton = (Button) findViewById(R.id.addButton);
        addTaskButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addButton:
                tryToAddTask();
                break;
            case R.id.calendarDateButton:
                showDateFragment();
                break;
        }
    }

    private void tryToAddTask() {
        try {
            addTask();
        } catch (NotEnoughInformationException e) {
            Toast.makeText(this, R.string.error_not_enough_information, Toast.LENGTH_SHORT).show();
        }
    }

    private void addTask() throws NotEnoughInformationException {
        Task task = createAndReturnTask();
        TasksProvider db = new TaskDatabase(this, user.getFacebookID());
        db.addTaskAndReturnItsId(task);
        addTaskToServer(task);
        Toast.makeText(this, getString(R.string.task_added_success), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, TasksListActivity.class);
        startActivity(i);
    }

    private void addTaskToServer(Task task) {
        Connection connection = new Connection(this,task);
        connection.execute();
    }
        private class Connection extends AsyncTask {
            Context context;
            Handler handler;
            Task task;

            public Connection(Context context, Task task) {
                this.context = context;
                handler =  new Handler(context.getMainLooper());
                this.task = task;
            }

            @Override
            protected Object doInBackground(Object[] params) {
                ServerConnector connector = new ServerConnector(context);
                try {
                    connector.addTaskToServer(task);
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, R.string.task_added_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, R.string.server_exception_text, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                return null;
            }
    }

    private Task createAndReturnTask() throws NotEnoughInformationException{
        Task task = new Task();

        String name = taskName.getText().toString();
        String description = taskDescription.getText().toString();
        String endDate = taskEndDateLabel.getText().toString();

        if (name.isEmpty() || endDate.isEmpty()) {
            throw new NotEnoughInformationException();
        }

        task.setName(name);
        task.setDescription(description);
        task.setEndDate(dateSelected);
        task.setCreatedAt(new Date());
        task.setFacebookId(user.getFacebookID());
        return task;
    }

    private void showDateFragment() {
        DatePickerFragment picker = new DatePickerFragment();
        picker.setDate(dateSelected);
        picker.show(getFragmentManager(), getString(R.string.date_fragment_label));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit, menu);

        setupActionBar();

        return super.onCreateOptionsMenu(menu);
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBackToMainIntent();
                return true;
            default:
                return false;
        }
    }

    private void goBackToMainIntent() {
        Intent intent = new Intent(this, TasksListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDateSelected(Date date) {
        dateSelected = date;
        String dateText = getString(R.string.task_date_label) + " " + formatDate(date, "dd-MM-yyyy");
        taskEndDateLabel.setText(dateText);
    }

    private String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

}

