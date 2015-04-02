package com.github.marwinxxii.rxsamples.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public abstract class BaseFragment extends Fragment {
    private final BehaviorSubject<BaseFragment> mViewCreatedSubject = BehaviorSubject.create();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreatedSubject.onNext(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewCreatedSubject.onCompleted();
    }

    protected <T extends BaseFragment> Observable<T> observeViewCreated() {
        return (Observable<T>) mViewCreatedSubject;
    }
}
