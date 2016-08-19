package org.kitpies.canvaslayout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.MDBtn).setOnClickListener(this);
        findViewById(R.id.ABtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id  = v.getId();
        Intent intent;
        switch (id){
            case R.id.MDBtn:
                intent = new Intent(this,MixtureDesignActivity.class);
                startActivity(intent);
                break;
            case R.id.ABtn:
                intent = new Intent(this,AdapterActivity.class);
                startActivity(intent);
                break;
        }
    }
}
