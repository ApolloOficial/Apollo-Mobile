package org.apollo.mobile.view.access;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;
import org.apollo.mobile.session.SessionManagerFactory;
import org.apollo.mobile.auth.navigation.SessionNavigator;

public final class AccessDeniedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout); --> view de acesso negado
        findViewById(R.id.btnBack).setOnClickListener(view -> {
            SessionManagerFactory.create(getApplicationContext()).logout();
            SessionNavigator.openLogin(this);
        });
    }
}
