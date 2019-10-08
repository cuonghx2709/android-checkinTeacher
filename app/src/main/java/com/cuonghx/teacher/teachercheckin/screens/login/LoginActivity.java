package com.cuonghx.teacher.teachercheckin.screens.login;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cuonghx.teacher.teachercheckin.CheckInApplication;
import com.cuonghx.teacher.teachercheckin.R;
import com.cuonghx.teacher.teachercheckin.data.source.remote.AuthenticationRemoteDataSource;
import com.cuonghx.teacher.teachercheckin.data.source.remote.api.response.BaseResponse;
import com.cuonghx.teacher.teachercheckin.screens.BaseActivity;
import com.cuonghx.teacher.teachercheckin.screens.home.HomeActivity;
import com.cuonghx.teacher.teachercheckin.util.StringUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends BaseActivity implements FacebookCallback<LoginResult>, View.OnClickListener, TextWatcher {

    private AppCompatEditText mInputEditTextEnterEmail;
    private AppCompatEditText mInputEditTextEmailForgot;
    private TextInputLayout mInputLayoutPassword;
    private TextInputLayout mInputLayoutEmail;
    private TextInputEditText mInputEditTextEmail;
    private TextInputEditText mInputEditTextPassword;
    private ProgressBar mProgressBarLoading;

    private  CallbackManager mCallbackManagerFacebook;

    private String TAG = LoginActivity.class.getName();
    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private Dialog dialog;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        setupView();
    }

    private void setupView(){

        mInputLayoutEmail = findViewById(R.id.til_email);
        mInputLayoutPassword = findViewById(R.id.til_password);
        mInputEditTextEmail = findViewById(R.id.tiet_email);
        mInputEditTextPassword = findViewById(R.id.tiet_password);
        mProgressBarLoading = findViewById(R.id.progress_circular_loading);

        findViewById(R.id.bt_login).setOnClickListener(this);
        findViewById(R.id.bt_to_register).setOnClickListener(this);
        findViewById(R.id.bt_forgot_password).setOnClickListener(this);

        mInputEditTextPassword.addTextChangedListener(this);
        mInputEditTextEmail.addTextChangedListener(this);

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mCallbackManagerFacebook = CallbackManager.Factory.create();
        mFirebaseAuth = FirebaseAuth.getInstance();

        LoginButton loginButton = findViewById(R.id.lgbt_facbook);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManagerFacebook,this);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(this);

        if (mFirebaseAuth.getCurrentUser() != null){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            register(CheckInApplication.getInstance().getCurrentEmail(), user.getUid(), user.getDisplayName());
            mNavigator.startActivity(HomeActivity.class, null);
        }
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // Facbook Login Callback

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManagerFacebook.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, R.string.msg_something_went_wrong, Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        showLoadingIndicator();
        handleFacebookAccessToken(loginResult.getAccessToken());
        Log.d(TAG, "onSuccess: ");
    }

    @Override
    public void onCancel() {
        Log.d(TAG, "onCancel: ");
    }

    @Override
    public void onError(FacebookException error) {
        Log.d(TAG, "onError: " + error);
        Toast.makeText(this, R.string.msg_something_went_wrong, Toast.LENGTH_SHORT).show();
    }
    // Connect to firebase Auth
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideLoadingIndicator();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            LoginManager.getInstance().logOut();
                            if (CheckInApplication.getInstance().getCurrentEmail() == null) {
                                dialog = new Dialog(LoginActivity.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setCancelable(false);
                                dialog.setContentView(R.layout.dialog_enter_email);
                                dialog.findViewById(R.id.tv_continue_dialog).setOnClickListener(LoginActivity.this);
                                dialog.findViewById(R.id.tv_cancel_dialog).setOnClickListener(LoginActivity.this);
                                mInputEditTextEnterEmail = dialog.findViewById(R.id.edt_email_dialog);
                                dialog.show();
                            }else {
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                register(CheckInApplication.getInstance().getCurrentEmail(), user.getUid(), user.getDisplayName());
                                mNavigator.startActivity(HomeActivity.class, null);
                            }
                            Log.d(TAG, "onComplete: " + CheckInApplication.getInstance().getCurrentEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.msg_something_went_wrong,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideLoadingIndicator();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "onComplete: " + CheckInApplication.getInstance().getCurrentEmail());
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            register(CheckInApplication.getInstance().getCurrentEmail(), user.getUid(), user.getDisplayName());
                            mNavigator.startActivity(HomeActivity.class, null);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.msg_something_went_wrong, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button:
                showLoadingIndicator();
                signIn();
                break;
            case R.id.tv_continue_dialog:
                String text = String.valueOf(mInputEditTextEnterEmail.getText());
                dialog.cancel();
                mInputEditTextEnterEmail.setText("");
                FirebaseAuth.getInstance().getCurrentUser().updateEmail(text)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                    register(CheckInApplication.getInstance().getCurrentEmail(), user.getUid(), user.getDisplayName());
                                    Toast.makeText(LoginActivity.this, "Tài khoản đã được cập nhật email", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "User email address updated.");
                                    mNavigator.startActivity(HomeActivity.class, null);
                                }else {
                                    dialog.show();
                                    Toast.makeText(LoginActivity.this, "Email đã tồn tại chọn email khác", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case R.id.tv_cancel_dialog :
                dialog.cancel();
                break;
            case R.id.bt_login :
                final String email = String.valueOf(mInputEditTextEmail.getText());
                String password = String.valueOf(mInputEditTextPassword.getText());

                if (!validateEmailAndPassword(email, password)){
                    return;
                }
                showLoadingIndicator();
                mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        hideLoadingIndicator();
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this , email + " logged-in", Toast.LENGTH_SHORT)
                                    .show();
                            mNavigator.startActivity(HomeActivity.class, null);
                        }else {
                            Toast.makeText(LoginActivity.this, R.string.msg_wrong_email_or_password, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.bt_to_register:
                mNavigator.startActivity(RegisterActivity.class, null);
                break;
            case R.id.bt_forgot_password:
                dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_forgot_email);
                dialog.findViewById(R.id.tv_send_dialog).setOnClickListener(this);
                dialog.findViewById(R.id.tv_cancel_dialog).setOnClickListener(this);
                mInputEditTextEmailForgot = dialog.findViewById(R.id.edt_email_dialog);
                dialog.show();
                break;
            case R.id.tv_send_dialog:
                dialog.cancel();
                FirebaseAuth.getInstance().sendPasswordResetEmail(String.valueOf(mInputEditTextEmailForgot.getText())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if  (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, getString(R.string.msg_check_your_email), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(LoginActivity.this, getString(R.string.msg_something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
                    }
                });
                break;
        }
    }

    private boolean validateEmailAndPassword(String email, String password) {
        boolean validate = true;

        if (StringUtils.checkNullOrEmpty(email)) {
            mInputLayoutEmail.setError(getString(R.string.msg_email_should_not_empty));
            validate = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mInputLayoutEmail.setError(getString(R.string.msg_email_not_match));
            validate = false;
        }

        if (StringUtils.checkNullOrEmpty(password)) {
            mInputLayoutPassword.setError(getString(R.string.msg_password_should_not_empty));
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
        mInputLayoutEmail.setError(null);
        mInputLayoutPassword.setError(null);
    }
    public void showLoadingIndicator() {
        mNavigator.disableUserInteraction();
        mProgressBarLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoadingIndicator() {
        mNavigator.enableUserInteraction();
        mProgressBarLoading.setVisibility(View.GONE);
    }


    private void register(String email, String id, String name){
        AuthenticationRemoteDataSource.getInstance(CheckInApplication.getInstance().getCheckInApi())
                .register(email, id, name)
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
                    Log.d(TAG, "accept: ok");
                }else {
                    Log.d(TAG, "accept: reject");
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });


    }
}
