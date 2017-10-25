package com.example.allan.booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText searchQuery;
    ImageButton searchButton;
    TextView resultsNumber;
    TextView noResultView;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchQuery = (EditText) findViewById(R.id.searchQuery);
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        resultsNumber = (TextView) findViewById(R.id.resultsNumber);
        noResultView = (TextView) findViewById(R.id.noResultView);
        list = (ListView) findViewById(R.id.list);
        noResultView.setVisibility(View.INVISIBLE);
        resultsNumber.setVisibility(View.VISIBLE);
        list.setVisibility(View.VISIBLE);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info == null || !info.isConnected()) {
                    Toast.makeText(MainActivity.this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                } else {
                    BookAsyncTask bookAsyncTask = new BookAsyncTask();
                    bookAsyncTask.execute(searchQuery.getText().toString());
                }
            }
        });
    }

    private class BookAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder JsonData = new StringBuilder();
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            try {
                String query = URLEncoder.encode(strings[0], "UTF-8");
                String urlString = "https://www.googleapis.com/books/v1/volumes?q=" + query;
                Log.v("NETWORK_URL", urlString);
                URL url = new URL(urlString);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(10000);
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.connect();
                inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    JsonData.append(line);
                    line = reader.readLine();
                }
                Log.v("AsyncTask", "Connected" + httpURLConnection.getResponseCode());
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("AsyncTask", e.getMessage());
            } finally {
                if (httpURLConnection != null)
                    httpURLConnection.disconnect();
                if (inputStream != null)
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            return JsonData.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            if (!s.isEmpty())
                updateUI(s);
            else Toast.makeText(MainActivity.this, "Enter a book.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(String s) {
        try {
            JSONObject root = new JSONObject(s);
            int totalItems = root.getInt("totalItems");
            if (totalItems == 0) {
                noResultView.setText(R.string.no_results);
                noResultView.setVisibility(View.VISIBLE);
                list.setVisibility(View.INVISIBLE);
                resultsNumber.setVisibility(View.INVISIBLE);
            } else {
                noResultView.setVisibility(View.INVISIBLE);
                list.setVisibility(View.VISIBLE);
                resultsNumber.setVisibility(View.VISIBLE);
                JSONArray items = root.getJSONArray("items");
                final ArrayList<Books> booksArrayList = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item;
                    if (root.has("items")) {
                        item = items.getJSONObject(i);
                        JSONObject volumeInfo;
                        if (item.has("volumeInfo")) {
                            volumeInfo = item.getJSONObject("volumeInfo");
                            String title;
                            if (volumeInfo.has("title")) {
                                title = volumeInfo.getString("title");
                                JSONArray authors;
                                if (volumeInfo.has("authors")) {
                                    authors = volumeInfo.getJSONArray("authors");
                                    final ArrayList<String> authorsArrayList = new ArrayList<>();
                                    for (int j = 0; j < authors.length(); j++) {
                                        String author = authors.getString(j);
                                        authorsArrayList.add(author);
                                    }
                                    Books book = new Books(title, authorsArrayList);
                                    booksArrayList.add(book);
                                }
                            }
                        }
                    }
                }
                resultsNumber.setText(String.format("%s%s", String.valueOf(totalItems), getString(R.string.results_number)));
                BookAdapter adapter = new BookAdapter(getApplicationContext(), R.layout.items_book_layout, booksArrayList);
                list.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}