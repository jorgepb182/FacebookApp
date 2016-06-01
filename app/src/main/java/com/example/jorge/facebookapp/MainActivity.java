package com.example.jorge.facebookapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION = "publish_actions";
    private Button postStatusUpdateButtom;
    private ProfilePictureView profilePictureView;
    private TextView greeting;
    private boolean canPresentSharedDialog;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private ShareDialog sharedDialog;


    private FacebookCallback<Sharer.Result> sharedCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {

            if (result.getPostId() != null) {
                showMesaage("Publicacion enviada.");
            }

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {
            showMesaage("Error al crear la publicación.");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                updateUI();
            }

            @Override
            public void onCancel() {
                showMesaage("No se han dado los permisos para publicar.");
                updateUI();
            }

            @Override
            public void onError(FacebookException error) {
                showMesaage("Ha ocurrido un error al iniciar sesión.");
                updateUI();
            }
        });

        sharedDialog = new ShareDialog(this);
        sharedDialog.registerCallback(callbackManager, sharedCallback);

        setContentView(R.layout.activity_main);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
            }
        };

        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        greeting =  (TextView) findViewById(R.id.greeting);

        postStatusUpdateButtom = (Button)findViewById(R.id.postStatusUpdateButtom);
        postStatusUpdateButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPostStatusUpdate();
            }
        });

        canPresentSharedDialog = ShareDialog.canShow(ShareLinkContent.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppEventsLogger.activateApp(this);
        updateUI();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        postStatusUpdateButtom.setEnabled(enableButtons || canPresentSharedDialog);

        Profile profile = Profile.getCurrentProfile();

        if (enableButtons && profile != null) {
            profilePictureView.setProfileId(profile.getId());
            greeting.setText("Hola " + profile.getFirstName());
        } else {
            profilePictureView.setProfileId(null);
            greeting.setText(null);
        }

    }

    private void onClickPostStatusUpdate() {
        postStatusUpdate();
    }

    private void postStatusUpdate() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null || canPresentSharedDialog) {
            Profile profile = Profile.getCurrentProfile();
            ShareLinkContent linkContent = new ShareLinkContent.Builder().build();

            if (canPresentSharedDialog) {
                sharedDialog.show(linkContent);
            } else if (profile != null && hasPublishPermission()) {
                ShareApi.share(linkContent, sharedCallback);
            }
        }
    }

    private boolean hasPublishPermission () {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains(PERMISSION);
    }

    private void showMesaage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
