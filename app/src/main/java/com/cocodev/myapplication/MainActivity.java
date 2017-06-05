package com.cocodev.myapplication;


import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;


import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentManager;
import android.widget.ListView;
import android.widget.Toast;

import com.cocodev.myapplication.data.Contract;
import com.cocodev.myapplication.data.FetchDataTask;
import com.cocodev.myapplication.data.db.DBHelper;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {



    public static final String TAG = "check";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();



        //to set the main activity as home page
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NoticeBoard noticeBoard = new NoticeBoard();
        FragmentManager manager = getSupportFragmentManager();

        Log.i(TAG, "FRAGMENt");
        manager.beginTransaction().replace(R.id.fragment_layout, noticeBoard, null).commit();

    }







    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.i("settings12","settings");
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_refresh){
            FetchDataTask fetchDataTask = new FetchDataTask(this);
            fetchDataTask.execute();
            Toast.makeText(this,"Called FetchDataTask",Toast.LENGTH_SHORT).show();
        }

        return true;

    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            // Handle the camera action
            Home home = new Home();
            FragmentManager manager = getSupportFragmentManager();

            Log.i(TAG,"FRAGMENt");
            manager.beginTransaction().replace(R.id.fragment_layout,home,null).commit();

        } else if (id == R.id.notices) {
            NoticeBoard noticeBoard = new NoticeBoard();
            FragmentManager manager = getSupportFragmentManager();

            Log.i(TAG, "FRAGMENt");
            manager.beginTransaction().replace(R.id.fragment_layout, noticeBoard, null).commit();
        } else if (id == R.id.time_table) {
            TimeTable timeTable = new TimeTable();
            FragmentManager manager = getSupportFragmentManager();

            Log.i(TAG,"timetable");
            manager.beginTransaction().replace(R.id.fragment_layout,timeTable,null).commit();
        } else if (id == R.id.results) {
          //  Intent intent = new Intent(this,Main2Activity.class);
          //  startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
