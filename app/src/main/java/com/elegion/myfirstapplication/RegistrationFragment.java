package com.elegion.myfirstapplication;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.elegion.myfirstapplication.albums.AlbumsActivity;
import com.elegion.myfirstapplication.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;


public class RegistrationFragment extends Fragment {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private EditText mEmail;
    private EditText mName;
    private EditText mPassword;
    private EditText mPasswordAgain;
    private Button mRegistration;

    public static RegistrationFragment newInstance() {
        return new RegistrationFragment();
    }

    private View.OnClickListener mOnRegistrationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isInputValid()) {
                User user = new User(
                        mEmail.getText().toString(),
                        mName.getText().toString(),
                        mPassword.getText().toString());

                ApiUtils.getApiService().registration(user).enqueue(
                        new retrofit2.Callback<Void>() {
                            //используем Handler, чтобы показывать ошибки в Main потоке, т.к. наши коллбеки возвращаются в рабочем потоке
                            final AuthActivity activity = (AuthActivity) getActivity();
                            Handler mainHandler = new Handler(activity.getMainLooper());

                            @Override
                            public void onResponse(retrofit2.Call<Void> call, final retrofit2.Response<Void> response) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!response.isSuccessful()) {
                                            //completed добавить полноценную обработку ошибок по кодам ответа от сервера и телу запроса

                                            ApiError error = ApiUtils.parseError(response,response.code());
                                            highlightErrors(error, activity);
                                        } else {
                                            showMessage(R.string.registration_success);
                                            getFragmentManager().popBackStack();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showMessage(R.string.request_error);
                                    }
                                });
                            }
                        });
            } else {
                showMessage(R.string.input_error);
            }
        }
    };

    private void highlightErrors(ApiError error, SingleFragmentActivity activity) {
        ApiError.ErrorBean errorBean = error.getError();
        int code = error.getCode();
        String commonMessage = activity.getResponseErrorMessage(code);

        if (errorBean != null) {
            String currentError = errorBean.getNameFirstError();
            if (!currentError.isEmpty())
                mName.setError(currentError);
            else
                mName.setError(null);

            currentError = errorBean.getEmailFirstError();

            //Подсвечиваем все поля - имя, еmai
            if (!currentError.isEmpty()) {
                mEmail.setError(commonMessage);
                mName.setError(commonMessage);
            } else {
                mEmail.setError(null);
                mName.setError(null);
            }


            currentError = errorBean.getPasswordFirstError();
            if (!currentError.isEmpty()) {
                mPassword.setError(currentError);
                mPasswordAgain.setError(currentError);
            }
            else{
                mPassword.setError(null);
                mPasswordAgain.setError(currentError);
            }
        } else {
            mEmail.setError(commonMessage);
            mName.setError(commonMessage);
            mPassword.setError(commonMessage);
            mPasswordAgain.setError(commonMessage);
        }

        showMessage(commonMessage);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_registration, container, false);

        mEmail = view.findViewById(R.id.etEmail);
        mName = view.findViewById(R.id.etName);
        mPassword = view.findViewById(R.id.etPassword);
        mPasswordAgain = view.findViewById(R.id.tvPasswordAgain);
        mRegistration = view.findViewById(R.id.btnRegistration);

        mRegistration.setOnClickListener(mOnRegistrationClickListener);

        return view;
    }


    private boolean isInputValid() {
        return isEmailValid()
                & isNameValid()
                & isPasswordValid()
                & isRetypedPasswordsValid();
    }


    private boolean isEmailValid() {
        boolean result =
                !TextUtils.isEmpty(mEmail.getText())
                        && Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches();
        if (!result)
            mEmail.setError(getString(R.string.email_validation_error));

        return result;
    }

    private boolean isNameValid() {
        boolean result =!TextUtils.isEmpty(mName.getText());

        if (!result)
            mName.setError(getString(R.string.username_validation_error));

        return result;
    }

    private boolean isPasswordValid() {
        String password = mPassword.getText().toString();

        boolean result =
                !TextUtils.isEmpty(password)
                        && password.length() >= 8;
        if (!result)
            mPassword.setError(getString(R.string.password_validation_error));

        return result;
    }

    private boolean isRetypedPasswordsValid() {
        String password = mPassword.getText().toString();
        String retypedPassword = mPasswordAgain.getText().toString();

        boolean result = password.equals(retypedPassword);
        if (!result)
            mPasswordAgain.setError(getString(R.string.retyped_password_validation_error));

        return result;
    }


    private void showMessage(@StringRes int string) {
        Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

}
