package com.cuonghx.teacher.teachercheckin.screens.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;
import com.cuonghx.teacher.teachercheckin.screens.home.HomeActivity;
import com.cuonghx.teacher.teachercheckin.util.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


public class RegisterActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    private TextInputLayout mInputLayoutPassword;
    private TextInputLayout mInputLayoutConfirmPassword;
    private TextInputLayout mInputLayoutName;
    private TextInputLayout mInputLayoutEmail;

    private TextInputEditText mInputEditTextEmail;
    private TextInputEditText mInputEditTextPassword;
    private TextInputEditText mInputEditTextName;
    private TextInputEditText mInputEditTextConfirmPassword;

    private ProgressBar mProgressBarLoading;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_register;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
    }
    private void setupView(){
        mInputEditTextEmail = findViewById(R.id.tiet_mail);
        mInputEditTextConfirmPassword = findViewById(R.id.tiet_confirm_password);
        mInputEditTextPassword = findViewById(R.id.tiet_password);
        mInputEditTextName = findViewById(R.id.tiet_name);

        mInputLayoutName = findViewById(R.id.til_name);
        mInputLayoutEmail = findViewById(R.id.til_mail);
        mInputLayoutPassword = findViewById(R.id.til_password);
        mInputLayoutConfirmPassword = findViewById(R.id.til_confirm_password);

        mInputEditTextName.addTextChangedListener(this);
        mInputEditTextPassword.addTextChangedListener(this);
        mInputEditTextConfirmPassword.addTextChangedListener(this);
        mInputEditTextEmail.addTextChangedListener(this);

        findViewById(R.id.bt_register).setOnClickListener(this);

        mProgressBarLoading = findViewById(R.id.progress_circular_loading);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_register:
                register();
                break;
        }
    }
    private void register(){
        final String name = String.valueOf(mInputEditTextName.getText());
        final String email = String.valueOf(mInputEditTextEmail.getText());
        String password = String.valueOf(mInputEditTextPassword.getText());
        String confirmPassword = String.valueOf(mInputEditTextConfirmPassword.getText());
        if (!isValidate(name, email, password, confirmPassword)){
            return;
        }
        showLoadingIndicator();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()){
                    hideLoadingIndicator();
                    mNavigator.startActivity(HomeActivity.class, null);
                    AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                            .register(email, FirebaseAuth.getInstance().getCurrentUser().getUid(), name)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(new Consumer<Disposable>() {
                                @Override
                                public void accept(Disposable disposable) {
                                }
                            }).subscribe(new Consumer<BaseResponse>() {
                        @Override
                        public void accept(BaseResponse baseResponse) throws Exception {
                            if (baseResponse.getStatus() == 1){
//                                Toast.makeText(RegisterActivity.this, "Thành Công!", Toast.LENGTH_SHORT).show();
                            }else {
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            handleErrors(throwable);
                        }
                    });
                }else {
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d("abcd", "onFailure: " + e.getLocalizedMessage());
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void handleErrors(Throwable throwable) {
        if (throwable instanceof HttpException) {
            handleHttpExceptions((HttpException) throwable);
            return;
        } else if (throwable instanceof UnknownHostException) {
            Toast.makeText(this, R.string.msg_check_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                R.string.msg_something_went_wrong,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void handleHttpExceptions(HttpException httpException) {
        switch (httpException.code()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(this,
                        R.string.msg_wrong_email_or_password,
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, httpException.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean isValidate(String name, String email, String password, String confirmPassword){
        boolean validate = true;

        if (StringUtils.checkNullOrEmpty(email)) {
            mInputLayoutEmail.setError(getString(R.string.msg_email_should_not_empty));
            validate = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mInputLayoutEmail.setError(getString(R.string.msg_email_not_match));
            validate = false;
        }

        if (StringUtils.checkNullOrEmpty(name)){
            validate = false;
            mInputLayoutName.setError(getString(R.string.msg_name_should_not_empty));
        }

        Log.d("cuonghx", "isValidate: " + password + confirmPassword);

        if (StringUtils.checkNullOrEmpty(password)) {
            mInputLayoutPassword.setError(getString(R.string.msg_password_should_not_empty));
            validate = false;
        }else if (password.length() < 6 ){
            mInputLayoutPassword.setError(getString(R.string.msg_password_should_be_at_least_6_characters
            ));
            validate = false;
        } else if (!password.equals(confirmPassword)) {
            mInputLayoutConfirmPassword.setError(getString(R.string.msg_confimpassword_should_match_password));
            validate = false;
        }

        return validate;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        mInputLayoutName.setError(null);
        mInputLayoutEmail.setError(null);
        mInputLayoutPassword.setError(null);
        mInputLayoutConfirmPassword.setError(null);
    }
    public void showLoadingIndicator() {
        mNavigator.disableUserInteraction();
        mProgressBarLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoadingIndicator() {
        mNavigator.enableUserInteraction();
        mProgressBarLoading.setVisibility(View.GONE);
    }

}
