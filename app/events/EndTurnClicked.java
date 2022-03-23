package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import structures.GameState;


public class EndTurnClicked implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        if (gameState.current == GameState.Operator.PLAYER1) {
            gameState.nextTurn(out);
            gameState.deActive(out, gameState.activeTarget);
            gameState.activeTarget = null;
        }
    }

}
