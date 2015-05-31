package com.github.marwinxxii.rxsamples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CombineIOSampleActivity extends Activity {
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_mix);
        final Button calculateBtn = (Button) findViewById(R.id.calculate);
        final EditText result = (EditText) findViewById(R.id.result);
        result.setEnabled(false);
        final ProgressBar progress = (ProgressBar) findViewById(android.R.id.progress);

        //NOTE: this sample doesn't handle orientation change and other activity restore cases
        mSubscription = ViewObservable.clicks(calculateBtn)
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
                  return observeBackgroundOperation()
                    .observeOn(AndroidSchedulers.mainThread())//interaction with UI must be performed on main thread
                    .doOnError(new Action1<Throwable>() {//handle error before it will be suppressed
                        @Override
                        public void call(Throwable throwable) {
                            progress.setVisibility(View.GONE);
                            calculateBtn.setEnabled(true);
                            Toast.makeText(CombineIOSampleActivity.this, R.string.mix_error_message, Toast.LENGTH_SHORT).show();
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
        /*if you have multiple subscriptions you can use CompositeSubscription, add subscriptions there and
        unsubscribe to all of them in one place.*/
    }

    @Override
    protected void onStop() {
        mSubscription.unsubscribe();//prevent leaking of activity
        super.onStop();
    }

    private static Observable<Integer> observeBackgroundOperation() {
        return Observable.just(new Random().nextInt())
          .delay(3L, TimeUnit.SECONDS)//by default operates on computation Scheduler
          .doOnNext(new Action1<Integer>() {
              @Override
              public void call(Integer integer) {
                  if (new Random().nextBoolean()) {
                      //simulate error
                      throw new RuntimeException("Error calculating value");
                  }
              }
          });
    }

    //Transformer to be used in flatMap
    public static class IOErrorTransformer<T> implements Observable.Transformer<T, T> {
        private final Action1<Throwable> mErrorHandler;

        public IOErrorTransformer(Action1<Throwable> errorHandler) {
            mErrorHandler = errorHandler;
        }

        @Override
        public Observable<T> call(Observable<T> observable) {
            return observable
              .observeOn(AndroidSchedulers.mainThread())//interaction with UI must be performed on main thread
              .doOnError(mErrorHandler)
              .onErrorResumeNext(Observable.<T>empty());//prevent observable from breaking
        }
    }
}
