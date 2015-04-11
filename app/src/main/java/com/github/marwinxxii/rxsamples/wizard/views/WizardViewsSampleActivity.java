package com.github.marwinxxii.rxsamples.wizard.views;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import com.github.marwinxxii.rxsamples.R;
import com.github.marwinxxii.rxsamples.wizard.Pizza;
import com.github.marwinxxii.rxsamples.wizard.PizzaOrder;
import com.github.marwinxxii.rxsamples.wizard.Size;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayDeque;

public class WizardViewsSampleActivity extends Activity {
    private SelectPizzaView mSelectPizzaView;
    private SelectSizeView mSelectSizeView;
    private SubmitOrderView mSubmitOrderView;

    private ArrayDeque<View> mBackStack = new ArrayDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_wizard_views);
        mSelectPizzaView = (SelectPizzaView) findViewById(R.id.sample_wizard_views_step1);
        mSelectSizeView = (SelectSizeView) findViewById(R.id.sample_wizard_views_step2);
        mSubmitOrderView = (SubmitOrderView) findViewById(R.id.sample_wizard_views_step3);

        show(mSelectPizzaView);

        mSelectPizzaView.observeSelectedPizza()
          .doOnNext(new Action1<Pizza>() {
              @Override
              public void call(Pizza pizza) {
                  show(mSelectSizeView);
              }
          })
          .flatMap(new Func1<Pizza, Observable<Pair<Pizza, Size>>>() {
              @Override
              public Observable<Pair<Pizza, Size>> call(final Pizza pizza) {
                  return mSelectSizeView.observeSelectedSize().map(new Func1<Size, Pair<Pizza, Size>>() {
                      @Override
                      public Pair<Pizza, Size> call(Size size) {
                          return new Pair<>(pizza, size);
                      }
                  });
              }
          })
          .doOnNext(new Action1<Pair<Pizza, Size>>() {
              @Override
              public void call(Pair<Pizza, Size> pizzaSizePair) {
                  show(mSubmitOrderView);
              }
          })
          .flatMap(new Func1<Pair<Pizza, Size>, Observable<PizzaOrder>>() {
              @Override
              public Observable<PizzaOrder> call(Pair<Pizza, Size> pizzaSizePair) {
                  return mSubmitOrderView.setPizza(pizzaSizePair.first)
                    .setSize(pizzaSizePair.second)
                    .observeOrder();
              }
          })
          .subscribe(new Action1<PizzaOrder>() {
              @Override
              public void call(PizzaOrder order) {
                  String text = getString(R.string.sample_wizard_result, order.getSize(), order.getType(), order.getPhone());
                  Toast.makeText(WizardViewsSampleActivity.this, text, Toast.LENGTH_SHORT).show();
              }
          });
    }

    private void show(View view) {
        if (view instanceof SubmitOrderView) {
            mSelectSizeView.setVisibility(View.GONE);
        } else if (view instanceof SelectSizeView) {
            mSelectPizzaView.setVisibility(View.GONE);
        } else {
        }
        view.setVisibility(View.VISIBLE);
        mBackStack.push(view);
    }

    private void onBackStackChange() {
        View view = mBackStack.pop();
        if (view == null) {
            WizardViewsSampleActivity.super.onBackPressed();
            return;
        }
        if (view instanceof SubmitOrderView) {
            view.setVisibility(View.GONE);
            mSelectSizeView.setVisibility(View.VISIBLE);
        } else if (view instanceof SelectSizeView) {
            view.setVisibility(View.GONE);
            mSelectPizzaView.setVisibility(View.VISIBLE);
        } else {
            WizardViewsSampleActivity.super.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        onBackStackChange();
    }
}
