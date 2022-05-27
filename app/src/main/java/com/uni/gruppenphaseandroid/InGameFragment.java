package com.uni.gruppenphaseandroid;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;
import com.uni.gruppenphaseandroid.Cards.CardUI;
import com.uni.gruppenphaseandroid.Cards.Cardtype;
import com.uni.gruppenphaseandroid.communication.Client;
import com.uni.gruppenphaseandroid.communication.dto.LeaveLobbyPayload;
import com.uni.gruppenphaseandroid.communication.dto.Message;
import com.uni.gruppenphaseandroid.communication.dto.MessageType;
import com.uni.gruppenphaseandroid.communication.dto.StartGamePayload;
import com.uni.gruppenphaseandroid.manager.GameManager;
import com.uni.gruppenphaseandroid.playingfield.PlayingField;

public class InGameFragment extends Fragment implements SensorEventListener, CardViewFragment.OnInputListener, SpecialCardDialogFragment.OnCardInputListener {
    private Client websocketClient;
    private final Gson gson = new Gson();
    private SensorManager sensorManager;
    private Sensor sensor;
    private ImageButton btnCardholder;
    private Cardtype selectedCardtype;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_ingame, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PlayingField playingField = new PlayingField(view);
        GameManager.getInstance().setPlayingField(playingField);
        GameManager.getInstance().setWebSocketClient(((MainActivity) getContext()).getWebsocketClient());

        btnCardholder = playingField.getView().findViewById(R.id.btn_cardholderButton);

        view.findViewById(R.id.bttn_leave_game).setOnClickListener(view1 -> {
            websocketClient = ((MainActivity) getContext()).getService().getClient();
            var lobbyId = ((MainActivity) getContext()).getLobbyId();
            var playerId = ((MainActivity) getContext()).getPlayerId();
            var message = new Message();
            message.setType(MessageType.LEAVE_LOBBY);
            var payload = new LeaveLobbyPayload(lobbyId, playerId);

            message.setPayload(gson.toJson(payload));

            websocketClient.send(message);
            NavHostFragment.findNavController(InGameFragment.this)
                    .navigate(R.id.action_InGameFragment_to_FirstFragment);
        });

        view.findViewById(R.id.move_button).setOnClickListener(view13 -> GameManager.getInstance().moveFigureShowcase(1, 1));

        view.findViewById(R.id.move2).setOnClickListener(view14 -> GameManager.getInstance().moveFigureShowcase(3, 3));


        view.findViewById(R.id.start_game_button).setOnClickListener(view12 -> {
            //deactivate start game button
            playingField.getView().findViewById(R.id.start_game_button).setVisibility(View.INVISIBLE);
            //activate cardholder
            btnCardholder.setVisibility(View.VISIBLE);

            websocketClient = ((MainActivity) getContext()).getService().getClient();
            var lobbyId = ((MainActivity) getContext()).getLobbyId();
            var message = new Message();
            message.setType(MessageType.START_GAME);

            var payload = new StartGamePayload(lobbyId, 0, 0);
            message.setPayload(gson.toJson(payload));

            websocketClient.send(message);


        });


        getActivity().findViewById(R.id.btn_cardholderButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardViewFragment cardholder = new CardViewFragment();
                cardholder.show(getFragmentManager(), "cardholder Dialog");
                cardholder.setTargetFragment(InGameFragment.this, 1);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); // Type_Light ist der int Wert 5

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];

        if (x < 40 && !GameManager.getInstance().isHasCheated()) {
            Log.d("sensor_Light", "Light change was registerd");
            GameManager.getInstance().moveWormholes();

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    //method for Dialog Fragment - Card Holder
    @Override
    public void sendInputCardFragment(String input) {
        getActivity().findViewById(R.id.btn_cardholderButton).setBackgroundResource(Integer.parseInt(input));
        setCardViewImage(Integer.parseInt(input));
        selectedCardtype = CardUI.getInstance().idToCardType(Integer.parseInt(input));
    }

    public void setCardViewImage (int imageID){
        btnCardholder.setVisibility(View.VISIBLE);
        if (imageID != -1){
            btnCardholder.setImageResource(imageID);
            checkCard(imageID);
        } else{
            btnCardholder.setImageResource(R.drawable.ic_card_cardholder);
        }
    }

    public void checkCard (int imageID){
        if (checkIfSpecialNumberCardEffect(CardUI.getInstance().idToCardType(imageID))){
            // TODO open new overlay to check conditions for 4+- or 1-7 or 1/11
            //or add new button to check the conditions
            Log.d("check card", "choosen card is a special card, open new dialog window");
            getActivity().findViewById(R.id.fab_specialCards).setVisibility(View.VISIBLE);

            getActivity().findViewById(R.id.fab_specialCards).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SpecialCardDialogFragment specialCardDialog = new SpecialCardDialogFragment(selectedCardtype);
                    specialCardDialog.show(getFragmentManager(), "Special Card Dialog");
                    specialCardDialog.setTargetFragment(InGameFragment.this, 1);
                }
            });
        }
    }

    public boolean checkIfSpecialNumberCardEffect(Cardtype cardtype){
        return cardtype == Cardtype.ONEORELEVEN_START || cardtype == Cardtype.FOUR_PLUSMINUS || cardtype == Cardtype.ONETOSEVEN;

    }

    //methods for selecting special cards
    @Override
    public void sendInputSpecialCardFragment(String input) {

        Log.d("selectedSpecialcArd", input);

        //TODO handle string from special card dialog and sent cardvalue
        // if one to seven set Cardtype.ONE or what so ever


    }

    //todo choose figure -- initialize figure i guess?
    //TODO send card + figure to gamemanger && "make move" button
    //TODO ablagestapel
    //TODO visual note for cheating!

}
