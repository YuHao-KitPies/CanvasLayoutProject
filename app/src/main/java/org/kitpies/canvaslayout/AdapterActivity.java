package org.kitpies.canvaslayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.kitpies.canvaslayout.adapter.TestAdapter;

public class AdapterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter);

        ListView listView = (ListView) findViewById(R.id.list);
        TestAdapter testAdapter = new TestAdapter(this);
        listView.setAdapter(testAdapter);
    }
}
