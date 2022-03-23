package structures;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import commands.BasicCommands;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleCard extends Card {
    @JsonIgnore
    public TargetType targetType = TargetType.ADJACENT;
    @JsonIgnore
    public List<Tile> activeTiles;
    @JsonIgnore
    public String summonConfig;
    @JsonIgnore
    int position = -1;

    public SimpleCard() {
    }

    public SimpleCard(int id, String cardname, int manacost,
                      MiniCard miniCard, BigCard bigCard) {
        super(id, cardname, manacost, miniCard, bigCard);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void active(ActorRef out) {
        BasicCommands.drawCard(out, this, position, 1);
    }

    public boolean isSpell() {
        return getBigCard().getAttack() < 0;
    }

    public List<Tile> getTargetTiles(GameState gameState) {
        List<Tile> tiles = new ArrayList<>();
        if (targetType == TargetType.ANYWHERE) {
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 5; y++) {
                    if (gameState.getSummon(x, y) == null) {
                        tiles.add(BasicObjectBuilders.loadTile(x, y));
                    }
                }
            }
        } else if (targetType == TargetType.ADJACENT) {
            Map<String, Tile> data = new HashMap<>();
            List<Summon> friendlyUnits = gameState.getFriendlyUnits();
            for (Summon unit : friendlyUnits) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        int tileX = unit.getX() + x;
                        int tileY = unit.getY() + y;
                        String coord = tileX + "-" + tileY;
                        if ((x == 0 && y == 0) || !isInBoard(tileX, tileY) || data.containsKey(coord)) {
                            continue;
                        }
                        Tile tile = BasicObjectBuilders.loadTile(tileX, tileY);
                        data.put(coord, tile);
                    }
                }
            }

            for (Map.Entry<String, Tile> entry : data.entrySet()) {
                if (gameState.getSummon(entry.getValue().getTilex(), entry.getValue().getTiley()) == null) {
                    tiles.add(entry.getValue());
                }
            }

        } else if (targetType == TargetType.FRIENDLY) {
            for (Summon unit : gameState.getFriendlyUnits()) {
                tiles.add(unit.getTile());
            }
        } else if (targetType == TargetType.FRIENDLY_AVATAR) {
            tiles.add(gameState.getFriendlyAvatar().getTile());
        } else if (targetType == TargetType.ENEMY) {
            for (Summon unit : gameState.getEnemyUnits()) {
                tiles.add(unit.getTile());
            }
        } else if (targetType == TargetType.ENEMY_AVATAR) {
            tiles.add(gameState.getEnemyAvatar().getTile());
        } else if (targetType == TargetType.NON_AVATAR) {
            for (Summon unit : gameState.player1Units) {
                if (unit != gameState.avatar1) {
                    tiles.add(unit.getTile());
                }
            }
            for (Summon unit : gameState.player2Units) {
                if (unit != gameState.avatar2) {
                    tiles.add(unit.getTile());
                }
            }
        }
        return tiles;
    }

    public boolean isInBoard(int x, int y) {
        return x >= 0 && x <= 8 && y >= 0 && y <= 4;
    }

    public void deActive(ActorRef out) {
        if (activeTiles == null) {
            return;
        }
        for (Tile tile : activeTiles) {
            BasicCommands.drawTile(out, tile, 0);
        }
        activeTiles = null;
    }

    public boolean canTarget(int x, int y) {
        if (activeTiles == null) {
            return false;
        }
        for (Tile tile : activeTiles) {
            if (tile.getTilex() == x && tile.getTiley() == y) {
                return true;
            }
        }
        return false;
    }

    public void init() {
        switch (getCardname()) {
            case "Comodo Charger":
                summonConfig = StaticConfFiles.u_comodo_charger;
                targetType = TargetType.ADJACENT;
                return;
            case "Hailstone Golem":
                summonConfig = StaticConfFiles.u_hailstone_golem;
                targetType = TargetType.ADJACENT;
                return;
            case "Pureblade Enforcer":
                //If the enemy player casts a spell, this minion gains +1 attack and +1 health
                summonConfig = StaticConfFiles.u_pureblade_enforcer;
                targetType = TargetType.ADJACENT;
                return;
            case "Azure Herald":
                //When this unit is summoned give your avatar +3 health (maximum 20)
                summonConfig = StaticConfFiles.u_azure_herald;
                targetType = TargetType.ADJACENT;
                return;
            case "Silverguard Knight":
                // Provoke: If an enemy unit can attack and is adjacent to any unit with provoke,
                // then it can only choose to attack the provoke units. Enemy units cannot move when provoked.
                // If your avatar is dealt damage this unit gains +2 attack
                summonConfig = StaticConfFiles.u_silverguard_knight;
                targetType = TargetType.ADJACENT;
                return;
            case "Azurite Lion":
                //Can attack twice per turn
                summonConfig = StaticConfFiles.u_azurite_lion;
                targetType = TargetType.ADJACENT;
                return;
            case "Fire Spitter":
                //Ranged: Can attack any enemy on the board
                summonConfig = StaticConfFiles.u_fire_spitter;
                targetType = TargetType.ADJACENT;
                return;
            case "Ironcliff Guardian":
                //Can be summoned anywhere on the board
                //Provoke: If an enemy unit can attack and is adjacent to any unit with provoke,
                // then it can only choose to attack the provoke units. Enemy units cannot move when provoked.
                summonConfig = StaticConfFiles.u_ironcliff_guardian;
                targetType = TargetType.ANYWHERE;
                return;
            case "Truestrike":
                //Deal 2 damage to an enemy unit
                targetType = TargetType.ENEMY;
                return;
            case "Sundrop Elixir":
                //Add +5 health to a Unit. This cannot take a unit over its starting health value.
                targetType = TargetType.FRIENDLY;
                return;
            case "Planar Scout":
                //Canbe summoned anywhere on the board
                summonConfig = StaticConfFiles.u_planar_scout;
                targetType = TargetType.ANYWHERE;
                return;
            case "Rock Pulveriser":
                //Provoke: If an enemy unit can attack and is adjacent to any unit with provoke,
                //then it can only choose to attack the provoke units. Enemy units cannot move when provoked.
                summonConfig = StaticConfFiles.u_rock_pulveriser;
                targetType = TargetType.ADJACENT;
                return;
            case "Pyromancer":
                //Ranged: Can attack any enemy on the board
                summonConfig = StaticConfFiles.u_pyromancer;
                targetType = TargetType.ADJACENT;
                return;
            case "Bloodshard Golem":
                summonConfig = StaticConfFiles.u_bloodshard_golem;
                targetType = TargetType.ADJACENT;
                return;
            case "Blaze Hound":
                //When this unit is summoned, both players draw a card
                summonConfig = StaticConfFiles.u_blaze_hound;
                targetType = TargetType.ADJACENT;
                return;
            case "Windshrike":
                //Flying: Can move anywhere on the board
                //When this unit dies, its owner draws a card
                summonConfig = StaticConfFiles.u_windshrike;
                targetType = TargetType.ADJACENT;
                return;
            case "Serpenti":
                //Can attack twice per turn
                summonConfig = StaticConfFiles.u_serpenti;
                targetType = TargetType.ADJACENT;
                return;
            case "Staff of Y'Kir'":
                //Add +2 attack to your avatar
                targetType = TargetType.FRIENDLY_AVATAR;
                return;
            case "Entropic Decay":
                //Reduce a non-avatar unit to 0 health
                targetType = TargetType.NON_AVATAR;
                return;
        }
    }

    public void playTarget(ActorRef out, GameState gameState, Tile tile) {
        if (summonConfig != null) {
            gameState.drawSummon(out, summonConfig, getBigCard().getHealth()
                    , getBigCard().getAttack(), tile.getTilex(), tile.getTiley()
                    , gameState.current, getCardname());
            gameState.removeCard(this, out);
            gameState.reduceMana(out, getManacost());
            return;
        }
        //spell
        if ("Truestrike".equals(getCardname())) {
            EffectAnimation ef = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_inmolation);
            BasicCommands.playEffectAnimation(out, ef, tile);
            Summon summon = gameState.getSummon(tile.getTilex(), tile.getTiley());
            if (summon != null) {
                summon.onDamage(out, gameState, 2);
            }
            gameState.removeCard(this, out);
            gameState.reduceMana(out, getManacost());
            return;
        }
        if ("Sundrop Elixir".equals(getCardname())) {
            EffectAnimation ef = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_buff);
            BasicCommands.playEffectAnimation(out, ef, tile);
            Summon summon = gameState.getSummon(tile.getTilex(), tile.getTiley());
            if (summon != null) {
                summon.onDamage(out, gameState, -5);
            }
            gameState.removeCard(this, out);
            gameState.reduceMana(out, getManacost());
            return;
        }
    }

    public enum TargetType {
        ANYWHERE,
        ADJACENT,
        FRIENDLY,
        FRIENDLY_AVATAR,
        ENEMY,
        ENEMY_AVATAR,
        NON_AVATAR
    }
}
