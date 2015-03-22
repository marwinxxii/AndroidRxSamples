package com.github.marwinxxii.rxsamples;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import rx.Observable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class EmailSampleActivity extends Activity {
    private EditText mLoginText;
    private Spinner mDomainSpinner;
    private EditText mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_email);
        mLoginText = (EditText) findViewById(R.id.login);
        mDomainSpinner = (Spinner) findViewById(R.id.domain);
        mResultText = (EditText) findViewById(R.id.result);
        mResultText.setEnabled(false);
        setupFields();
        //setupFieldsOldWay();
    }
    
    private void setupFields() {
        Observable.combineLatest(
          WidgetObservable.text(mLoginText, false),//false - do not emit start value
          observeSelect(mDomainSpinner),

          new Func2<OnTextChangeEvent, String, String>() {
              @Override
              public String call(OnTextChangeEvent onTextChangeEvent, String domain) {
                  CharSequence userLogin = onTextChangeEvent.text();
                  if (TextUtils.isEmpty(userLogin)) {
                      return "";
                  } else {
                      return userLogin.toString() + '@' + domain;
                  }
              }
          }
        ).subscribe(new Action1<String>() {
            @Override
            public void call(String email) {
                mResultText.setText(email);
            }
        });
    }

    public static Observable<String> observeSelect(Spinner spinner) {
        final PublishSubject<String> selectSubject = PublishSubject.create();
        // for production code, unsubscribe, UI thread assertions are needed
        // see WidgetObservable from rxandroid for example
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                selectSubject.onNext(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return selectSubject;
    }

    private void setupFieldsOldWay() {
        mLoginText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onFieldUpdated();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mDomainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFieldUpdated();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void onFieldUpdated() {
        String login = mLoginText.getText().toString();
        String domain = (String) mDomainSpinner.getSelectedItem();
        if (TextUtils.isEmpty(login)) {
            mResultText.setText("");
        } else {
            mResultText.setText(login + '@' + domain);
        }
    }
}
