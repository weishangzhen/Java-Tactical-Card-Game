package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;

public interface EventProcessor {

	public void processEvent(ActorRef out, GameState gameState, JsonNode message);
	
}
