package com.example.dreamera_master;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.adapter.FragmentAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private CircleImageView profilePhoto;

    private TextView userName;

    private TextView email;

    private PostFragment postFragment;

    private PutFragment putFragment;

    private DeleteFragment deleteFragment;


    private GetFragment getFragment;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private ArrayList<String> tabTitles = new ArrayList<String>();

    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    private TabLayout.Tab postTab;

    private TabLayout.Tab putTab;

    private TabLayout.Tab deleteTab;

    private TabLayout.Tab getTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        /**if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }*/
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Log.d("MainActivity", "actionbar is empty");
        if (actionBar != null) {
            Log.d("MainActivity", "actionBar is not empty");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        //View headerView = mNavigationView.inflateHeaderView(R.layout.nav_header);
        //setNavigationHeaderListener();
        //setNavigationMenuListener();
        //initViewPager();//初始化ViewPager
        applyPermission();
    }

    private void applyPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission
        .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
        .READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "You can use the app by aggreeing all the permissions",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return ;
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "something wrong happen",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void setNavigationHeaderListener(){
        profilePhoto = (CircleImageView) findViewById(R.id.nav_profile_photo);
        userName = (TextView) findViewById(R.id.username);
        email = (TextView) findViewById(R.id.email);
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "set profile photo----uncompelete",
                        Toast.LENGTH_SHORT).show();
            }
        });
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "set userName----uncompelete",
                        Toast.LENGTH_SHORT).show();
            }
        });
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "set email----uncompelete",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setNavigationMenuListener() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_call:
                        Toast.makeText(MainActivity.this, "set Call----uncompelete",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_email:
                        Toast.makeText(MainActivity.this, "set email----uncompelete",
                                Toast.LENGTH_SHORT).show();
                    case R.id.nav_login_out:
                        Toast.makeText(MainActivity.this, "login out----uncompelete",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_exit_application:
                        Toast.makeText(MainActivity.this, "exit application----uncompelete",
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
                return true;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_out:
                Toast.makeText(this, "Login out----uncompelete", Toast
                .LENGTH_SHORT).show();
                break;
            case R.id.exit_application:
                /**Toast.makeText(this, "Exit application----uncompelete", Toast
                .LENGTH_SHORT).show();*/
                finish();
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
     }

     private void initViewPager() {
         //tabLayout = (TabLayout) findViewById(R.id.tab_layout);
         //viewPager = (ViewPager) findViewById(R.id.view_pager);

         postFragment = new PostFragment();
         //putFragment = new PutFragment();
         //deleteFragment = new DeleteFragment();
         //getFragment = new GetFragment();
         fragmentList.add(postFragment);
         //fragmentList.add(putFragment);
         //fragmentList.add(deleteFragment);
         //fragmentList.add(getFragment);

         tabTitles.add(new String("地图"));
         //tabTitles.add(new String("Put"));
         //tabTitles.add(new String("Delete"));
         //tabTitles.add(new String("Get"));
         FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(),
                 fragmentList, tabTitles);
         //viewPager.setOffscreenPageLimit(3);
         viewPager.setAdapter(adapter);
         tabLayout.setupWithViewPager(viewPager);
     }
}
