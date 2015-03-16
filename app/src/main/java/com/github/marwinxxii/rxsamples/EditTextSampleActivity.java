package com.github.marwinxxii.rxsamples;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import rx.Observable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func2;

public class EditTextSampleActivity extends Activity {
    private EditText mHost, mPath, mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_edittext);
        mHost = (EditText) findViewById(R.id.ets_host);
        mPath = (EditText) findViewById(R.id.ets_path);
        mResult = (EditText) findViewById(R.id.result);
        mResult.setEnabled(false);
        initRxWay();
        //initOldSchoolWay();
    }

    private void setResultUrl(Uri uri) {
        // set text only when both values are present
        if (uri.getAuthority().isEmpty()) {
            mResult.setText("");
        } else {
            mResult.setText(uri.toString());
        }
    }
    
    private static Uri buildUri(String host, String path) {
        return new Uri.Builder()
          .scheme("http")
          .encodedAuthority(host)
          .encodedPath("/some/path")
          .appendEncodedPath(path)
          .build();
    }

    private void initRxWay() {
        Observable.combineLatest(
          WidgetObservable.text(mHost, false),//false - do not emit start value
          WidgetObservable.text(mPath, true),//can be false if both fields are mandatory for result URL

          new Func2<OnTextChangeEvent, OnTextChangeEvent, Uri>() {
              @Override
              public Uri call(OnTextChangeEvent onHostChangeEvent, OnTextChangeEvent onPathChangeEvent) {
                  return buildUri(onHostChangeEvent.text().toString(), onPathChangeEvent.text().toString());
              }
          }
        ).subscribe(new Action1<Uri>() {
            @Override
            public void call(Uri uri) {
                setResultUrl(uri);
            }
        });
    }

    private void initOldSchoolWay() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Uri uri = buildUri(mHost.getText().toString(), mPath.getText().toString());
                setResultUrl(uri);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        mHost.addTextChangedListener(watcher);
        mPath.addTextChangedListener(watcher);
    }
}
