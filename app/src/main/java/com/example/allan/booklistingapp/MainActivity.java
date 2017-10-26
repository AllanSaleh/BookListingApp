package com.example.allan.booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<String>{
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
                    if (getSupportLoaderManager().getLoader(0) == null)
                        getSupportLoaderManager().initLoader(0, null, MainActivity.this).forceLoad();
                    else
                        getSupportLoaderManager().restartLoader(0, null, MainActivity.this).forceLoad();

                }
            }
        });
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

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new BookAsyncTask(this, searchQuery.getText().toString());
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // call the updateUI() method and pass the json string to it (s)
        if (data != null && !data.isEmpty())
            updateUI(data);
        else
            Toast.makeText(MainActivity.this, "No Internet!!!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}