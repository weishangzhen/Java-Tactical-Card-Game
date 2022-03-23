package structures;

import akka.actor.ActorRef;

public interface NextAction {
    void doNext(ActorRef out, GameState gameState, Summon target);
}
