package com.github.marwinxxii.rxsamples.wizard;

import android.app.*;
import android.os.Bundle;
import android.util.Pair;
import android.widget.*;
import com.github.marwinxxii.rxsamples.R;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class WizardSampleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_wizard);
    }

    @Override
    protected void onStart() {
        super.onStart();//TODO in onCreate
        Observable.just(new SelectPizzaFragment())
          .flatMap(new Func1<SelectPizzaFragment, Observable<Pizza>>() {
              @Override
              public Observable<Pizza> call(SelectPizzaFragment selectPizzaFragment) {
                  showFragment(selectPizzaFragment, false);
                  return selectPizzaFragment.observeSelectedPizza();
              }
          })
          .flatMap(new Func1<Pizza, Observable<Pair<Pizza, Size>>>() {
              @Override
              public Observable<Pair<Pizza, Size>> call(final Pizza pizza) {
                  SelectSizeFragment selectSizeFragment = new SelectSizeFragment();
                  showFragment(selectSizeFragment, true);
                  return selectSizeFragment.observeSelectedSize().map(new Func1<Size, Pair<Pizza, Size>>() {
                      @Override
                      public Pair<Pizza, Size> call(Size size) {
                          return new Pair<>(pizza, size);
                      }
                  });
              }
          })
          .flatMap(new Func1<Pair<Pizza, Size>, Observable<PizzaOrder>>() {
              @Override
              public Observable<PizzaOrder> call(Pair<Pizza, Size> params) {
                  SubmitOrderFragment submitFragment = SubmitOrderFragment.create(params.first, params.second);
                  showFragment(submitFragment, true);
                  return submitFragment.observeOrder();
              }
          }).subscribe(new Action1<PizzaOrder>() {
            @Override
            public void call(PizzaOrder order) {
                String text = String.format("Deliver %1$s %2$s pizza to %3$s", order.getSize(), order.getType(), order.getPhone());
                Toast.makeText(WizardSampleActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction()
          .replace(android.R.id.content, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        transaction.commit();
    }
}
