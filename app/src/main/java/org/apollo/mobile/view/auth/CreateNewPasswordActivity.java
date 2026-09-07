package org.apollo.mobile.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apollo.mobile.R;

public final class CreateNewPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);

        findViewById(R.id.btnResetPassword).setOnClickListener(
                view -> resetPassword()
        );
    }

    private void resetPassword() {
        EditText newPasswordInput = findViewById(R.id.etNewPassword);
        EditText confirmPasswordInput = findViewById(R.id.etConfirmPassword);

        String password = newPasswordInput.getText().toString();
        String confirmation = confirmPasswordInput.getText().toString();

        if (password.length() < 8 || !password.equals(confirmation)) {
            Toast.makeText(
                    this,
                    R.string.recovery_password_invalid,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        /*
         * Por enquanto o fluxo é visual e o backend ainda não expõe endpoint de redefinição de senha.
         */
        Toast.makeText(
                this,
                R.string.recovery_password_success,
                Toast.LENGTH_LONG
        ).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }
}