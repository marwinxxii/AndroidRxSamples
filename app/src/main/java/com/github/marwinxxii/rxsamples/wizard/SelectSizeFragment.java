package com.github.marwinxxii.rxsamples.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import com.github.marwinxxii.rxsamples.R;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SelectSizeFragment extends Fragment {
    private PublishSubject<Integer> mSizeSubject = PublishSubject.create();
    private View mNextButton;

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
                mNextButton.setEnabled(true);//could be done with doOnNext on mSizeSubject
                mSizeSubject.onNext(checkedId);
            }
        });
        mNextButton = view.findViewById(R.id.next);
        return view;
    }

    public Observable<Size> observeSelectedSize() {
        //note that if check once and click twice, after second click new item WON'T me emitted
        return mSizeSubject.sample(ViewObservable.clicks(mNextButton))
          .map(new Func1<Integer, Size>() {
              @Override
              public Size call(Integer checkedId) {
                  return parseCheckedSize(checkedId);
              }
          });
    }

    private static Size parseCheckedSize(int checkedId) {
        switch (checkedId) {
            case R.id.pizza_size_big:
                return Size.BIG;
            case R.id.pizza_size_super_big:
                return Size.SUPER_BIG;
            default:
                return Size.STANDARD;
        }
    }
}
