package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.*;

public class GameState {
    public List<Summon> player1Units;
    public List<Summon> player2Units;
    public Summon avatar1;
    public Summon avatar2;
    public Player player1;
    public Player player2;
    public Object activeTarget;
    public Operator current;
    Iterator<String> deck1;
    Iterator<String> deck2;
    SimpleCard[] handCards1;
    SimpleCard[] handCards2;
    private int currentId;
    private int turns;

    public boolean checkreduceMana = false;
    public boolean checknextTurn = false;

    public GameState() {
        player1Units = new ArrayList<>();
        player2Units = new ArrayList<>();
        currentId = 0;
        List<String> cards1 = Arrays.asList(
                StaticConfFiles.c_azure_herald,
                StaticConfFiles.c_azurite_lion,
                StaticConfFiles.c_comodo_charger,
                StaticConfFiles.c_fire_spitter,
                StaticConfFiles.c_hailstone_golem,
                StaticConfFiles.c_ironcliff_guardian,
                StaticConfFiles.c_pureblade_enforcer,
                StaticConfFiles.c_silverguard_knight,
                StaticConfFiles.c_sundrop_elixir,
                StaticConfFiles.c_truestrike,
                StaticConfFiles.c_azure_herald,
                StaticConfFiles.c_azurite_lion,
                StaticConfFiles.c_comodo_charger,
                StaticConfFiles.c_fire_spitter,
                StaticConfFiles.c_hailstone_golem,
                StaticConfFiles.c_ironcliff_guardian,
                StaticConfFiles.c_pureblade_enforcer,
                StaticConfFiles.c_silverguard_knight,
                StaticConfFiles.c_sundrop_elixir,
                StaticConfFiles.c_truestrike);
        Collections.shuffle(cards1);
        List<String> cards2 = Arrays.asList(
                StaticConfFiles.c_blaze_hound,
                StaticConfFiles.c_bloodshard_golem,
                StaticConfFiles.c_entropic_decay,
                StaticConfFiles.c_hailstone_golem,
                StaticConfFiles.c_planar_scout,
                StaticConfFiles.c_pyromancer,
                StaticConfFiles.c_serpenti,
                StaticConfFiles.c_rock_pulveriser,
                StaticConfFiles.c_staff_of_ykir,
                StaticConfFiles.c_windshrike,
                StaticConfFiles.c_blaze_hound,
                StaticConfFiles.c_bloodshard_golem,
                StaticConfFiles.c_entropic_decay,
                StaticConfFiles.c_hailstone_golem,
                StaticConfFiles.c_planar_scout,
                StaticConfFiles.c_pyromancer,
                StaticConfFiles.c_serpenti,
                StaticConfFiles.c_rock_pulveriser,
                StaticConfFiles.c_staff_of_ykir,
                StaticConfFiles.c_windshrike);
        Collections.shuffle(cards2);
        deck1 = cards1.iterator();
        deck2 = cards2.iterator();
        handCards1 = new SimpleCard[5];
        handCards2 = new SimpleCard[5];
    }


    
    public void initPlayer(ActorRef out) {
        for (int x = 0; x <= 8; x++) {
            for (int y = 0; y <= 4; y++) {
                BasicCommands.drawTile(out, BasicObjectBuilders.loadTile(x, y), 0);
            }
        }

        avatar1 = drawSummon(out, StaticConfFiles.humanAvatar, 20, 2, 1, 2,
                Operator.PLAYER1, "avatar1");
        avatar2 = drawSummon(out, StaticConfFiles.aiAvatar, 20, 2, 7, 2,
                Operator.PLAYER2, "avatar2");
        player1 = new Player(20, 2);
        player2 = new Player(20, 2);
        current = Operator.PLAYER1;
        turns = 1;
    }

    public Summon drawSummon(ActorRef out, String config, int health, int attack,
                             int x, int y, Operator operator, String name) {
        Summon summon = (Summon) BasicObjectBuilders.loadUnit(config, getNextId(), Summon.class);
        if (summon == null) {
            return null;
        }
        summon.setName(name);
        summon.init(config);
        summon.initHealthAndAttack(health, attack);
        Tile tile = BasicObjectBuilders.loadTile(x, y);
        summon.setPositionByTile(tile);
        BasicCommands.drawUnit(out, summon, tile);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BasicCommands.setUnitHealth(out, summon, health);
        BasicCommands.setUnitAttack(out, summon, attack);
        addUnit(operator, summon);

        summon.onSummoned(out, this);
        return summon;
    }

