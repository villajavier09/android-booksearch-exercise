package com.codepath.android.booksearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.android.booksearch.R;
import com.codepath.android.booksearch.adapters.BookAdapter;
import com.codepath.android.booksearch.models.Book;
import com.codepath.android.booksearch.net.BookClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import androidx.appcompat.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;

import okhttp3.Headers;


public class BookListActivity extends AppCompatActivity {
    private RecyclerView rvBooks;
    private BookAdapter bookAdapter;
    private BookClient client;
    private ArrayList<Book> abooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvBooks = findViewById(R.id.rvBooks);
        abooks = new ArrayList<>();

        // Initialize the adapter
        bookAdapter = new BookAdapter(this, abooks);
        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Toast.makeText(
                        BookListActivity.this,
                        "An item at position " + position + " clicked!",
                        Toast.LENGTH_SHORT).show();

                // Handle item click here:
                // Create Intent to start BookDetailActivity
                Intent intent =  new Intent(getBaseContext(),BookDetailActivity.class);

                // Get Book at the given position
                Book book = abooks.get(position);
                // Pass the book into details activity using extras
                intent.putExtra("Book", Parcels.wrap(book));
                startActivity(intent);
            }
        });

        // Attach the adapter to the RecyclerView
        rvBooks.setAdapter(bookAdapter);

        // Set layout manager to position the items
        rvBooks.setLayoutManager(new LinearLayoutManager(this));

        // Fetch the data remotely

    }

    // Executes an API call to the OpenLibrary search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchBooks(String query) {
        client = new BookClient();
        client.getBooks(query, new JsonHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Headers headers, JSON response) {
                try {
                    JSONArray docs;
                    if (response != null) {
                        // Get the docs json array
                        docs = response.jsonObject.getJSONArray("docs");
                        // Parse json array into array of model objects
                        final ArrayList<Book> books = Book.fromJson(docs);
                        // Remove all books from the adapter
                        abooks.clear();
                        // Load model objects into the adapter
                        for (Book book : books) {
                            abooks.add(book); // add book through the adapter
                        }
                        bookAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    // Invalid JSON format, show appropriate error.
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String responseString, Throwable throwable) {
                // Handle failed request here
                Log.e(BookListActivity.class.getSimpleName(),
                        "Request failed with code " + statusCode + ". Response message: " + responseString);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_list, menu);

        MenuItem bookSearch = menu.findItem(R.id.book_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(bookSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchBooks(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
