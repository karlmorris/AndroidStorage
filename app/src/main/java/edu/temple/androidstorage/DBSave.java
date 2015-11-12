package edu.temple.androidstorage;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import edu.temple.androidstorage.dbstuff.StockDBContract;
import edu.temple.androidstorage.dbstuff.StockDBHelper;

public class DBSave extends Activity {

    SQLiteDatabase db;
    StockDBHelper mDbHelper;

    EditText symbolEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbsave);

        symbolEditText = (EditText) findViewById(R.id.symbolEditText);

        mDbHelper = new StockDBHelper(this);

        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String stockSymbol = symbolEditText.getText().toString();
                Thread t = new Thread() {
                    @Override
                    public void run() {

                        URL stockQuoteUrl;

                        try {

                            stockQuoteUrl = new URL("http://finance.yahoo.com/webservice/v1/symbols/" + stockSymbol + "/quote?format=json");

                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(
                                            stockQuoteUrl.openStream()));

                            String response = "", tmpResponse;

                            tmpResponse = reader.readLine();
                            while (tmpResponse != null) {
                                response = response + tmpResponse;
                                tmpResponse = reader.readLine();
                            }

                            JSONObject stockObject = new JSONObject(response);
                            Message msg = Message.obtain();
                            msg.obj = stockObject;
                            stockResponseHandler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        });
        populateListView();

    }

    Handler stockResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            JSONObject responseObject = (JSONObject) msg.obj;

            try {
                Stock stock = new Stock(responseObject.getJSONObject("list")
                        .getJSONArray("resources")
                        .getJSONObject(0)
                        .getJSONObject("resource")
                        .getJSONObject("fields"));

                saveData(stock.getSymbol(), stock.getName(), stock.getPrice());
            } catch (Exception e) {
                e.printStackTrace();
            }


            return false;
        }
    });


    private void saveData(String symbol, String company, double price){

        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StockDBContract.StockEntry.COLUMN_NAME_SYMBOL, symbol);
        values.put(StockDBContract.StockEntry.COLUMN_NAME_COMPANY, company);
        values.put(StockDBContract.StockEntry.COLUMN_NAME_PRICE, price);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                StockDBContract.StockEntry.TABLE_NAME,
                null,
                values);

        if (newRowId > 0) {
            Log.d("Stock data saved ", newRowId + " - " + company);
            populateListView();
        }

    }


    private void populateListView() {
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(StockDBContract.StockEntry.TABLE_NAME, new String[]{"_id", StockDBContract.StockEntry.COLUMN_NAME_COMPANY}, null, null, null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.db_layout, cursor, new String[]{StockDBContract.StockEntry.COLUMN_NAME_COMPANY}, new int[]{R.id.companyName}, 0);
        ((ListView) findViewById(R.id.companyListView)).setAdapter(adapter);
    }
}
