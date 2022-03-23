package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import demo.CommandDemo;
import structures.GameState;
import utils.StaticConfFiles;


public class Initalize implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

        BasicCommands.addPlayer1Notification(out, "Game Start", 2);

        gameState.initPlayer(out);

        BasicCommands.setPlayer1Health(out, gameState.player1);
        BasicCommands.setPlayer2Health(out, gameState.player2);
        BasicCommands.setPlayer1Mana(out, gameState.player1);
        BasicCommands.setPlayer2Mana(out, gameState.player2);

        gameState.drawCard(3, GameState.Operator.PLAYER1, out);

        gameState.drawSummon(out, StaticConfFiles.u_pyromancer, 20, 1, 6, 1,
                GameState.Operator.PLAYER2,"ai1");
        gameState.drawSummon(out, StaticConfFiles.u_pyromancer, 5, 4, 6, 2,
                GameState.Operator.PLAYER2,"ai2");
        gameState.drawSummon(out, StaticConfFiles.u_pyromancer, 15, 2, 6, 3,
                GameState.Operator.PLAYER2,"ai3");
        gameState.drawSummon(out, StaticConfFiles.u_pyromancer, 10, 3, 7, 1,
                GameState.Operator.PLAYER2,"ai4");
        gameState.drawSummon(out, StaticConfFiles.u_pyromancer, 12, 3, 7, 3,
                GameState.Operator.PLAYER2,"ai5");
        gameState.drawSummon(out, StaticConfFiles.u_ironcliff_guardian, 8, 3, 8, 1,
                GameState.Operator.PLAYER2,"Ironcliff Guardian");
        gameState.drawSummon(out, StaticConfFiles.u_pyromancer, 6, 3, 8, 2,
                GameState.Operator.PLAYER2,"ai7");
        gameState.drawSummon(out, StaticConfFiles.u_pyromancer, 6, 5, 8, 3,
                GameState.Operator.PLAYER2,"ai8");
    }

}


