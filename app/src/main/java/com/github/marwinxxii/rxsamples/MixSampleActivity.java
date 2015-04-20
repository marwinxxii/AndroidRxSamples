package com.github.marwinxxii.rxsamples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MixSampleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_mix);
        final Button calculateBtn = (Button) findViewById(R.id.calculate);
        final EditText result = (EditText) findViewById(R.id.result);
        result.setEnabled(false);
        final ProgressBar progress = (ProgressBar) findViewById(android.R.id.progress);

        //NOTE: this sample doesn't handle orientation change and other activity recreation cases
        ViewObservable.clicks(calculateBtn)
          .doOnNext(new Action1<OnClickEvent>() {
              @Override
              public void call(OnClickEvent onClickEvent) {
                  calculateBtn.setEnabled(false);
                  progress.setVisibility(View.VISIBLE);
                  result.setText("");
              }
          })
          .flatMap(new Func1<OnClickEvent, Observable<Integer>>() {
              @Override
              public Observable<Integer> call(OnClickEvent onClickEvent) {
                  return observeLongApiCall()
                    .observeOn(AndroidSchedulers.mainThread())//interaction with UI must be performed on main thread
                    .doOnError(new Action1<Throwable>() {//handle error before it will be suppressed
                        @Override
                        public void call(Throwable throwable) {
                            progress.setVisibility(View.GONE);
                            calculateBtn.setEnabled(true);
                            Toast.makeText(MixSampleActivity.this, R.string.mix_error_message, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .onErrorResumeNext(Observable.<Integer>empty());//prevent observable from breaking
              }
          })
          .subscribe(new Action1<Integer>() {
              //not handling errors, because they are handled for API calls, and normally no other errors should appear
              @Override
              public void call(Integer integer) {
                  progress.setVisibility(View.GONE);
                  calculateBtn.setEnabled(true);
                  result.setText(integer.toString());
              }
          });
    }

    private static Observable<Integer> observeLongApiCall() {
        return Observable.just(new Random().nextInt())
          .delay(3L, TimeUnit.SECONDS)//by default operates on computation Scheduler
          .doOnNext(new Action1<Integer>() {
              @Override
              public void call(Integer integer) {
                  if (new Random().nextBoolean()) {
                      throw new RuntimeException("Error calculating value");
                  }
              }
          });
    }
}
