package com.github.marwinxxii.rxsamples.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public abstract class BaseFragment extends Fragment {
    //behavior subject is needed to ensure that subscriber will receive item even if subscribed after view creation
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
