package com.example.browser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View text = findViewById(R.id.tobrowser);
        text.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tobrowser:
                toBrowser();
                break;
            default:
                break;
        }
    }

    protected void toBrowser() {
        Intent intent = new Intent(this, browserActivity.class);
        intent.putExtra("loadUrl", "http://10.0.63.122/test1.php");
//        intent.putExtra("loadUrl", "https://m.startimestv.com/directional_flow_pkg/lists/uganda_pkg_lists.php");
//        intent.putExtra("loadUrl", "http://m.startimestv.com");
        startActivity(intent);
    }
}
