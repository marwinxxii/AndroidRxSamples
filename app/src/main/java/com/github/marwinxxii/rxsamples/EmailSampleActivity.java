package com.github.marwinxxii.rxsamples;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_email);
        EditText loginText = (EditText) findViewById(R.id.login);
        Spinner domainSpinner = (Spinner) findViewById(R.id.domain);
        final EditText resultText = (EditText) findViewById(R.id.result);
        resultText.setEnabled(false);

        Observable.combineLatest(
          WidgetObservable.text(loginText, true),
          observeSelect(domainSpinner),

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
                resultText.setText(email);
            }
        });
    }

    public static Observable<String> observeSelect(Spinner spinner) {
        final PublishSubject<String> selectSubject = PublishSubject.create();
        // for production code, unsubscribe, UI thread assertions are needed
        // see WidgetObservable for example
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
}
