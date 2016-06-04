package com.example.mariusz.todotask.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mariusz.todotask.restManagement.ServerConnector;
import com.example.mariusz.todotask.Models.Task;
import com.example.mariusz.todotask.Models.User;
import com.example.mariusz.todotask.PrefUtils;
import com.example.mariusz.todotask.R;
import com.example.mariusz.todotask.TasksAdapter;
import com.example.mariusz.todotask.fragments.DeleteConfirmDialogFragment;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class TasksListActivity extends AppCompatActivity implements TasksAdapter.OnDeleteTaskClicked, DeleteConfirmDialogFragment.OnPositiveDelete  {
    private User user;
    private ListView listView;
    private TasksAdapter adapter;
    private ImageView profileImage;
    private TextView username;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.tasks_list);
        user= PrefUtils.getCurrentUser(TasksListActivity.this);
        profileImage= (ImageView) findViewById(R.id.profileImage);
        username=(TextView) findViewById(R.id.username);
        getFacebookUserData();
        initializeList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tasks_list, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_task: {
                startAddTaskActivity();
                return true;
            }
            case R.id.logout: {
                logout();
                return true;
            }
            case R.id.getTasksFromServer: {
                    getTasksFromServer();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void logout() {
        PrefUtils.clearCurrentUser(this);
        LoginManager.getInstance().logOut();

        Intent i= new Intent(this,LoginActivity.class);
        startActivity(i);
    }

    private void startAddTaskActivity() {
        Intent i = new Intent(this, AddTaskActivity.class);
        startActivity(i);
    }

    private void getTasksFromServer() {
        adapter.deleteAllTasks();
        ConnectionAdd connection = new ConnectionAdd(this);
        connection.execute();
    }

    private void initializeList() {
        listView = (ListView) findViewById(R.id.tasksList);
        adapter = new TasksAdapter(this, user.getFacebookID());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(createAndReturnClickListener());

    }

    private AdapterView.OnItemClickListener createAndReturnClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toggleTaskItem(position);
            }
        };
    }

    private void toggleTaskItem(final int position) {
        Task task = adapter.getItem(position);
        task.setOpen(!task.isOpen());
        adapter.updateTaskInDatabase(task, position);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void deleteTask(int position) {
        showConfirmDialog(position);
    }

    private void showConfirmDialog(int position) {
        DeleteConfirmDialogFragment confirmDialogFragment = new DeleteConfirmDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(getString(R.string.positionExtra), position);
        confirmDialogFragment.setArguments(bundle);
        confirmDialogFragment.show(getFragmentManager(), getString(R.string.delete_task_tag));
    }

    @Override
    public void positiveDelete(int position) {
        Task task = adapter.getItem(position);
        adapter.deleteTaskByPosition(position);
        tryDeleteTaskFromServer(task.getTaskId());
        Toast.makeText(this, getString(R.string.task_deleted_success), Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    private void tryDeleteTaskFromServer(String position) {
        ConnectionDelete connection = new ConnectionDelete(this,position);
        connection.execute();
    }
    private class ConnectionDelete extends AsyncTask {
        Context context;
        Handler handler;
        String position;

        public ConnectionDelete(Context context, String position) {
            this.context = context;
            handler =  new Handler(context.getMainLooper());
            this.position = position;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            ServerConnector connector = new ServerConnector(context);
            try {
                connector.deleteTaskFromServer(position);
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, R.string.task_deleted_success, Toast.LENGTH_SHORT).show();
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
    private void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    private void getFacebookUserData() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                URL imageURL = null;
                try {
                    imageURL = new URL("https://graph.facebook.com/" + user.getFacebookID() + "/picture?type=small");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    bitmap  = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                profileImage.setImageBitmap(bitmap);
            }
        }.execute();
        username.setText(user.getName());
    }

    private class ConnectionAdd extends AsyncTask {
        Context context;
        Handler handler;

        public ConnectionAdd(Context context) {
            this.context = context;
            handler =  new Handler(context.getMainLooper());
        }

        @Override
        protected Object doInBackground(Object[] params) {
            ServerConnector connector = new ServerConnector(context);
            try {
                connector.addTasksFromServer(connector.downloadAllTasks(user.getFacebookID()));
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, R.string.task_fetched_success, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException | JSONException e) {
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, R.string.server_exception_text, Toast.LENGTH_SHORT).show();
                    }
                });


            }


            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ((TasksListActivity) context).notifyDataSetChanged();
        }
}

}
