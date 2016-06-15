package com.holenstudio.awesomeview;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.holenstudio.awesomeview.ui.FanlikeFragment;
import com.holenstudio.awesomeview.ui.TurnableFragment;
import com.holenstudio.awesomeview.ui.TurnableLayoutFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TurnableFragment mTurnableFragment;
    private TurnableLayoutFragment mTurnableLayoutFragment;
    private FanlikeFragment mFanlikeFragment;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mTurnableFragment == null) {
            mTurnableFragment = TurnableFragment.getInstance(null);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, mTurnableFragment).commit();
        toolbar.setTitle(R.string.turnable_fragment_title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
//        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (id == R.id.turnable_fragment) {
            if (mTurnableFragment == null) {
                mTurnableFragment = TurnableFragment.getInstance(null);
            }
            ft.replace(R.id.content_layout, mTurnableFragment).commit();
            toolbar.setTitle(R.string.turnable_fragment_title);
        } else if (id == R.id.fanlike_fragment) {
            if (mFanlikeFragment == null) {
                mFanlikeFragment = mFanlikeFragment.getInstance(null);
            }
            ft.replace(R.id.content_layout, mFanlikeFragment).commit();
            toolbar.setTitle(R.string.fanlike_fragment_title);
        } else if (id == R.id.turnable_layout_fragment) {
            if (mTurnableLayoutFragment == null) {
                mTurnableLayoutFragment = TurnableLayoutFragment.getInstance(null);
            }
            ft.replace(R.id.content_layout, mTurnableLayoutFragment).commit();
            toolbar.setTitle(R.string.turnable_layout_fragment_title);

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
