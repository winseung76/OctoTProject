package com.example.seung.octotproject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{

    Button goToControl,goToVisual,goToBundle,goToSettings;
    TextView member,logout;
    String membername;
    MyApplication octotdata;
    MyService myService;
    boolean isService = false; // 서비스 중인 확인용

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent service_intent = new Intent(this,MyService.class); // 다음넘어갈 컴퍼넌트
        bindService(service_intent,conn, Context.BIND_AUTO_CREATE);

        octotdata=(MyApplication)getApplication();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        goToControl=(Button) findViewById(R.id.goToControl);
        goToVisual=(Button) findViewById(R.id.goToVisual);
        //goToBundle=(Button)findViewById(R.id.goToBundle);
        //goToSettings=(Button)findViewById(R.id.goToSettings);

        Intent intent=getIntent();
        membername=intent.getStringExtra("name");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header_view = navigationView.getHeaderView(0);

        TextView logout = (TextView) nav_header_view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                member = (TextView) findViewById(R.id.member);
                membername = octotdata.getMembername();
                member.setText(Html.fromHtml("<b>" + membername + "</b>" + "  님"));
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        goToVisual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),DataVisualizingActivity.class);
                intent.putExtra("membername",membername);
                startActivity(intent);
            }
        });
        goToControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),ControlActivity.class);
                intent.putExtra("membername",membername);
                startActivity(intent);
            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("로그아웃 하시겠습니까?");
                builder.setCancelable(false);
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("아니요",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 다이얼로그를 취소한다
                                        dialog.cancel();
                                    }
                                });

                // 다이얼로그 생성
                AlertDialog alertDialog = builder.create();

                // 다이얼로그 보여주기
                alertDialog.show();

            }
        });
    }
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MyService.LocalBinder mb = (MyService.LocalBinder) service;
            myService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(), "서비스 연결 해제", Toast.LENGTH_LONG).show();
        }
    };

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if(id==R.id.logout)
            Toast.makeText(getApplicationContext(),"logout 클릭",Toast.LENGTH_LONG).show();

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")

    /*네비게이션 뷰 하단 부분의 아이템 클릭 되었을 시 */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_control){
            Intent intent=new Intent(getApplicationContext(),ControlActivity.class);
            intent.putExtra("name",membername);
            startActivity(intent);
        }
        if(id==R.id.nav_settings){
            Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
            intent.putExtra("name",membername);
            startActivity(intent);
        }
        if(id==R.id.nav_datachart){
            Intent intent=new Intent(getApplicationContext(),DataVisualizingActivity.class);
            intent.putExtra("name",membername);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

}

