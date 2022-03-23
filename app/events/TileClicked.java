package events;


import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import structures.BaseSummon;
import structures.GameState;
import structures.SimpleCard;
import structures.Summon;
import structures.basic.Tile;
import utils.BasicObjectBuilders;

import java.util.List;

public class TileClicked implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

        int tilex = message.get("tilex").asInt();
        int tiley = message.get("tiley").asInt();

        if (gameState.activeTarget == null) {
            Summon summon = gameState.getSummon(tilex, tiley);
            if (summon == null || !summon.canAttack(gameState)
                    || summon.getBelong() != GameState.Operator.PLAYER1) {
                return;
            }
            List<Tile> movableTiles = null;
            if (summon.canMove(gameState)) {
                movableTiles = summon.getMovableTiles(gameState);
                for (Tile movableTile : movableTiles) {
                    BasicCommands.drawTile(out, movableTile, 1);
                    movableTile.mode = 1;
                }
                summon.setActiveTiles(movableTiles);
            }
            List<Summon> attackTargets = summon.getAttackTargets(gameState, !summon.canMove(gameState), movableTiles);
            for (BaseSummon attackTarget : attackTargets) {
                Tile tile = BasicObjectBuilders.loadTile(
                        attackTarget.getPosition().getTilex(), attackTarget.getPosition().getTiley()
                );
                BasicCommands.drawTile(out, tile, 2);
                tile.mode = 2;
                summon.appendActiveTile(tile);
            }
            gameState.activeTarget = summon;
            return;
        }

        if (gameState.activeTarget instanceof SimpleCard) {
            SimpleCard card = (SimpleCard) gameState.activeTarget;
            if (card.canTarget(tilex, tiley)) {
                card.playTarget(out, gameState, BasicObjectBuilders.loadTile(tilex, tiley));
            }
            gameState.deActive(out, gameState.activeTarget);
            gameState.activeTarget = null;
            return;
        }

        Summon summon = (Summon) gameState.activeTarget;
        Tile clickTile = summon.getActiveClickTile(tilex, tiley);
        gameState.deActive(out, gameState.activeTarget);
        gameState.activeTarget = null;
        if (clickTile == null) {
            return;
        }

        if (clickTile.mode == 1) {
            summon.move(out, clickTile);
        } else {
            Summon hitter = (Summon) gameState.getSummon(tilex, tiley);
            if (Summon.isAround(summon.getPosition().getTilex()
                    , summon.getPosition().getTiley(), tilex, tiley) || summon.isRanged) {
                summon.attack(out, gameState, hitter);
            } else {
                List<Tile> movableTiles = summon.getMovableTiles(gameState);
                for (Tile movableTile : movableTiles) {
                    if (Summon.isAround(hitter.getPosition().getTilex(), hitter.getPosition().getTiley()
                            , movableTile.getTilex(), movableTile.getTiley())) {
                        summon.move(out, movableTile);
                        summon.setNextAction((out1, gameState1, target) -> {
                            target.setNextAction(null);
                            summon.attack(out1, gameState, hitter);
                        });
                        return;
                    }
                }
            }
        }

    }


}
