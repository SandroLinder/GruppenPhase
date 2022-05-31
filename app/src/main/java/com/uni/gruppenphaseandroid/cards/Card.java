package com.uni.gruppenphaseandroid.cards;

import com.uni.gruppenphaseandroid.manager.GameManager;
import com.uni.gruppenphaseandroid.playingfield.Figure;
import com.uni.gruppenphaseandroid.playingfield.PlayingField;

public class Card {
    private Cardtype cardtype;

    public Card(Cardtype cardtype) {
        this.cardtype = cardtype;
    }

    public Cardtype getCardtype() {
        return cardtype;
    }

    private static final String INVALID_ARGUMENTS= "Invalid Combination of values";

    public void playCard(Figure myFigure, int effect, Figure targetFigure) {
        if(myFigure==null){
            throw new IllegalArgumentException("myFigure cannot be null");
        }

        if(getCardtype()==Cardtype.EQUAL){
            //Gets last played Card
            playEqualCard(myFigure, effect, targetFigure);
        }else if(effect==-1 && targetFigure==null){
            //Cards with only one Effect
            playNonEffectCard(myFigure);
        }else if(effect>=0 && effect<=13 && targetFigure==null){
            //Cards with multiple effects
            playEffectCard(myFigure, effect);
        }else if(effect==-1){
            //Switchcard
            playSwitchCard(myFigure, targetFigure);
        }else{
            throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }

    private void playEqualCard(Figure myFigure, int effect, Figure targetFigure){
        Cardtype newCardtype = GameManager.getInstance().getLastTurn().getCardtype();
        if(newCardtype==Cardtype.EQUAL){
            throw new IllegalArgumentException("You cannot activate an EQUAL Card if the last activated Card was an EQUAL Card");
        }else{
            Card newCard = new Card(newCardtype);
            newCard.playCard(myFigure, effect, targetFigure);
        }
    }

    private void playNonEffectCard(Figure myFigure) {
        PlayingField playingField = GameManager.getInstance().getPlayingField();
        switch (getCardtype()){
            case MAGNET:
                playingField.moveToNextFigure(myFigure);
                break;

            case TWO:
            case THREE:
            case FIVE:
            case SIX:
            case EIGTH:
            case NINE:
            case TEN:
            case TWELVE:
                playingField.move(myFigure, getCardtype().getValue());
                break;

            default:throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }

    private void playEffectCard(Figure myFigure, int effect) {
        PlayingField playingField = GameManager.getInstance().getPlayingField();
        int value;
        switch (getCardtype()) {
            case FOUR_PLUSMINUS:
                if (effect == 1){
                    value = 4;
                }else{
                    value = -4;
                }
                playingField.move(myFigure, value);
                break;

            case ONETOSEVEN:
                playingField.move(myFigure, effect);
                break;

            case ONEORELEVEN_START:
                if (effect == 0){
                    playingField.moveToStart(myFigure);
                    break;
                }
                if (effect == 1){
                    value = 1;
                } else {
                    value = 11;
                }
                playingField.move(myFigure, value);
                break;

            case THIRTEEN_START:
                if (effect == 0){
                    playingField.moveToStart(myFigure);
                } else {
                    playingField.move(myFigure, 13);
                }
                break;

            default:
                throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }

    private void playSwitchCard(Figure figure1, Figure figure2){
        PlayingField playingField = GameManager.getInstance().getPlayingField();
        if (getCardtype().equals(Cardtype.SWITCH)) {
            playingField.switchPositions(figure1, figure2);
        } else {
            throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }

    //////////CHECKS IF MOVE IS POSSIBLE//////////

    public boolean checkIfCardIsPlayable(Figure myFigure, int effect, Figure targetFigure){
        if(myFigure==null){
            throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
        if(getCardtype()==Cardtype.EQUAL){
            //Cards with Cardtype EQUAL
            return checkEqualCard(myFigure, effect, targetFigure);
        }else if(effect==-1 && targetFigure==null){
            //Cards with only one Effect
            return checkNonEffectCard(myFigure);
        }else if(effect>=0 && effect<=13 && targetFigure==null){
            //Cards with multiple effects
            return checkEffectCard(myFigure, effect);
        }else if(effect==-1){
            //Switchcard
            return checkSwitchCard(myFigure, targetFigure);
        }else{
            throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }

    private boolean checkEqualCard(Figure myFigure, int effect, Figure targetFigure){
        Cardtype newCardtype = GameManager.getInstance().getLastTurn().getCardtype();
        if(newCardtype==Cardtype.EQUAL){
            return false;
        }else{
            Card newCard = new Card(newCardtype);
            return newCard.checkIfCardIsPlayable(myFigure, effect, targetFigure);
        }
    }

    private boolean checkNonEffectCard(Figure figure){
        switch (getCardtype()){
            case MAGNET:
                return figure.checkIfAnotherFigureOnPlayingfield();

            case TWO:
            case THREE:
            case FIVE:
            case SIX:
            case EIGTH:
            case NINE:
            case TEN:
            case TWELVE:
                return figure.checkMoving(figure, getCardtype().getValue());

            default:
                throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }

    private boolean checkEffectCard(Figure figure, int effect){
        int value;
        switch (getCardtype()) {
            case FOUR_PLUSMINUS:
                if (effect == 1){
                    value = 4;
                }else{
                    value = -4;
                }
                return figure.checkMoving(figure, value);

            case ONETOSEVEN:
                return figure.checkMoving(figure, effect);

            case ONEORELEVEN_START:
                if (effect == 0){
                    return figure.isOnStartingAreaField();
                }
                if (effect == 1){
                    value = 1;
                }else{
                    value = 11;
                }
                return figure.checkMoving(figure, value);

            case THIRTEEN_START:
                if (effect == 0){
                    return figure.isOnStartingAreaField();
                }
                return figure.checkMoving(figure, 13);

            default:
                throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }

    private boolean checkSwitchCard(Figure figure1, Figure figure2){
        if (getCardtype().equals(Cardtype.SWITCH)) {
            return figure1.checkisOnNormalField() && figure2.checkisOnNormalField();
        } else {
            throw new IllegalArgumentException(INVALID_ARGUMENTS);
        }
    }
}
