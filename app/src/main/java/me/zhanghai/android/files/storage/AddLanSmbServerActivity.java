package me.zhanghai.android.files.storage;

import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.FragmentTransaction;
import me.zhanghai.android.files.app.AppActivity;

public class AddLanSmbServerActivity extends AppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new AddLanSmbServerFragment())
                    .commit();
        }
    }
}

