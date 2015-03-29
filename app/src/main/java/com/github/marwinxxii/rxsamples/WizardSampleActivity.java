package com.github.marwinxxii.rxsamples;

import android.app.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
          .flatMap(new Func1<Pizza, Observable<Pair<Pizza, Size>>>() {
              @Override
              public Observable<Pair<Pizza, Size>> call(final Pizza pizza) {
                  SelectSizeFragment selectSizeFragment = new SelectSizeFragment();
                  showFragment(selectSizeFragment, true);
                  return selectSizeFragment.observeSelectedSize().map(new Func1<Size, Pair<Pizza, Size>>() {
                      @Override
                      public Pair<Pizza, Size> call(Size size) {
                          return new Pair<>(pizza, size);
                      }
                  });
              }
          })
          .flatMap(new Func1<Pair<Pizza, Size>, Observable<PizzaOrder>>() {
              @Override
              public Observable<PizzaOrder> call(Pair<Pizza, Size> params) {
                  SubmitOrderFragment submitFragment = SubmitOrderFragment.create(params.first, params.second);
                  showFragment(submitFragment, true);
                  return submitFragment.observeOrder();
              }
          }).subscribe(new Action1<PizzaOrder>() {
            @Override
            public void call(PizzaOrder order) {
                String text = String.format("Deliver %1$s %2$s pizza to %3$s", order.getSize(), order.getType(), order.getPhone());
                Toast.makeText(WizardSampleActivity.this, text, Toast.LENGTH_SHORT).show();
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

    public static class SubmitOrderFragment extends Fragment {
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

    public static class PizzaOrder {
        private Pizza mType;
        private Size mSize;
        private String mPhone;

        public PizzaOrder(Pizza type, Size size, String phone) {
            mType = type;
            mSize = size;
            mPhone = phone;
        }

        public Pizza getType() {
            return mType;
        }

        public Size getSize() {
            return mSize;
        }

        public String getPhone() {
            return mPhone;
        }
    }
}
