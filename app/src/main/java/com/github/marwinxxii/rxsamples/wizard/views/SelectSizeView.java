package com.github.marwinxxii.rxsamples.wizard.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import com.github.marwinxxii.rxsamples.R;
import com.github.marwinxxii.rxsamples.wizard.Size;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class SelectSizeView extends FrameLayout implements IWizardStepView {
    private PublishSubject<Integer> mSizeSubject = PublishSubject.create();
    private View mNextButton;
    
    public SelectSizeView(Context context) {
        super(context);
        init();
    }

    public SelectSizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectSizeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SelectSizeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }
    
    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.sample_wizard_step2, this, true);
        RadioGroup sizes = (RadioGroup) view.findViewById(R.id.sample_wizard_step2_sizes);
        sizes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mNextButton.setEnabled(true);//could be done with doOnNext on mSizeSubject
                mSizeSubject.onNext(checkedId);
            }
        });
        mNextButton = view.findViewById(R.id.next);
    }

    @Override
    public CharSequence getStepTitle() {
        return getResources().getString(R.string.sample_wizard_step2_title);
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
