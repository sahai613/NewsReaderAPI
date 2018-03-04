package com.beginner.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String data = "";
    Map<Integer,String>articleurls=new HashMap<Integer, String>();
    Map<Integer,String>articletitles=new HashMap<Integer, String>();
    ArrayList<Integer> articleids=new ArrayList<Integer>();
    SQLiteDatabase articlesDB;
    ArrayList<String> titles= new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    ArrayList<String> urls=new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listview= findViewById(R.id.listview);
       articlesDB =this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);

        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleid INTEGER, url VARCHAR, title VARCHAR, content VARCHAR);" );
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,titles);
        listview.setAdapter(arrayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.i("Article Url",urls.get(position));
                Intent i = new Intent(getApplicationContext(),Articlectivity.class);
                i.putExtra("articleurl",urls.get(position));
                startActivity(i);
            }
        });
        updatelistview();
        try {
            DownloadTask task=new DownloadTask();
           data=task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();

                JSONArray jsonArray =new JSONArray(data);
                articlesDB.execSQL("DELETE FROM articles");
                for (int i = 0; i < 20; i++) {
                    String articleid=  jsonArray.get(i).toString();
//                    Log.i("output",articleid);

                    DownloadTask task1=new DownloadTask();
                  String data1= task1.execute("https://hacker-news.firebaseio.com/v0/item/"+articleid+".json?print=pretty").get();
                    JSONObject obj=new JSONObject(data1);
                            String articleTitle=obj.getString("title");
                            String articleURL=obj.getString("url");
                            articleids.add(Integer.valueOf(articleid));
                            articletitles.put(Integer.valueOf(articleid),articleTitle);
                            articleurls.put(Integer.valueOf(articleid),articleURL);
                           String sql= "INSERT INTO articles(articleid, url, title) VALUES (? , ? , ? )";
                    SQLiteStatement statement=articlesDB.compileStatement(sql);
                    statement.bindString(1,articleid);
                    statement.bindString(2,articleURL);
                    statement.bindString(3,articleTitle);
                    statement.execute();
//                    Log.i("output1",articleTitle);
//                    Log.i("output2",articleURL);


            }



        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public void updatelistview(){try{Cursor c=articlesDB.rawQuery("SELECT * FROM articles",null);
        int articleIDindex=c.getColumnIndex("articleid");
        int articleURLindex=c.getColumnIndex("url");
        int articleTITLEindex=c.getColumnIndex("title");
        c.moveToFirst();
        titles.clear();
        urls.clear();
        while(c!=null){  titles.add(c.getString(articleTITLEindex));
            urls.add(c.getString(articleURLindex));
//                    Log.i("output articleID",Integer.toString(c.getInt(articleIDindex)));
//                    Log.i("output articleURL",c.getString(articleURLindex));
//                    Log.i("output articleTITLE",c.getString(articleTITLEindex));
            c.moveToNext();
        }}catch (Exception e){
        e.printStackTrace();
    }}
    public class DownloadTask extends AsyncTask<String, Void, String> {




        @Override
        protected String doInBackground(String... urls) {
            String data="";

            try {
                URL url = new URL(urls[0]);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                 data = data + line;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;



}

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updatelistview();
        }


        }

    }

