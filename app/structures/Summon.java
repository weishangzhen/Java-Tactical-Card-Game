package structures;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import commands.BasicCommands;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

public class Summon extends BaseSummon {
    @JsonIgnore
    public TurnData turnData = new TurnData();
    @JsonIgnore
    public boolean isRanged;
    @JsonIgnore
    public boolean canFly;
    @JsonIgnore
    public boolean canProvoke;
    @JsonIgnore
    public Summon provoker;
    public boolean CheckProvoke = false;

    @Override
    public List<Summon> getAttackTargets(GameState gameState, boolean moved, List<Tile> moveTiles) {
        if (provoker != null) {
            return new ArrayList<Summon>() {{
                add(provoker);
            }};
        }
        if (isRanged) {
            return new ArrayList<>(belong ==
                    GameState.Operator.PLAYER2 ? gameState.player1Units : gameState.player2Units);
        }
        return super.getAttackTargets(gameState, moved, moveTiles);
    }

    @Override
    public List<Tile> getMovableTiles(GameState gameState) {
        if (canFly) {
            List<Tile> tiles = new ArrayList<>();
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 5; y++) {
                    if (gameState.getSummon(x, y) == null) {
                        tiles.add(BasicObjectBuilders.loadTile(x, y));
                    }
                }
            }
            return tiles;
        }
        return super.getMovableTiles(gameState);
    }

    public void finishTurn() {
        turnData.clear();
    }

    public int getX() {
        return getPosition().getTilex();
    }

    public int getY() {
        return getPosition().getTiley();
    }

    public Tile getTile() {
        return BasicObjectBuilders.loadTile(getX(), getY());
    }

    @Override
    public boolean canAttack(GameState gameState) {
        return turnData.attackedTimes < maxAttackTime;
    }

    @Override
    public boolean canMove(GameState gameState) {
        return turnData.movedTimes < maxMoveTime && provoker == null;
    }

    
    /**
     * 
     * @author Chuyan Niu 2553833N
     * 
     *
     */
    public void attack(ActorRef out, GameState gameState, Summon hitter) {
    	
    	
        BasicCommands.playUnitAnimation(out, this, UnitAnimationType.attack);
        if (isRanged) {
            gameState.sleep(200);
            EffectAnimation projectile = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_projectiles);
            BasicCommands.playProjectileAnimation(out, projectile, 0, getTile(), hitter.getTile());
        }
        turnData.attackedTimes++;
        if (hitter != null) {
            gameState.sleep(200);
            hitter.onDamage(out, gameState, attack);
            if (hitter.health <= 0 || !hitter.isRanged ) {
                return;
            }
            gameState.sleep(200);
            BasicCommands.playUnitAnimation(out, hitter, UnitAnimationType.hit);
            gameState.sleep(200);
            BasicCommands.playUnitAnimation(out, hitter, UnitAnimationType.attack);
            gameState.sleep(200);
            onDamage(out, gameState, hitter.attack);
            gameState.sleep(1000);
            BasicCommands.playUnitAnimation(out, hitter, UnitAnimationType.idle);
            BasicCommands.playUnitAnimation(out, this, UnitAnimationType.idle);
        }
    }

    /**
     * 
     * @author Dejing Liu 2524537L
     * 
     *
     */
    
    public void onDamage(ActorRef out, GameState gameState, int damage) {
        health -= damage;
        health = Math.max(0, health);
        health = Math.min(originHealth, health);
        BasicCommands.setUnitHealth(out, this, health);
        if (this == gameState.avatar1) {
            gameState.player1.setHealth(health);
            BasicCommands.setPlayer1Health(out, gameState.player1);
            addAttack(out, "Silverguard Knight", 2, gameState.player1Units);
        } else if (this == gameState.avatar2) {
            gameState.player2.setHealth(health);
            BasicCommands.setPlayer2Health(out, gameState.player2);
            addAttack(out, "Silverguard Knight", 2, gameState.player2Units);
        }
        if (health <= 0) {
            BasicCommands.playUnitAnimation(out, this, UnitAnimationType.death);
            gameState.sleep(1000);
            BasicCommands.deleteUnit(out, this);
            if (belong == GameState.Operator.PLAYER1) {
                gameState.player1Units.remove(this);
            } else {
                gameState.player2Units.remove(this);
            }
            if (this == gameState.avatar1) {
                BasicCommands.addPlayer1Notification(out, "Game Lose!", 300);
            } else if (this == gameState.avatar2) {
                BasicCommands.addPlayer1Notification(out, "Game Win!", 300);
            }
        }
    }

    public void addAttack(ActorRef out, String name, int attack, List<Summon> summonList) {
        if (summonList == null || name == null) {
            return;
        }
        summonList.stream().filter(summon -> name.equals(summon.name))
                .forEach(summon -> {
                    summon.setAttack(summon.getAttack() + attack);
                    BasicCommands.setUnitAttack(out, summon, summon.getAttack());
                });
    }

    
    /**
     * 
     * @author 2582961W HanYang Wei
     * 
     *
     */
    public void move(ActorRef out, Tile clickTile) {
        BasicCommands.moveUnitToTile(out, this, clickTile);
        setPositionByTile(clickTile);
        turnData.movedTimes++;
    }

    public void init(String name) {
        maxMoveTime = 1;
        maxAttackTime = 1;
        switch (name) {
            case StaticConfFiles.u_pyromancer:
            case StaticConfFiles.u_fire_spitter:
                isRanged = true;
                return;
            case StaticConfFiles.u_azurite_lion:
                maxAttackTime = 2;
                return;
            case StaticConfFiles.u_silverguard_knight:
            case StaticConfFiles.u_ironcliff_guardian:
            case StaticConfFiles.u_rock_pulveriser:
                canProvoke = true;
                return;
            default:

                return;
        }

    }

    /**
     * 
     * @author 2541221W ShangZhen Wei
     * 
     *
     */
    
    public void freshProvoke(GameState gameState) {
        CheckProvoke = true;
        if (!canProvoke) {
            return;
        }
        List<Summon> attackTargets = getAttackTargets(gameState, true, null);
        for (Summon target : attackTargets) {
            // target.canAttackTarget(this)
            target.provoker = this;
        }
    }

    /**
     * 
     * @author Lu Bai 2460007B 
     * 
     *
     */
    
    public void onSummoned(ActorRef out, GameState gameState) {
        if ("Azure Herald".equals(name)) {
            Summon avatar = gameState.current == GameState.Operator.PLAYER1 ? gameState.avatar1 : gameState.avatar2;
            avatar.onDamage(out, gameState, -3);
        }
        freshProvoke(gameState);

        EffectAnimation ef = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_summon);
        BasicCommands.playEffectAnimation(out, ef, getTile());
    }


    public class TurnData {
        int movedTimes;
        int attackedTimes;

        public void clear() {
            movedTimes = 0;
            attackedTimes = 0;
        }
    }

}
