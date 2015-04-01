package com.github.marwinxxii.rxsamples.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.github.marwinxxii.rxsamples.R;
import rx.Observable;
import rx.android.widget.OnItemClickEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;

public class SelectPizzaFragment extends BaseFragment {
    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.sample_wizard_step1_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView view = (ListView) inflater.inflate(R.layout.sample_wizard_step1, container, false);
        ArrayAdapter<Pizza> adapter = new ArrayAdapter<Pizza>(getActivity(), R.layout.sample_wizard_item, android.R.id.title, Pizza.values()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View result = super.getView(position, convertView, parent);
                Pizza pizza = getItem(position);
                ImageView photo = (ImageView) result.findViewById(R.id.photo);//view holder should be used
                photo.setImageResource(pizza.drawableId);
                return result;
            }
        };
        view.setAdapter(adapter);
        return view;
    }

    public Observable<Pizza> observeSelectedPizza() {
        return observeViewCreated().flatMap(new Func1<Void, Observable<Pizza>>() {
            @Override
            public Observable<Pizza> call(Void aVoid) {
                final ListView lv = (ListView) getView();
                return WidgetObservable.itemClicks(lv)
                  .map(new Func1<OnItemClickEvent, Pizza>() {
                      @Override
                      public Pizza call(OnItemClickEvent onItemClickEvent) {
                          return (Pizza) lv.getItemAtPosition(onItemClickEvent.position());
                      }
                  })
                  .first();
            }
        });
    }
}
