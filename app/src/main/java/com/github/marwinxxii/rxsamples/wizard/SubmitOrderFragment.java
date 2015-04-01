package com.github.marwinxxii.rxsamples.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import com.github.marwinxxii.rxsamples.R;
import rx.Observable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;

public class SubmitOrderFragment extends Fragment {
    private Pizza mPizza;
    private Size mSize;
    private EditText mPhone;
    private Button mSubmitButton;
    private Animation mValidateAnim;

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

        mValidateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.validation);
        return view;
    }

    public Observable<PizzaOrder> observeOrder() {
        return ViewObservable.clicks(mSubmitButton)
          .map(new Func1<OnClickEvent, CharSequence>() {
              @Override
              public CharSequence call(OnClickEvent onClickEvent) {
                  return mPhone.getText();
              }
          })
          .doOnNext(new Action1<CharSequence>() {
              @Override
              public void call(CharSequence phone) {
                  if (TextUtils.isEmpty(phone)) {
                      mPhone.startAnimation(mValidateAnim);
                  }
              }
          })
          .filter(new Func1<CharSequence, Boolean>() {
              @Override
              public Boolean call(CharSequence phone) {
                  return !TextUtils.isEmpty(phone);
              }
          })
          .map(new Func1<CharSequence, PizzaOrder>() {
              @Override
              public PizzaOrder call(CharSequence phone) {
                  return new PizzaOrder(mPizza, mSize, phone.toString());
              }
          });
    }
}