    public void removeCard(SimpleCard card, ActorRef out) {
        SimpleCard[] handCards = current == Operator.PLAYER1 ? handCards1 : handCards2;
        BasicCommands.deleteCard(out, card.position);
        handCards[card.position - 1] = null;
    }

    public void addUnit(Operator operator, Summon summon) {
        if (operator == Operator.PLAYER1) {
            player1Units.add(summon);
        } else {
            player2Units.add(summon);
        }
        summon.setBelong(operator);
    }


    public int getNextId() {
        return ++currentId;
    }

    public void drawCard(int nums, Operator operator, ActorRef out) {
        Iterator<String> cardIt = operator == Operator.PLAYER1 ? deck1 : deck2;
        SimpleCard[] handCards = operator == Operator.PLAYER1 ? handCards1 : handCards2;
        int slotIndex;
        while (cardIt.hasNext() && nums > 0 && (slotIndex = emptyHandCardIndex(handCards)) >= 0) {
            String next = cardIt.next();
            nums--;
            SimpleCard card = (SimpleCard) BasicObjectBuilders.loadCard(next, getNextId(), SimpleCard.class);
            card.init();
            BasicCommands.drawCard(out, card, slotIndex + 1, 0);
            handCards[slotIndex] = card;
            card.setPosition(slotIndex + 1);
        }
    }

    public SimpleCard getHandCard(int handPosition) {
        return handCards1[handPosition - 1];
    }

    public int emptyHandCardIndex(SimpleCard[] handCards) {
        for (int i = 0; i < handCards.length; i++) {
            Card handCard = handCards[i];
            if (handCard == null) {
                return i;
            }
        }
        return -1;
    }

    public void deActive(ActorRef out, Object target) {
        if (target == null) {
            return;
        }
        if (target instanceof SimpleCard) {
            SimpleCard card = (SimpleCard) target;
            card.deActive(out);
            for (int i = 0; i < handCards1.length; i++) {
                SimpleCard simpleCard = handCards1[i];
                if (simpleCard == null) {
                    BasicCommands.deleteCard(out, i + 1);
                } else {
                    BasicCommands.drawCard(out, simpleCard, simpleCard.position, 0);
                }
            }
        } else if (target instanceof Summon) {
            Summon summon = (Summon) target;
            summon.deActive(out);
        }

    }

    public Summon getSummon(int tileX, int tileY) {
        for (Summon unit : player1Units) {
            if (unit.getPosition().getTilex() == tileX
                    && unit.getPosition().getTiley() == tileY) {
                return unit;
            }
        }
        for (Summon unit : player2Units) {
            if (unit.getPosition().getTilex() == tileX
                    && unit.getPosition().getTiley() == tileY) {
                return unit;
            }
        }
        return null;
    }

    public void nextTurn(ActorRef out) {
        checknextTurn = true;
        turns++;
        player1.setMana(turns);
        player2.setMana(turns);
        BasicCommands.setPlayer1Mana(out, player1);
        BasicCommands.setPlayer2Mana(out, player2);

        for (Summon unit : player1Units) {
            unit.finishTurn();
        }
        for (Summon unit : player2Units) {
            unit.finishTurn();
        }

        drawCard(1, Operator.PLAYER1, out);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getTurns() {
        return turns;
    }

    public Player getCurrentPlayer() {
        return current == Operator.PLAYER1 ? player1 : player2;
    }

    public List<Summon> getFriendlyUnits() {
        return current == Operator.PLAYER1 ? player1Units : player2Units;
    }

    public List<Summon> getEnemyUnits() {
        return current == Operator.PLAYER2 ? player1Units : player2Units;
    }

    public Summon getFriendlyAvatar() {
        return current == Operator.PLAYER1 ? avatar1 : avatar2;
    }

    public Summon getEnemyAvatar() {
        return current == Operator.PLAYER2 ? avatar1 : avatar2;
    }

    public void reduceMana(ActorRef out, int manacost) {
        checkreduceMana = true;
        Player player = getCurrentPlayer();
        player.setMana(player.getMana() - manacost);
        if (current == Operator.PLAYER1) {
            BasicCommands.setPlayer1Mana(out, player);
        } else {
            BasicCommands.setPlayer2Mana(out, player);
        }
    }

    public enum Operator {
        PLAYER1,
        PLAYER2
    }
}
