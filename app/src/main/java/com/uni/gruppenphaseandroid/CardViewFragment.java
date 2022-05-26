package com.uni.gruppenphaseandroid;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.uni.gruppenphaseandroid.Cards.Card;
import com.uni.gruppenphaseandroid.Cards.CardAdapter;
import com.uni.gruppenphaseandroid.Cards.CardUI;
import com.uni.gruppenphaseandroid.manager.GameManager;

import java.util.EventListener;
import java.util.LinkedList;
import java.util.Objects;


public class CardViewFragment extends DialogFragment implements EventListener, SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView textView;
    private Button chooseCard;
    private String clickedCard;

    public interface OnInputListener{
        void sendInput (String input);
    }

    public OnInputListener mOnInputListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_view, container, false);
        chooseCard = view.findViewById(R.id.btn_playCard);
        //set card default
        clickedCard = "-1";

        //set up for recyclerview
        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewCard);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        CardAdapter cardAdapter = new CardAdapter(new CardAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int card) {
                if(GameManager.getInstance().isItMyTurn() == true) {
                    chooseCard.setVisibility(View.VISIBLE);
                    clickedCard = Integer.toString(card);
                }
            }
        });
        recyclerView.setAdapter(cardAdapter);
        recyclerView.scrollToPosition(((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager()))
                .findFirstCompletelyVisibleItemPosition());

        //return to board button
        view.findViewById(R.id.btn_returnToGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });


        //select card and return to board
        view.findViewById(R.id.btn_playCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("card_input", "input:" + clickedCard);
                //capture input
                if (!clickedCard.equals("")) {
                    mOnInputListener.sendInput(clickedCard);
                    getDialog().dismiss();
                }
            }
        });
        return view;
    }


    //on create --> creats seonsorListener
    //creats "hand" --> where cards will be stored --> dunno if it's the smartes way
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        //initialization of sensor
        try {
            sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }catch (NullPointerException e){
            Log.d("Sensor error tilt", e.getMessage());
        };

    }

    @Override
    public void onAttach(@NonNull Context context) {            //Methode für DialogFragement communication
        super.onAttach(context);
        try{
            mOnInputListener = (OnInputListener) getTargetFragment();
        }catch (ClassCastException e){

            Log.e("error", "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    /**
     * Mehtoden die notwendig für den Sensor sind
     */
    //if in CardViewFragment --> listen, otherwise sensor on pause
    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }


    //What happens if sensor change detected
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        textView = getView().findViewById(R.id.tv_cheater);
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];

        if (Math.abs(x) > Math.abs(y)) {
            if (x < 0) { //tilt to right
                //TODO Manipulate move -1
                //TODO check cheating

                textView.setText("Cheater Cheater -1");
                textView.setVisibility(View.VISIBLE);



                //shows a textView that is gone after 5 seconds
                new CountDownTimer(3000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        textView.setVisibility(View.INVISIBLE); //(or GONE)
                    }
                }.start();

            } else {
                if (x > 0) { //tilt to left
                    //TODO Manipulate move +1
                    textView.setText("Cheater Cheater + 1");
                    textView.setVisibility(View.VISIBLE);

                    //shows a textView that is gone after 5 seconds
                    new CountDownTimer(3000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            textView.setVisibility(View.INVISIBLE); //(or GONE)
                        }
                    }.start();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    
        
}