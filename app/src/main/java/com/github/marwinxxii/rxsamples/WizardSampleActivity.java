package com.github.marwinxxii.rxsamples;

import android.app.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import rx.Observable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.android.widget.OnItemClickEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class WizardSampleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_wizard);
    }

    @Override
    protected void onStart() {
        super.onStart();//TODO in onCreate
        Observable.just(new SelectPizzaFragment())
          .flatMap(new Func1<SelectPizzaFragment, Observable<Pizza>>() {
              @Override
              public Observable<Pizza> call(SelectPizzaFragment selectPizzaFragment) {
                  showFragment(selectPizzaFragment, false);
                  return selectPizzaFragment.observeSelectedPizza();
              }
          })
          .flatMap(new Func1<Pizza, Observable<Size>>() {
              @Override
              public Observable<Size> call(Pizza pizza) {
                  SelectSizeFragment selectSizeFragment = new SelectSizeFragment();
                  showFragment(selectSizeFragment, true);
                  return selectSizeFragment.observeSelectedSize();
              }
          }).subscribe(new Action1<Size>() {
            @Override
            public void call(Size size) {
                Toast.makeText(WizardSampleActivity.this, size.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction()
          .replace(android.R.id.content, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        transaction.commit();
        fm.executePendingTransactions();//TODO remove
    }

    public static enum Pizza {
        Pepperoni(R.drawable.pizza_pepperoni),
        Margherita(R.drawable.pizza_margherita),
        Hawaiian(R.drawable.pizza_hawaiian),
        QuattroFormaggi(R.drawable.pizza_quattro);

        public final int drawableId;

        Pizza(int drawableId) {
            this.drawableId = drawableId;
        }
    }

    public static enum Size {
        STANDARD, BIG, SUPER_BIG
    }

    public static class SelectPizzaFragment extends ListFragment {
        @Override
        public void onResume() {
            super.onResume();
            getActivity().setTitle(R.string.sample_wizard_step1_title);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ListView view = (ListView) inflater.inflate(R.layout.sample_wizard_step1, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
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
            setListAdapter(adapter);
        }

        public Observable<Pizza> observeSelectedPizza() {
            return WidgetObservable.itemClicks(getListView()).map(new Func1<OnItemClickEvent, Pizza>() {
                @Override
                public Pizza call(OnItemClickEvent onItemClickEvent) {
                    return (Pizza) getListView().getItemAtPosition(onItemClickEvent.position());
                }
            });//TODO restart observable
        }
    }

    public static class SelectSizeFragment extends Fragment {
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
}
