package structures;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import commands.BasicCommands;
import structures.basic.*;
import utils.BasicObjectBuilders;

import java.util.ArrayList;
import java.util.List;

public class BaseSummon extends Unit {
	
	
    @JsonIgnore
    protected int originHealth;
    @JsonIgnore
    protected int health;
    @JsonIgnore
    protected int attack;
    @JsonIgnore
    protected int maxMoveTime;
    @JsonIgnore
    protected int maxAttackTime;
    @JsonIgnore
    protected GameState.Operator belong;
    @JsonIgnore
    protected List<Tile> activeTiles;
    @JsonIgnore
    protected NextAction nextAction;
    @JsonIgnore
    protected String name;

    public BaseSummon() {
    }

    public BaseSummon(int id, UnitAnimationSet animations, ImageCorrection correction) {
        super(id, animations, correction);
    }

    public BaseSummon(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
        super(id, animations, correction, currentTile);
    }

    public BaseSummon(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations, ImageCorrection correction) {
        super(id, animation, position, animations, correction);
    }

    public static boolean isAround(int centerX, int centerY, int x, int y) {
        return Math.abs(centerX - x) <= 1 && Math.abs(centerY - y) <= 1;
    }

    public static boolean isAround(List<Tile> tiles, int x, int y) {
        for (Tile tile : tiles) {
            if (isAround(tile.getTilex(), tile.getTiley(), x, y)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxMoveTime() {
        return maxMoveTime;
    }

    public void setMaxMoveTime(int maxMoveTime) {
        this.maxMoveTime = maxMoveTime;
    }

    public int getMaxAttackTime() {
        return maxAttackTime;
    }

    public void setMaxAttackTime(int maxAttackTime) {
        this.maxAttackTime = maxAttackTime;
    }

    public NextAction getNextAction() {
        return nextAction;
    }

    public void setNextAction(NextAction nextAction) {
        this.nextAction = nextAction;
    }

    public List<Tile> getActiveTiles() {
        return activeTiles;
    }

    public void setActiveTiles(List<Tile> activeTiles) {
        this.activeTiles = activeTiles;
    }

    public Tile getActiveClickTile(int x, int y) {
        if (activeTiles == null) {
            return null;
        }
        for (int i = activeTiles.size() - 1; i >= 0; i--) {
            Tile tile = activeTiles.get(i);
            if (tile.getTilex() == x && tile.getTiley() == y) {
                return tile;
            }
        }
        return null;
    }

    public void initHealthAndAttack(int health, int attack) {
        this.attack = attack;
        originHealth = this.health = health;
    }

    public GameState.Operator getBelong() {
        return belong;
    }

    public void setBelong(GameState.Operator belong) {
        this.belong = belong;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getOriginHealth() {
        return originHealth;
    }

    public void setOriginHealth(int originHealth) {
        this.originHealth = originHealth;
    }


    public boolean canMove(GameState gameState) {
        return true;
    }

    public boolean canAttack(GameState gameState) {
        return true;
    }

    public List<Tile> getMovableTiles(GameState gameState) {
        List<Tile> tiles = new ArrayList<>();
        int tileX = getPosition().getTilex();
        int tileY = getPosition().getTiley();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if ((x == 0 && y == 0) || !isInBoard(tileX + x, tileY + y)
                        || gameState.getSummon(tileX + x, tileY + y) != null) {
                    continue;
                }
                Tile tile = BasicObjectBuilders.loadTile(tileX + x, tileY + y);
                tiles.add(tile);
            }
        }
        if (isInBoard(tileX - 2, tileY)
                && gameState.getSummon(tileX - 2, tileY) == null) {
            Tile tile = BasicObjectBuilders.loadTile(tileX - 2, tileY);
            tiles.add(tile);
        }
        if (isInBoard(tileX + 2, tileY)
                && gameState.getSummon(tileX + 2, tileY) == null) {
            Tile tile = BasicObjectBuilders.loadTile(tileX + 2, tileY);
            tiles.add(tile);
        }
        if (isInBoard(tileX, tileY - 2)
                && gameState.getSummon(tileX, tileY - 2) == null) {
            Tile tile = BasicObjectBuilders.loadTile(tileX, tileY - 2);
            tiles.add(tile);
        }
        if (isInBoard(tileX, tileY + 2)
                && gameState.getSummon(tileX, tileY + 2) == null) {
            Tile tile = BasicObjectBuilders.loadTile(tileX, tileY + 2);
            tiles.add(tile);
        }
        return tiles;
    }

    public List<Summon> getAttackTargets(GameState gameState, boolean moved, List<Tile> moveTiles) {
        List<Summon> summonList = new ArrayList<>();
        List<Summon> hostilityUnits;
        if (belong == GameState.Operator.PLAYER1) {
            hostilityUnits = gameState.player2Units;
        } else {
            hostilityUnits = gameState.player1Units;
        }

        if (moved) {
            for (Summon hostilityUnit : hostilityUnits) {
                if (isAround(getPosition().getTilex(), getPosition().getTiley()
                        , hostilityUnit.getPosition().getTilex()
                        , hostilityUnit.getPosition().getTiley())) {
                    summonList.add(hostilityUnit);
                }
            }
        } else {
            for (Summon hostilityUnit : hostilityUnits) {
                if (isAround(getPosition().getTilex(), getPosition().getTiley()
                        , hostilityUnit.getPosition().getTilex()
                        , hostilityUnit.getPosition().getTiley())
                        || isAround(moveTiles, hostilityUnit.getPosition().getTilex()
                        , hostilityUnit.getPosition().getTiley())) {
                    summonList.add(hostilityUnit);
                }
            }
        }
        return summonList;
    }

    public boolean isInBoard(int x, int y) {
        return x >= 0 && x <= 8 && y >= 0 && y <= 4;
    }

    public void deActive(ActorRef out) {
        if (activeTiles == null) {
            return;
        }
        for (Tile activeTile : activeTiles) {
            BasicCommands.drawTile(out, activeTile, 0);
        }
        activeTiles = null;
    }

    public void appendActiveTile(Tile activeTile) {
        if (this.activeTiles == null) {
            this.activeTiles = new ArrayList<>();
        }
        this.activeTiles.add(activeTile);
    }
}
