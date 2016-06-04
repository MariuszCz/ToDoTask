package com.example.mariusz.todotask.restManagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.mariusz.todotask.Models.Task;
import com.example.mariusz.todotask.R;
import com.example.mariusz.todotask.database.TaskDatabase;
import com.example.mariusz.todotask.exceptions.UnauthorizedException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

;


public class ServerConnector {
    private static final String serverBaseUrl = "http://192.168.1.101:8080/api";
    private Context context;

    public ServerConnector(Context context) {
        this.context = context;
    }


 /*   private HttpURLConnection tryToOpenConnectionAndReturnIt(String serverUrl, String method)throws IOException{
        return tryToOpenConnectionAndReturnIt(serverUrl, method, true, true);
    }*/

    private HttpURLConnection tryToOpenConnectionAndReturnIt(String serverUrl, String method, boolean doInput, boolean doOutput)
            throws IOException {
        URL url = new URL(serverUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod(method);
        conn.setDoInput(doInput);
        conn.setDoOutput(doOutput);
        conn.setRequestProperty("content-type", "application/json");
        return conn;
    }

    private String readStream(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

 /*   private JSONObject parseStringToJsonObject(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject;
    }*/


    public JSONArray downloadAllTasks(String facebookId) throws IOException, JSONException{
        Log.d("fid",facebookId);
        HttpURLConnection urlConnection = tryToOpenConnectionAndReturnIt(serverBaseUrl + "/tasks/" + facebookId, "GET", true, false);
        Log.d("status", Integer.toString(urlConnection.getResponseCode()));
        Log.d("error", urlConnection.getResponseMessage());
        Log.d("url", serverBaseUrl + "/tasks/facebookId");

        String inputString = readStream(urlConnection.getInputStream());
        JSONArray tasks = parseStringToJsonArray(inputString);

        return tasks;
    }


    private JSONArray parseStringToJsonArray(String json) throws JSONException {
        return new JSONArray(json);
    }



    public void deleteTaskFromServer(String serverTaskId) throws IOException{
        HttpURLConnection connection = tryToOpenConnectionAndReturnIt(serverBaseUrl + "/tasks/" + serverTaskId, "DELETE", false, true);
        connection.getResponseCode();
    }


    private Task parseJsonObjectToTask(JSONObject tasks) throws JSONException{
        Task task = new Task();
        task.setTaskId(tasks.getString("taskId"));
        task.setFacebookId(tasks.getString("facebookId"));
        task.setName(tasks.getString("name"));
        task.setDescription(tasks.getString("description"));
        task.setEndDate(parseDate(tasks.getString("endDate")));
        task.setCreatedAt(parseDate(tasks.getString("createdAt")));
        return task;
    }


    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public void updateTaskOnServer(Task task) throws IOException {
        HttpURLConnection connection = tryToOpenConnectionAndReturnIt(serverBaseUrl + "/tasks/"+task.getTaskId(), "PUT", false, true);
        JSONObject jsonObjectForTask = createJsonObjectForTaskUpdate(task);
        sendJsonRequest(jsonObjectForTask, connection);
    }


/*
    private List<Task> getTasksFromJsonArray(JSONArray tasks) {
        List<Task> task = new ArrayList<>();
        for (int i=0;i<tasks.length();i++) {
            try {
                task.add(parseJsonObjectToTask(tasks.getJSONObject(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return task;
    }

*/

    public void addTaskToServer(Task task) throws IOException {
        HttpURLConnection connection = tryToOpenConnectionAndReturnIt(serverBaseUrl + "/tasks", "POST", true, true);
        JSONObject jsonObject = createJsonObjectForTask(task);
        sendJsonRequest(jsonObject, connection);
    }

    public void addTasksFromServer(JSONArray tasks) throws JSONException, IOException {
        TaskDatabase db = new TaskDatabase(context);
        for (int i=0; i<tasks.length();i++) {
            JSONObject task = tasks.getJSONObject(i);
            Task serverTask = parseJsonObjectToTask(task);
                db.addTaskFromServer(serverTask);
        }
    }

    private JSONObject createJsonObjectForTask(Task task) {
        JSONObject credentials = new JSONObject();
        try {
            credentials.put("taskId", task.getId());
            credentials.put("facebookId", task.getFacebookId());
            credentials.put("name", task.getName());
            credentials.put("description", task.getDescription());
            credentials.put("endDate",  new SimpleDateFormat("dd-MM-yyyy").format(task.getEndDate()));
            credentials.put("createdAt", new SimpleDateFormat("dd-MM-yyyy").format(task.getCreatedAt()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return credentials;
    }

    private JSONObject createJsonObjectForTaskUpdate(Task task) {
        JSONObject credentials = new JSONObject();
        try {
            credentials.put("taskId", task.getTaskId());
            credentials.put("facebookId", task.getFacebookId());
            credentials.put("name", task.getName());
            credentials.put("description", task.getDescription());
            credentials.put("endDate",  new SimpleDateFormat("dd-MM-yyyy").format(task.getEndDate()));
            credentials.put("createdAt", new SimpleDateFormat("dd-MM-yyyy").format(task.getCreatedAt()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return credentials;
    }

    private void sendJsonRequest(JSONObject jsonObject, HttpURLConnection connection) throws IOException {
        OutputStream os = connection.getOutputStream();
        String json = jsonObject.toString();
        os.write(json.getBytes("UTF-8"));
        os.close();

        if (connection.getResponseCode() != 201 && connection.getResponseCode() != 200) {
            throw new RuntimeException();
        }
    }

    public boolean isOnline() {
        ConnectivityManager connetion = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connetion.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
