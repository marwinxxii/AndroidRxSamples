package com.github.marwinxxii.rxsamples.wizard;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.github.marwinxxii.rxsamples.R;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

public class SubmitOrderFragment extends BaseFragment {
    private Pizza mPizza;
    private Size mSize;
    private EditText mPhone;
    private Button mSubmitButton;

    public static SubmitOrderFragment create(Pizza pizza, Size size) {
        Bundle args = new Bundle();
        args.putSerializable(Pizza.class.getSimpleName(), pizza);
        args.putSerializable(Size.class.getSimpleName(), size);
        SubmitOrderFragment fragment = new SubmitOrderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.sample_wizard_step3_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sample_wizard_step3, container, false);
        Bundle args = getArguments();
        mPizza = (Pizza) args.getSerializable(Pizza.class.getSimpleName());
        mSize = (Size) args.getSerializable(Size.class.getSimpleName());

        EditText type = (EditText) view.findViewById(R.id.sample_wizard_step3_type);
        type.setEnabled(false);
        type.setText(mPizza.toString());

        EditText size = (EditText) view.findViewById(R.id.sample_wizard_step3_size);
        size.setEnabled(false);
        size.setText(mSize.toString());

        mPhone = (EditText) view.findViewById(R.id.sample_wizard_step3_phone);
        mSubmitButton = (Button) view.findViewById(R.id.submit);

        return view;
    }

    public Observable<PizzaOrder> observeOrder() {
        return observeViewCreated().flatMap(new Func1<Void, Observable<PizzaOrder>>() {
            @Override
            public Observable<PizzaOrder> call(Void aVoid) {
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
                  })
                  .first();
            }
        });
    }
}
