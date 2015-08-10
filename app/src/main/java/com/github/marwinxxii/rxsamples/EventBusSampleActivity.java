package com.github.marwinxxii.rxsamples;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

public class EventBusSampleActivity extends Activity {
    private static final EventBus<Object> sAppBus = EventBus.createWithLatest();//singleton bus
    private static final MyLocationService sLocationService = new MyLocationService();//singleton background service

    private final CompositeSubscription mSubscriptions = new CompositeSubscription();
    private Subscription mLocationChangesSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_event_bus);
        initSimpleSample();
        initGenericBusSample();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSubscriptions.unsubscribe();
        //ideally we need to stop sLocationService if it's still running
        //in this case it's not critical, anyway it will be stopped
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void initSimpleSample() {
        Button simple = (Button) findViewById(R.id.sample_event_bus_simple);
        simple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSimpleBusSample();
            }
        });
    }

    public void runSimpleBusSample() {
        //create bus
        PublishSubject<String> stringBus = PublishSubject.create();
        //listen for events
        mSubscriptions.add(stringBus.subscribe(new Action1<String>() {
            @Override
            public void call(String event) {
                Log.d("EventBusSample", "Event happened: " + event);
            }
        }));

        mSubscriptions.add(stringBus.observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Action1<String>() {
              @Override
              public void call(String event) {
                  showToast("Simple event bus: " + event);
              }
          }));

        //post events
        stringBus.onNext("event1");
        stringBus.onNext("event2");
    }

    public void initGenericBusSample() {
        final Button runService = (Button) findViewById(R.id.run_location_service);
        runService.setText(sLocationService.isRunning() ? R.string.stop_location_service : R.string.run_location_service);
        runService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sLocationService.isRunning()) {
                    sLocationService.stop();
                    runService.setText(R.string.run_location_service);
                    showToast("Location service stopped");
                } else {
                    sLocationService.run();
                    runService.setText(R.string.stop_location_service);
                }
            }
        });

        final TextView lastKnownLocation = (TextView) findViewById(R.id.last_known_location);
        final Button listenLocationChanges = (Button) findViewById(R.id.listen_location_changes);

        listenLocationChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationChangesSubscription != null) {
                    mLocationChangesSubscription.unsubscribe();
                    mLocationChangesSubscription = null;
                    listenLocationChanges.setText(R.string.listen_location_changes);
                } else {
                    listenLocationChanges(lastKnownLocation);
                    listenLocationChanges.setText(R.string.listen_location_changes_stop);
                }
            }
        });
    }

    private void listenLocationChanges(final TextView lastKnownLocation) {
        showToast("Listening for location changes");
        mLocationChangesSubscription = sAppBus.observeEvents(LocationChangedEvent.class)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Action1<LocationChangedEvent>() {
              @Override
              public void call(LocationChangedEvent event) {
                  lastKnownLocation.setVisibility(View.VISIBLE);
                  Location location = event.getLocation();
                  lastKnownLocation.setText(getString(R.string.last_known_location, location.toString()
                    + "\nupdated: " + DateFormat.getTimeInstance().format(location.getTime())));
              }
          });
        mSubscriptions.add(mLocationChangesSubscription);
    }

    public static <E1, E2> Observable<Object> observeEvents(EventBus<Object> bus, Class<E1> class1, Class<E2> class2) {
        return Observable.merge(bus.observeEvents(class1), bus.observeEvents(class2));
    }

    public static class EventBus<T> {
        private final Subject<T, T> subject;

        public EventBus() {
            this(PublishSubject.<T>create());
        }

        public EventBus(Subject<T, T> subject) {
            this.subject = subject;
        }

        public <E extends T> void post(E event) {
            subject.onNext(event);
        }

        public Observable<T> observe() {
            return subject;
        }

        public <E extends T> Observable<E> observeEvents(Class<E> eventClass) {
            return subject.ofType(eventClass);//pass only events of specified type, filter all other
        }

        public static <T> EventBus<T> createSimple() {
            return new EventBus<>();
        }

        public static <T> EventBus<T> createRepeating(int numberOfEventsToRepeat) {
            return new EventBus<>(ReplaySubject.<T>createWithSize(numberOfEventsToRepeat));
        }

        public static <T> EventBus<T> createWithLatest() {
            return new EventBus<>(BehaviorSubject.<T>create());
        }
    }

    public static class MyLocationService {
        private Subscription mSubscription;

        public void run() {
            //for simplicity I simulated background location service using observables
            if (mSubscription == null) {
                mSubscription = observeLocationChanges().subscribe(new Action1<LocationChangedEvent>() {
                    @Override
                    public void call(LocationChangedEvent locationChangedEvent) {
                        sAppBus.post(locationChangedEvent);
                    }
                });
            }
        }

        public boolean isRunning() {
            return mSubscription != null;
        }

        //if service was not stopped manually it should be stopped somewhere when it's not needed
        public void stop() {
            if (mSubscription != null) {
                mSubscription.unsubscribe();
                mSubscription = null;
            }
        }

        private static Observable<LocationChangedEvent> observeLocationChanges() {
            final Location[] locations = new Location[]{
              createLocation(59.9500, 30.3000),//Saint-Petersburg
              createLocation(55.7500, 37.6167),//Moscow
              createLocation(52.5167, 13.3833),//Berlin
              createLocation(48.8567, 2.3508),//Paris
              createLocation(51.5072, 0.1275),//London
              createLocation(40.7127, -74.0059)//New York
            };
            //generate new location every second, but first is generated after 3
            return Observable.timer(3L, 1L, TimeUnit.SECONDS).map(new Func1<Long, LocationChangedEvent>() {
                @Override
                public LocationChangedEvent call(Long counter) {
                    Location location = new Location(locations[((int) (counter % locations.length))]);
                    location.setTime(System.currentTimeMillis());//set time when location has "changed"
                    return new LocationChangedEvent(location);
                }
            });
        }

        private static Location createLocation(double latitude, double longitude) {
            Location location = new Location("HARDCODED_PROVIDER");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            return location;
        }
    }

    public static class LocationChangedEvent {
        private final Location mLocation;

        public LocationChangedEvent(Location location) {
            mLocation = location;
        }

        public Location getLocation() {
            return mLocation;
        }
    }
}
