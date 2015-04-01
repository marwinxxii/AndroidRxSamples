package com.github.marwinxxii.rxsamples.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import com.github.marwinxxii.rxsamples.R;
import rx.Observable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class SelectSizeFragment extends Fragment {
    private PublishSubject<Integer> mSizeSubject = PublishSubject.create();
    private Observable<OnClickEvent> mNextClickObservable;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.sample_wizard_step2_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sample_wizard_step2, container, false);
        RadioGroup sizes = (RadioGroup) view.findViewById(R.id.sample_wizard_step2_sizes);
        sizes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mSizeSubject.onNext(checkedId);
            }
        });
        View nextButton = view.findViewById(R.id.next);
        mNextClickObservable = ViewObservable.clicks(nextButton);
        return view;
    }

    public Observable<Size> observeSelectedSize() {
        return Observable.zip(
          mNextClickObservable,
          mSizeSubject,

          new Func2<OnClickEvent, Integer, Size>() {
              @Override
              public Size call(OnClickEvent onClickEvent, Integer radioButtonId) {
                  switch (radioButtonId) {
                      case R.id.pizza_size_big:
                          return Size.BIG;
                      case R.id.pizza_size_super_big:
                          return Size.SUPER_BIG;
                      default:
                          return Size.STANDARD;
                  }
              }
          }
        );
    }
}
