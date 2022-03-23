package events;


import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import structures.GameState;
import structures.Summon;

import java.util.List;

public class UnitStopped implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

        int unitid = message.get("id").asInt();
        int tilex = message.get("tilex").asInt();
        int tiley = message.get("tiley").asInt();

        Summon summon = (Summon) gameState.getSummon(tilex, tiley);
        if (summon != null && summon.getNextAction() != null) {
            summon.getNextAction().doNext(out, gameState, summon);
        }
        List<Summon> attackTargets = summon.getAttackTargets(gameState, true, null);
        for (Summon target : attackTargets) {
            target.freshProvoke(gameState);
        }
    }

}
