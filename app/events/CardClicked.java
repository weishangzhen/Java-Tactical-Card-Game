package events;


import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import structures.GameState;
import structures.SimpleCard;
import structures.basic.Tile;

public class CardClicked implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

        int handPosition = message.get("position").asInt();
        SimpleCard handCard = gameState.getHandCard(handPosition);
        if (handCard == null) {
            return;
        }
        gameState.deActive(out, gameState.activeTarget);
        gameState.sleep(30);
        gameState.activeTarget = handCard;
        //highlight
        handCard.active(out);
        if (gameState.getCurrentPlayer().getMana() < handCard.getManacost()) {
            return;
        }
        handCard.activeTiles = handCard.getTargetTiles(gameState);
        for (Tile tile : handCard.activeTiles) {
            BasicCommands.drawTile(out, tile, 1);
        }

    }

}
