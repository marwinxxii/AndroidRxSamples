package com.github.marwinxxii.rxsamples.wizard;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;
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
        observeFragmentShown(new SelectPizzaFragment(), false)
          .flatMap(new Func1<SelectPizzaFragment, Observable<Pizza>>() {
              @Override
              public Observable<Pizza> call(SelectPizzaFragment selectPizzaFragment) {
                  return selectPizzaFragment.observeSelectedPizza().first();
              }
          })
          .flatMap(new Func1<Pizza, Observable<Pair<Pizza, Size>>>() {
              @Override
              public Observable<Pair<Pizza, Size>> call(final Pizza pizza) {
                  /* view is created each time fragment is shown (first time, pop from back stack)
                  fragment is not recreated after popping from back stack
                  flatMap ensures that on each view create we observe results from new view */
                  return observeFragmentShown(new SelectSizeFragment(), true)
                    .flatMap(new Func1<SelectSizeFragment, Observable<Pair<Pizza, Size>>>() {
                        @Override
                        public Observable<Pair<Pizza, Size>> call(SelectSizeFragment selectSizeFragment) {
                            return selectSizeFragment
                              .observeSelectedSize()
                              .map(new Func1<Size, Pair<Pizza, Size>>() {
                                  @Override
                                  public Pair<Pizza, Size> call(Size size) {
                                      return new Pair<>(pizza, size);
                                  }
                              })
                              .first();//prevents leaking of views and fragment inside persistent sequence
                        }
                    });
              }
          })
          .flatMap(new Func1<Pair<Pizza, Size>, Observable<PizzaOrder>>() {
              @Override
              public Observable<PizzaOrder> call(Pair<Pizza, Size> params) {
                  return observeFragmentShown(SubmitOrderFragment.create(params.first, params.second), true)
                    .flatMap(new Func1<SubmitOrderFragment, Observable<PizzaOrder>>() {
                        @Override
                        public Observable<PizzaOrder> call(SubmitOrderFragment submitOrderFragment) {
                            return submitOrderFragment.observeOrder().first();
                        }
                    });
              }
          }).subscribe(new Action1<PizzaOrder>() {
            @Override
            public void call(PizzaOrder order) {
                String text = String.format("Deliver %1$s %2$s pizza to %3$s", order.getSize(), order.getType(), order.getPhone());
                Toast.makeText(WizardSampleActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private <T extends BaseFragment> Observable<T> observeFragmentShown(T fragment, boolean addToBackStack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction()
          .replace(android.R.id.content, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        transaction.commit();
        return fragment.observeViewCreated();
    }
}
