package com.gyobeom29.hipboard.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.gyobeom29.hipboard.NewsArticle;
import com.gyobeom29.hipboard.PostInfo;
import com.gyobeom29.hipboard.R;
import com.gyobeom29.hipboard.SettingService;
import com.gyobeom29.hipboard.adapter.MainPostAdapter;
import com.gyobeom29.hipboard.adapter.NewsAdapter;
import com.gyobeom29.hipboard.adapter.YoutubeViewPageAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.CipherSpi;

public class MainActivity extends BasicActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView, newsRecyclerView;
    private MainPostAdapter mainPostAdapter;
    FirebaseUser user;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    ViewPager youtubeViewPager;
    YoutubeViewPageAdapter youtubeAdapter;
    Handler newsHandler = new Handler();
    NewsAdapter newsAdapter;


    private ArrayList<NewsArticle> newsArticles;

    private TabLayout youtubeTabLayout;
    TextView listMoreTextView;
    TextView newsListMoreTextView;

    private static final String YOUTUBE_THUMBNAIL_COLLECTION = "youtubeThumbnails";
    private static final String YOUTUBE_THUMBNAIL_DOCUMENT = "thumbnails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBarTitle(R.string.app_name);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.mainRecyclerView);
        newsRecyclerView = findViewById(R.id.news_recycler_view);


        recyclerView.setHasFixedSize(true);
        newsRecyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,RecyclerView.VERTICAL,false));
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,RecyclerView.VERTICAL,false));

        youtubeViewPager = findViewById(R.id.youtubeImageViewPager);
        youtubeTabLayout= findViewById(R.id.youtube_tab_layout);
        youtubeTabLayout.setupWithViewPager(youtubeViewPager, true);

        writeLog("auth getUid : " + FirebaseAuth.getInstance().getUid());
        listMoreTextView = findViewById(R.id.list_more_textView);
        newsListMoreTextView = findViewById(R.id.news_list_more_text_view);
        listMoreTextView.setOnClickListener(moreTextViewOnClickListener);
        newsListMoreTextView.setOnClickListener(moreTextViewOnClickListener);

    }

    View.OnClickListener moreTextViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.list_more_textView : startActiNoFinish(PostListActivity.class); break;
                case R.id.news_list_more_text_view : startActiNoFinish(NewsListActivity.class); break;
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setNavigation(this.getClass());
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            startActiNoFinish(SignUpActivity.class);
        }else{
            Log.e("UId" , user.getUid());
            firestore = FirebaseFirestore.getInstance();
            DocumentReference docRef = firestore.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document != null) {
                            Log.d("document" , ""+document);
                            Log.e("Boolean",""+document.exists());
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                if(document.getData().get("name") == null) {
                                    startActi(MemberInitActivity.class);
                                }else{
                                    if(SettingService.setting==null){
                                        SettingService.getSetting(user,getApplicationContext());
                                    }
                                    Log.e("documentData", "data" + document.getData());
                                    setYoutubeThumNail();
                                    readRss();
                                }
                            } else {
                                startActi(MemberInitActivity.class);
                                Log.d(TAG, "No such document");
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
            final ArrayList<PostInfo> postList = new ArrayList<>();
            recyclerView.removeAllViewsInLayout();
            firestore.collection("posts").orderBy("createAt", Query.Direction.DESCENDING).limit(10).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if(task.getResult().size()>0){
                                    listMoreTextView.setVisibility(View.VISIBLE);
                                }
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    Log.i("documentId", document.getId());
                                    String documentId = document.getId();
                                    String title = document.getData().get("title").toString();
                                    ArrayList<String> contents = (ArrayList<String>) document.getData().get("contents");
                                    String publisher = document.getData().get("publisher").toString();
                                    Date createAt = new Date(document.getDate("createAt").getTime());
                                    long views = (Long) document.getData().get("views");
                                    long likeCnt = (long) document.getData().get("likeCount");
                                    String publisherName = document.getData().get("publisherName").toString();
                                    PostInfo info = new PostInfo(title, contents, publisher, views, likeCnt, createAt,publisherName);
                                    info.setDocumentId(documentId);
                                    postList.add(info);
                                    writeLog(info.toString());

                                }
                                mainPostAdapter = new MainPostAdapter(postList,MainActivity.this);
                                recyclerView.setAdapter(mainPostAdapter);
                                mainPostAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                                listMoreTextView.setVisibility(View.GONE);
                            }
                        }
                    });

        }

    }

    private void startActi(Class c){
        Intent intent = new Intent(MainActivity.this,c);
        startActivity(intent);
        finish();
    }

    private void startActiNoFinish(Class c){
        Intent intent = new Intent(MainActivity.this,c);
        startActivity(intent);
    }


    private void startingToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    private void writeLog(String msg){
        Log.i(TAG,msg);
    }

    private Date getPreviousMonth(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH,-1);
        Date date = c.getTime();
        return date;
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alBuilder = new AlertDialog.Builder(MainActivity.this);
        alBuilder.setMessage("정말 종료 하시겠습니까?");
        alBuilder.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.super.onBackPressed();
            }
        });
        alBuilder.create().show();
    }

    private void setYoutubeThumNail(){
        FirebaseFirestore.getInstance().collection(YOUTUBE_THUMBNAIL_COLLECTION).document(YOUTUBE_THUMBNAIL_DOCUMENT).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> thumbNailPathList = null;
                if(documentSnapshot.exists()){
                    if(documentSnapshot.getData()!=null){
                        if(documentSnapshot.getData().get("thumbnailList")!=null){
                            thumbNailPathList =  (ArrayList)documentSnapshot.getData().get("thumbnailList");
                            for (String t:thumbNailPathList) {
                                writeLog("thubnail : " + t);
                            }
                        }
                    }
                }
                if(thumbNailPathList!=null){
                    youtubeAdapter = new YoutubeViewPageAdapter(getApplicationContext(),thumbNailPathList);
                    youtubeViewPager.setAdapter(youtubeAdapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void readRss(){
        URL url = null;
        try {
            url = new URL("http://hiphople.com/?module=rss&act=rss");
            RssTask rssTask = new RssTask();
            rssTask.execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    class RssTask extends AsyncTask<URL,Void,String>{

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
                newsArticles = new ArrayList<>();
                String tagName = null;
                int itemCount = 0;
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
                            final int finalItemCount = itemCount;
                            newsArticles.add(item);
                            item = null;
                            publishProgress();
                            writeLog("itemCount : " + itemCount);
                            itemCount++;
                        } break;
                    }
                    eventType = xpp.next();
                    if(itemCount==10) break;
                }
                // news adapter create and setting

                final int finalItemCount1 = itemCount;
                newsHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        newsAdapter = new NewsAdapter(newsArticles,getApplicationContext());
                        newsRecyclerView.setAdapter(newsAdapter);
                        newsAdapter.notifyDataSetChanged();
                        if(finalItemCount1 >0){
                            newsListMoreTextView.setVisibility(View.VISIBLE);
                        }
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


}