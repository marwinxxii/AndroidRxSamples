package com.github.marwinxxii.rxsamples.wizard.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import com.github.marwinxxii.rxsamples.R;
import com.github.marwinxxii.rxsamples.wizard.Pizza;
import com.github.marwinxxii.rxsamples.wizard.PizzaOrder;
import com.github.marwinxxii.rxsamples.wizard.Size;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

public class SubmitOrderView extends FrameLayout implements IWizardStepView {
    private EditText mPhone;
    private Button mSubmitButton;
    private EditText mEditType;
    private EditText mEditSize;
    private Pizza mPizza;
    private Size mSize;

    public SubmitOrderView(Context context) {
        super(context);
        init();
    }

    public SubmitOrderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubmitOrderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SubmitOrderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.sample_wizard_step3, this, true);

        mEditType = (EditText) view.findViewById(R.id.sample_wizard_step3_type);
        mEditType.setEnabled(false);

        mEditSize = (EditText) view.findViewById(R.id.sample_wizard_step3_size);
        mEditSize.setEnabled(false);

        mPhone = (EditText) view.findViewById(R.id.sample_wizard_step3_phone);
        mSubmitButton = (Button) view.findViewById(R.id.submit);
    }

    @Override
    public CharSequence getStepTitle() {
        return getResources().getString(R.string.sample_wizard_step3_title);
    }

    @Override
    public void reset() {
        mEditType.setText("");
        mEditSize.setText("");
        mPhone.setText("");
    }

    public SubmitOrderView setPizza(Pizza pizza) {
        mPizza = pizza;
        mEditType.setText(pizza.toString());
        return this;
    }

    public SubmitOrderView setSize(Size size) {
        mSize = size;
        mEditSize.setText(size.toString());
        return this;
    }

    public Observable<PizzaOrder> observeOrder() {
        return WidgetObservable.text(mPhone)
          .doOnNext(new Action1<OnTextChangeEvent>() {
              @Override
              public void call(OnTextChangeEvent onPhoneChangeEvent) {
                  mSubmitButton.setEnabled(!TextUtils.isEmpty(onPhoneChangeEvent.text()));
              }
          })
          .sample(ViewObservable.clicks(mSubmitButton))
          .map(new Func1<OnTextChangeEvent, PizzaOrder>() {
              @Override
              public PizzaOrder call(OnTextChangeEvent onPhoneChangeEvent) {
                  return new PizzaOrder(mPizza, mSize, onPhoneChangeEvent.text().toString());
              }
          });
    }
}
