package com.gyobeom29.hipboard.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.gyobeom29.hipboard.NewsArticle;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.adapter.NewsAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class NewsListActivity extends BasicActivity {

    private Handler newsHandler = new Handler();
    private NewsAdapter newsAdapter;
    private RecyclerView newsRecyclerView;
    private ArrayList<NewsArticle> newsItems;
    private final static String NEWS_RSS_URL = "http://hiphople.com/?module=rss&act=rss";
    private final static String TAG = "NewsListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        setActionBarTitle("News List");

        newsRecyclerView = findViewById(R.id.news_list_ac_recycler_view);
        newsRecyclerView.setHasFixedSize(true);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(NewsListActivity.this,RecyclerView.VERTICAL,false));

    }

    @Override
    protected void onResume() {
        super.onResume();
        setNavigation(this.getClass());
        readRss();
    }

    private void readRss(){
        URL url = null;
        try {
            url = new URL(NEWS_RSS_URL);
            RssTask rssTask = new RssTask();
            rssTask.execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


    class RssTask extends AsyncTask<URL,Void,String> {

        @Override
        protected String doInBackground(URL... urls) {

            URL url = urls[0];
            InputStream is = null;
            try {
                XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = xppf.newPullParser();
                is = (InputStream) url.openStream();
                xpp.setInput(is, "UTF-8");

                int eventType = xpp.getEventType();
                NewsArticle item = null;
                newsItems = new ArrayList<>();
                String tagName = null;
                while (eventType != XmlPullParser.END_DOCUMENT){
                    switch (eventType){
                        case XmlPullParser.START_DOCUMENT : break;
                        case XmlPullParser.START_TAG : tagName = xpp.getName(); if(tagName.equals("item")){item = new NewsArticle();}
                        else if(tagName.equals("title")){xpp.next(); if(item != null){item.setTitle(xpp.getText());}}
                        else if(tagName.equals("link")){xpp.next(); if(item != null){item.setLink(xpp.getText());}}
                            break;
                        case XmlPullParser.END_TAG: tagName = xpp.getName();if(tagName.equals("item")){
//                            writeLog("item : " + item.toString());
                            final NewsArticle finalItem = item;
                            newsItems.add(item);
                            item = null;
                            publishProgress();
                        } break;
                    }
                    eventType = xpp.next();
                }

                // news adapter create and setting

                newsHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        newsAdapter = new NewsAdapter(newsItems,getApplicationContext());
                        newsRecyclerView.setAdapter(newsAdapter);
                        newsAdapter.notifyDataSetChanged();
                    }
                });


            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(is!=null) {
                    try {
                        is.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }

            return "End Parsing";
        }
    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }


}