package com.example.seung.octotproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class AlertDialogActivity extends Activity {

    Button yes,no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alert_dialog);

        yes=(Button)findViewById(R.id.yes);
        no=(Button)findViewById(R.id.no);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),StartActivity.class);
                startActivity(intent);
                finish();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("기계 정보가 변경되었습니다.\n변경된 사항을 적용하시겠습니까?\n예 버튼을 누를 시 초기화면으로 돌아갑니다.");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(getApplicationContext(),StartActivity.class);
                startActivity(intent);
                finish();

            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.cancel();
                finish();
                //setStateStop();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
*/
    }
}
