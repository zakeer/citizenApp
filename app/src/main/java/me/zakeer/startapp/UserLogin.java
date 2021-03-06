package me.zakeer.startapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class UserLogin extends Activity {

    CallbackManager callbackManager;
    Menu menu;
    Button loginBtn, registerBtn, fbLoginBtn, loginWithPhonebtn;
    LinearLayout userBtns, btLayout;
    LoginButton fbLoginButton;
    ImageView logo;
    EditText userName, password, rePassword;
    TextView tvRef, tvLoginText;
    AlertDialog.Builder dialog;
    int fb = 0;
    int loginState = 0;
    int user = 1;

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobileNetState = connectivityManager.getNetworkInfo(0).getState();
        NetworkInfo.State wifiState = connectivityManager.getNetworkInfo(1).getState();

        if (wifiState == NetworkInfo.State.CONNECTED) {
            return true;
        } else
            return (wifiState == NetworkInfo.State.DISCONNECTED || wifiState == NetworkInfo.State.UNKNOWN)
                    && mobileNetState == NetworkInfo.State.CONNECTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.setApplicationId(getResources().getString(R.string.app_id));
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.layout_login_citizen);

        tvLoginText = (TextView) findViewById(R.id.tvLoginText);
        tvRef = (TextView) findViewById(R.id.tvRef);
        logo = (ImageView) findViewById(R.id.login_logo);

        if(isNetworkAvailable(this)) {
            //showDialog();
        } else {
            showDialog("Internet Data Required", "net");
        }

        userName = (EditText) findViewById(R.id.etusername);
        password = (EditText) findViewById(R.id.etPassword);
        rePassword = (EditText) findViewById(R.id.etRePassword);

        userName.setText("7898");
        password.setText("123");

        loginBtn = (Button) findViewById(R.id.btnLogin);
        loginWithPhonebtn = (Button) findViewById(R.id.btnLoginWithPhone);
        registerBtn = (Button) findViewById(R.id.btnRegister);
        fbLoginBtn = (Button) findViewById(R.id.btnFbLogin);

        userBtns = (LinearLayout) findViewById(R.id.userButtons);
        btLayout = (LinearLayout) findViewById(R.id.bottomLayout);

        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        //fbLoginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 10 && loginState == 0) {
                    //fbLoginButton.setVisibility(View.VISIBLE);
                } else {
                    //fbLoginButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userName.getText().toString();
                String paswrd = password.getText().toString();
                loginCheck login = new loginCheck();
                if (username.equals("") && paswrd.equals("")) {
                    Toast.makeText(getApplicationContext(), "All Fields Required", Toast.LENGTH_SHORT).show();
                } else {
                    if (loginState == 1) {
                        Log.d("From :", "Admin");
                        login.execute("http://citizen.turpymobileapps.com/login.php", username, paswrd);
                    } else {
                        Log.d("From :", "USer");
                        login.execute("http://citizen.turpymobileapps.com/userlogin.php", username, paswrd);
                    }

                }
            }
        });

        fbLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLayout();
            }
        });

        loginWithPhonebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeForm(true);
                loginState = 1;
                changeLayout();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeForm(false);
            }
        });


        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Success : ", "Success");
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                JSONObject json = response.getJSONObject();
                                try {
                                    String userId = (String) json.get("id");
                                    if (userId != null) {
                                        String username = userName.getText().toString();
                                        loginCheck login = new loginCheck();
                                        if (username.equals("")) {
                                            Toast.makeText(getApplicationContext(), "All Fields Required", Toast.LENGTH_SHORT).show();
                                        } else {
                                            login.execute("http://citizen.turpymobileapps.com/userlogin.php", username, userId, "fb");
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }


                        }
                ).executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("Success : ", "Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Success : ", "Error " + error);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fb == 1) {
                fbLayout();
                if (user == 0) {
                    changeForm(true);
                }
                return true;
            }
            if (user == 0) {
                changeForm(true);
                return true;
            }

            if (loginState == 1) {
                changeLayout();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_user_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.official_login) {
            changeLayout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeForm(Boolean checkBtn) {
        String username = userName.getText().toString();
        String paswrd = password.getText().toString();
        String rePaswrd = rePassword.getText().toString();

        if (user == 1) {
            user = 0;
            rePassword.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
            logo.setVisibility(View.GONE);
            fbLoginButton.setVisibility(View.GONE);
            userName.setText("");
            password.setText("");
            tvLoginText.setText("User Registration Form");
            loginWithPhonebtn.setVisibility(View.VISIBLE);
            userName.setHint("Enter Phone Number");
            userName.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
            if (user == 0 && !checkBtn) {
                if (username.equals("") && paswrd.equals("") && paswrd.equals("")) {
                    Toast.makeText(getApplicationContext(), "All Fields Required", Toast.LENGTH_SHORT).show();
                } else {
                    if (paswrd.equals(rePaswrd) == false) {
                        Toast.makeText(getApplicationContext(), "Password Does not Match", Toast.LENGTH_SHORT).show();
                    } else {
                        loginCheck login = new loginCheck();
                        login.execute("http://citizen.turpymobileapps.com/user_reg.php", username, paswrd, "register");
                    }
                }

            } else {
                user = 1;
                rePassword.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);
                logo.setVisibility(View.VISIBLE);
                fbLoginButton.setVisibility(View.VISIBLE);
                tvLoginText.setText("User Login");
                loginWithPhonebtn.setVisibility(View.GONE);
            }
        }
    }

    public void fbLayout() {
        String username = userName.getText().toString();
        if (fb == 0) {
            fb = 1;
            userName.setText("");
            userName.setHint("Enter Phone Number");
            userName.setInputType(InputType.TYPE_CLASS_PHONE);
            fbLoginButton.setVisibility(View.VISIBLE);
            password.setVisibility(View.GONE);
            rePassword.setVisibility(View.GONE);
            btLayout.setVisibility(View.GONE);
            userBtns.setVisibility(View.GONE);
            tvRef.setVisibility(View.GONE);
            tvLoginText.setText("Login with Phone");
            logo.setVisibility(View.VISIBLE);
            Drawable image = getResources().getDrawable(R.drawable.citizen_logo);
            logo.setImageDrawable(image);
        } else {
            fb = 0;
            fbLoginButton.setVisibility(View.GONE);
            logo.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.VISIBLE);
            userBtns.setVisibility(View.VISIBLE);
            btLayout.setVisibility(View.VISIBLE);
            tvRef.setVisibility(View.VISIBLE);
            tvLoginText.setText("User Login");
            loginWithPhonebtn.setVisibility(View.GONE);
        }
    }

    public void changeLayout(){
        loginState = (loginState == 1) ? 0 : 1;
        fb = 1;
        fbLayout();
        if(loginState == 1) {
            user = 0;
            changeForm(true);
            tvLoginText.setText("Official Login");
            Drawable image = getResources().getDrawable(R.drawable.police_login);
            logo.setImageDrawable(image);
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.user_login));
            registerBtn.setVisibility(View.GONE);
            fbLoginButton.setVisibility(View.GONE);
            fbLoginBtn.setVisibility(View.GONE);
            tvRef.setVisibility(View.GONE);
            userName.setText("police");
            password.setText("password");
            userName.setInputType(InputType.TYPE_CLASS_TEXT);
            userName.setHint("Enter Username");
        } else {
            tvLoginText.setText("User Login");
            Drawable image = getResources().getDrawable(R.drawable.citizen_logo);
            logo.setImageDrawable(image);
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.official_login));
            registerBtn.setVisibility(View.VISIBLE);
            userName.setText("7898");
            password.setText("123");
            rePassword.setVisibility(View.GONE);
            userName.setInputType(InputType.TYPE_CLASS_PHONE);
            userName.setHint("Enter Phone Number");
            fbLoginButton.setVisibility(View.GONE);
            fbLoginBtn.setVisibility(View.VISIBLE);
            tvRef.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void showDialog(String msg, String type){
        String title = msg;
        dialog = new AlertDialog.Builder(UserLogin.this);
        dialog.setIcon(R.drawable.alert);
        dialog.setTitle(title);

        if(type.equals("net")) {
            dialog.setCancelable(false);
            dialog.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setPositiveButton("Enabled Network", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                }
            });
        } else if(type.equals("success")){
            dialog.setCancelable(true);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            dialog.setCancelable(true);
            dialog.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }

    public class loginCheck extends AsyncTask<String, String, String> {

        private ProgressDialog dialog = new ProgressDialog(UserLogin.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Loading");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(params[0]);
            HttpResponse response = null;
            List<NameValuePair> param = new ArrayList<NameValuePair>();

            if (params.length == 4) {
                if (params[3].equals("register")) {
                    param.add(new BasicNameValuePair("phone", params[1]));
                    param.add(new BasicNameValuePair("password", params[2]));
                    param.add(new BasicNameValuePair("password1", params[2]));
                } else {
                    param.add(new BasicNameValuePair("phone", params[1]));
                    param.add(new BasicNameValuePair("userid", params[2]));
                    param.add(new BasicNameValuePair("fb", "1"));
                    Log.i("Phone, userid" ,  params[1] + " " +params[2]);
                }

            } else {
                if (loginState == 1) {
                    param.add(new BasicNameValuePair("username", params[1]));
                } else {
                    param.add(new BasicNameValuePair("phone", params[1]));
                }

                param.add(new BasicNameValuePair("password", params[2]));
            }

            try {
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(param, HTTP.UTF_8);
                //postRequest.setHeader("host",params[1]);
                postRequest.setEntity(ent);
                HttpResponse responsePOST = client.execute(postRequest);
                HttpEntity resEntity = responsePOST.getEntity();
                return (resEntity != null) ? EntityUtils.toString(resEntity) : null;

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            if(s != null) {
                Log.d("Server Data : ", s);
                try {
                    JSONObject serverData = new JSONObject(s);
                    String status = serverData.getString("message");

                    if(status.equals("User Register successful")) {
                        userName.setText("");
                        password.setText("");
                        rePassword.setText("");
                        loginState = 1;
                        showDialog(status, "success");
                        changeLayout();
                    }else if (status.equals("login successful")) {
                        userName.setText("");
                        password.setText("");
                        if (loginState == 1) {
                            Intent intent = new Intent(getApplicationContext(), OfficialActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainScreen.class);
                            startActivity(intent);
                        }

                    } else {
                        showDialog(status, "fail");
                        System.out.print("login failled");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
