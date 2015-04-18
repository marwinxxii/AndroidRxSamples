package com.github.marwinxxii.rxsamples.wizard.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import com.github.marwinxxii.rxsamples.R;
import com.github.marwinxxii.rxsamples.wizard.Pizza;
import rx.Observable;
import rx.android.widget.OnItemClickEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;

public class SelectPizzaView extends FrameLayout implements IWizardStepView {
    private ListView mListView;

    public SelectPizzaView(Context context) {
        super(context);
        init();
    }

    public SelectPizzaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectPizzaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SelectPizzaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        Context context = getContext();
        mListView = (ListView) LayoutInflater.from(context).inflate(R.layout.sample_wizard_step1, this, true)
          .findViewById(android.R.id.list);
        ArrayAdapter<Pizza> adapter = new ArrayAdapter<Pizza>(context, R.layout.sample_wizard_item, android.R.id.title, Pizza.values()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View result = super.getView(position, convertView, parent);
                Pizza pizza = getItem(position);
                ImageView photo = (ImageView) result.findViewById(R.id.photo);//view holder should be used
                photo.setImageResource(pizza.drawableId);
                return result;
            }
        };
        mListView.setAdapter(adapter);
    }

    @Override
    public CharSequence getStepTitle() {
        return getResources().getString(R.string.sample_wizard_step1_title);
    }

    @Override
    public void reset() {
    }

    public Observable<Pizza> observeSelectedPizza() {
        return WidgetObservable.itemClicks(mListView)
          .map(new Func1<OnItemClickEvent, Pizza>() {
              @Override
              public Pizza call(OnItemClickEvent onItemClickEvent) {
                  return (Pizza) mListView.getItemAtPosition(onItemClickEvent.position());
              }
          });
    }
}
