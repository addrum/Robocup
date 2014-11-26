
//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.AbstractTeam;
import com.github.robocup_atan.atan.model.ControllerCoach;
import com.github.robocup_atan.atan.model.ControllerPlayer;

/**
 * A class to setup a Simple Silly AbstractTeam.
 *
 * @author Atan
 */
public class SimplySillyTeam extends AbstractTeam {

    /**
     * Constructs a new simple silly team.
     *
     * @param name The team name.
     * @param port The port to connect to SServer.
     * @param hostname The SServer hostname.
     * @param hasCoach True if connecting a coach.
     */
    public SimplySillyTeam(String name, int port, String hostname, boolean hasCoach) {
        super(name, port, hostname, hasCoach);
    }

    /**
     * {@inheritDoc}
     *
     * The first controller of the team is the goalie and the others are players (11 is for the captain).
     */
    @Override
    //!!!IMPORTANT!!!
    //Instead of modifying the Simple.java file we decided to modify here which player gets which individual AI
    //Numbers 9 and 10 (in-game 10 and 11) get to be the defenders
    //While 1 and 2 (in-game 2 and 3) get to be attackers
    //And 0 (in-game 1) is the goalie
    public ControllerPlayer getNewControllerPlayer(int number) {
        if (number == 0) 
            return new GoalieManager();
        else if (number == 1 || number == 2)
        	return new AttackerManager();
        else if (number == 9)
        	return new DefenderManagerRight();
        else if (number == 10)
        	return new DefenderManagerLeft();
        else
            return new Simple();
    }

    /**
     * {@inheritDoc}
     *
     * Generates a new coach.
     */
    @Override
    public ControllerCoach getNewControllerCoach() {
        return new Coach();
    }
}
