//~--- non-JDK imports --------------------------------------------------------

import com.github.robocup_atan.atan.model.ActionsPlayer;
import com.github.robocup_atan.atan.model.ControllerPlayer;
import com.github.robocup_atan.atan.model.enums.Errors;
import com.github.robocup_atan.atan.model.enums.Flag;
import com.github.robocup_atan.atan.model.enums.Line;
import com.github.robocup_atan.atan.model.enums.Ok;
import com.github.robocup_atan.atan.model.enums.PlayMode;
import com.github.robocup_atan.atan.model.enums.RefereeMessage;
import com.github.robocup_atan.atan.model.enums.ServerParams;
import com.github.robocup_atan.atan.model.enums.ViewAngle;
import com.github.robocup_atan.atan.model.enums.ViewQuality;
import com.github.robocup_atan.atan.model.enums.Warning;

import org.apache.log4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Random;

// ROBOCUP PEAS:

// Performance: play a game of 2D robocyp footbal simulator following the rules and score more goals than the enemy team
// Environment: Known environment
// Actuators: Legs to dash and kick the ball, body to turn, hands to pick up the ball (goalie only)
// Sensors: Eyes to view the 90 degree FOV, ears to hear commands by other players or the coach

// ROBOCUP TASK ENVIRONMENT:

// Partially Observable:
//     The player can only see what is in range of the 90 degree viewing sensor, everything else
//     the player assumes it is either still there in the same direction and distance it saw it last
//     or he assumes doesn't even exist anymore.
// Multi-Agent:
//     There are multiple thinking entities in the game at once and each may have similar or 
//     different state machines and behaviour
// Deterministic:
//     It is guaranteed that kicking a ball in a direction will send it to that direction.
//     The agent will do anything you program it to do.
// Sequential:
//     At different states and at different times or distances, the agent does something different.
//     For instance, episodic environments would assume that each player does exactly the same thing
//     in every scenario regardless of position or state
// Dynamic:
//     While the player deliberates, the game goes on (ie, if the player is finding the furthest own
//     player then the game still goes on, he can get tackled while he is thinking)
// Continuous:
//     The player can do a multitude of actions at once, such as kick and run at the same time and
//     look and run
// Partially Known Environment:
//     Every player knows the rules of the game, but unless you constantly tell the player the distance
//     between itself and things it hasn't seen for a while, it won't know

// Agent Type: Simple Reflex Agent
//     because it reacts to the current state of it's environment without taking into account what happened before

/**
 * A simple controller. It implements the following simple behaviour. If the
 * client sees nothing (it might be out of the field) it turns 180 degree. If
 * the client sees the own goal and the distance is less than 40 and greater
 * than 10 it turns to his own goal and dashes. If it cannot see the own goal
 * but can see the ball it turns to the ball and dashes. If it sees anything but
 * not the ball or the own goals it dashes a little bit and turns a fixed amount
 * of degree to the right.
 * 
 * @author Atan
 */
public class Simple implements ControllerPlayer {
	private static int count = 0;
	private static Logger log = Logger.getLogger(Simple.class);
	private Random random = null;
	private boolean canSeeOwnGoal = false;
	private boolean canSeeNothing = true;
	private boolean canSeeBall = false;
	private double directionBall;
	private double directionOwnGoal = -1.0;
	private double directionOtherGoal = 0;
	private double directionOwnPlayer;
	private double directionToTurn;
	private double distanceBall;
	private double distanceOwnGoal;
	private double distanceOtherGoal = 1000;
	private double distanceOwnPlayer;
	private double distanceOtherPlayer;
	private double distanceBallOwnPlayer;
	private ActionsPlayer player;
	private boolean canSeeGoal;
	private boolean canSeeGoalOther;
	private boolean canSeeFlagLeft;
	private boolean canSeeFlagRight;
	// the distance from this player to the ball
	private double distBall = 1000;
	// the direction from this player to the ball
	private double dirBall = 0;
	// the direction from this player to the own goal
	private double dirOwnGoal = 0;
	// the direction from this player to the other goal
	private double dirGoalOther = 0;
	// the distance from this player to the own goal
	private double distGoal = -1.0;
	// the distance from this player to the other goal
	private double distGoalOther = 1.0;
	// the distance from this player to the sidelines
	private double sidelineDistance;
	private double lineDistance;
	private boolean canSeeLine;

	/**
	 * Constructs a new simple client.
	 */
	public Simple() {
		random = new Random(System.currentTimeMillis() + count);
		count++;
	}

	/** {@inheritDoc} */
	@Override
	public ActionsPlayer getPlayer() {
		return player;
	}

	/** {@inheritDoc} */
	@Override
	public void setPlayer(ActionsPlayer p) {
		player = p;
	}

	/** {@inheritDoc} */
	@Override
	public void preInfo() {
		// reset values to default so they can be updated correctly on each tick
		canSeeOwnGoal = false;
		canSeeBall = false;
		canSeeNothing = true;
		distBall = 1000;
		distGoal = 1000;
		distGoalOther = 1000;
		dirGoalOther = 90;
		canSeeGoal = false;
		canSeeGoalOther = false;
		canSeeBall = false;
		canSeeFlagRight = false;
		canSeeFlagLeft = false;
		canSeeLine = false;
	}

	/** {@inheritDoc} */
	@Override
	public void postInfo() {
		if (canSeeNothing) {											//if the agent can see nothing
		} else if (canSeeOwnGoal) {										//if the agent can see his own goal
			if ((distanceOwnGoal < 20) && (distanceOwnGoal > 10)) {		//|		if the distance to his own goal is between 10 and 20
				canSeeOwnGoalAction();									//|		|	do appropriate defensive actions
			} else if (canSeeBall) {									//|		if the distance is not between 10 and 20 and the agent can see the ball 
				if (distanceOwnPlayer < 10) {							//|		|	if the distance to a team mate is less than 10
					if (distanceBallOwnPlayer < distanceBall) {			//|		|	|	if the team mate is closer to the ball than the agent is
						if (canSeeFlagLeft) {							//|		|	|	|	if the agent can see the left side of the goal
							getPlayer().turn(-45);						//|		|	|	|	|	turn anti clockwise to face a more appropriate direction
						} else if (canSeeFlagRight) {					//|		|	|	|	if the agent can see the right side of the goal
							getPlayer().turn(45);						//|		|	|	|	|	turn clockwise to face a more appropriate direction		
						}												//|		|	|	|
					} else {											//|		|	|	if the agent is closer to the ball than the team mate is
						if (distanceOtherPlayer < 10) {					//|		|	|	|	if the distance to an opponent is less than 10
							canSeeBallAction(directionOwnPlayer);		//|		|	|	|	|	make appropriate ball actions in the direction of a team mate
						} else {										//|		|	|	|	if the distance to an opponent is greater than 10
							canSeeBallAction(directionOtherGoal);		//|		|	|	|	|	make appropriate ball actions in the direction of the other goal
						}												//|		|	|	|
					}													//|		|	|
				} else {												//|		|	if the distance to a team mate is greater than 10
					if (distanceOtherPlayer < 10) {						//|		|	|	if the distance to an opponent is less than 10	
						canSeeBallAction(directionOwnPlayer);			//|		|	|	|	make appropriate ball actions in the direction of a team mate
					} else {											//|		|	|	if the distance to an opponent is greater than 10
						canSeeBallAction(directionOtherGoal);			//|		|	|	|	make appropriate ball actions in the direction of the other goal
					}													//|		|	|
				}														//|		|
			} else {													//|		if the distance is not between 10 and 20 and the agent can't see the ball
				canSeeAnythingAction();									//|		|	do appropriate actions for seeing something
			}															//|		
		} else if (canSeeBall) {										//if the agent can see the ball
			if (distanceOtherPlayer < 10) {								//|		if the distance to an opponent is less than 10
				canSeeBallAction(directionOwnPlayer);					//|		|	make appropriate ball actions in the direction of a team mate
			} else {													//|		if the distance to an opponent is greater than 10
				canSeeBallAction(directionOtherGoal);					//|		|	make appropriate ball actions in the direction of the other goal
			}													
		} else {
			canSeeAnythingAction();
		}
		if (lineDistance < 10) {										//if the distance to the edge of the pitch is less than 10
			if (canSeeFlagLeft) {										//|		if the agent can see the left end of the pitch
				getPlayer().turn(-45);									//|		|	turn anti-clockwise to face the rest of the pitch
			} else if (canSeeFlagRight) {								//|		if the agent can see the right end of the pitch
				getPlayer().turn(45);									//|		|	turn clockwise to face the rest of the pitch
			} else {													//|		if the agent can't see the left or right end's of the pitch
				getPlayer().turn(180);									//|		|	turn around completely to face the rest of the pitch
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
		canSeeFlagRight = true;		//set the flag to say the agent can see the right side of the pitch
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
		canSeeFlagLeft = true;		//set the flag to say the agent can see the left side of the pitch
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCenter(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCornerOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCornerOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
		if (flag == Flag.CENTER) {
			canSeeOwnGoal = true;
			distanceOwnGoal = distance;
			directionOwnGoal = direction;
		}
		distanceOtherGoal = 104 - distanceOwnGoal;	// work ou the distance to the other goal
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
		distanceOtherGoal = distance;
		directionOtherGoal = direction;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeLine(Line line, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
		lineDistance = distance;
		canSeeLine = true;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		this.distanceOtherPlayer = distance;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		this.distanceOwnPlayer = distance;																				//set the distance to a team mate to the distance from the info
		this.directionOwnPlayer = direction;																			//set the direction of a team mate to the direction from the info	
		this.directionToTurn = 90;																						//set the amount the agent will turn to stop being too close to team mates
		this.distanceBallOwnPlayer = Math.sqrt((distanceBall * distanceBall) + (distanceOwnPlayer * distanceOwnPlayer));//work out the distance of the team mate's distance to the ball based on a^2 = b^2 + c^2
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeBall(double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
		canSeeBall = true;
		distanceBall = distance;
		directionBall = direction;
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearReferee(RefereeMessage refereeMessage) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearPlayMode(PlayMode playMode) {
		if (playMode == PlayMode.BEFORE_KICK_OFF) {
			this.pause(1000);
			switch (this.getPlayer().getNumber()) {
			case 1:
				this.getPlayer().move(-50, 0);
				break;
			case 4:
				this.getPlayer().move(-20, 0);
				break;
			case 5:
				this.getPlayer().move(-20, 10);
				break;
			case 6:
				this.getPlayer().move(-20, -10);
				break;
			case 7:
				this.getPlayer().move(-20, 20);
				break;
			case 8:
				this.getPlayer().move(-20, -20);
				break;
			case 9:
				this.getPlayer().move(-30, 0);
				break;
			case 10:
				this.getPlayer().move(-40, 10);
				break;
			case 11:
				this.getPlayer().move(-40, -10);
				break;
			default:
				throw new Error("number must be initialized before move");
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearPlayer(double direction, String message) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSenseBody(ViewQuality viewQuality, ViewAngle viewAngle, double stamina, double unknown, double effort, double speedAmount, double speedDirection, double headAngle, int kickCount,
			int dashCount, int turnCount, int sayCount, int turnNeckCount, int catchCount, int moveCount, int changeViewCount) {
	}

	/** {@inheritDoc} */
	@Override
	public String getType() {
		return "Simple";
	}

	/** {@inheritDoc} */
	@Override
	public void setType(String newType) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearError(Errors error) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearOk(Ok ok) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearWarning(Warning warning) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoPlayerParam(double allowMultDefaultType, double dashPowerRateDeltaMax, double dashPowerRateDeltaMin, double effortMaxDeltaFactor, double effortMinDeltaFactor,
			double extraStaminaDeltaMax, double extraStaminaDeltaMin, double inertiaMomentDeltaFactor, double kickRandDeltaFactor, double kickableMarginDeltaMax, double kickableMarginDeltaMin,
			double newDashPowerRateDeltaMax, double newDashPowerRateDeltaMin, double newStaminaIncMaxDeltaFactor, double playerDecayDeltaMax, double playerDecayDeltaMin, double playerTypes,
			double ptMax, double randomSeed, double staminaIncMaxDeltaFactor, double subsMax) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoPlayerType(int id, double playerSpeedMax, double staminaIncMax, double playerDecay, double inertiaMoment, double dashPowerRate, double playerSize, double kickableMargin,
			double kickRand, double extraStamina, double effortMax, double effortMin) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoCPTOther(int unum) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoCPTOwn(int unum, int type) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoServerParam(HashMap<ServerParams, Object> info) {
	}

	/**
	 * This is the action performed when the player can see the ball. It
	 * involves running at it and kicking it...
	 */
	private void canSeeBallAction(double direction) {
		if (distanceOwnPlayer < 15 && (distanceBallOwnPlayer < distanceBall)) {			//if the agent is less than 15 from his own team mate and the team mate is closer to the ball
			getPlayer().turn(dirOwnGoal);												//|		turn to face the other goal to stay away from the team mate
			getPlayer().dash(this.randomDashValueSlow());								//|		run slowly to save stamina
		} else if (distanceOwnPlayer < 15 && (distanceBallOwnPlayer > distanceBall)) {	//if the agent is less than 15 from his own team mate but he is closer to the ball
			getPlayer().turn(directionBall);											//|		face the ball
			getPlayer().dash(this.randomDashValueFast());								//|		and dash quickly
			if (distanceBall < 10) {													//|		if the agent is less than 10 from the ball
				if (distanceBall < 0.7) {												//|		|	if the agent is less than 0.7 from the ball
					if (canSeeGoal) {													//|		|	|	if he is, can he see the goal?
						if (distGoal < 20) {											//|		|	|	|	if he can, is he less than 20 to his own goal?								
							this.getPlayer().kick(60, 135);								//|		|	|	|	|	if he is, kick it hard away from the goal
						} else {														//|		|	|	|	if he isn't, 						
							this.getPlayer().kick(20, dirGoalOther);					//|		|	|	|	|	dribble the ball towards the other goal so he doesn't give away possession	
						}																//|		|	|	|	
						getPlayer().turn(dirBall);										//|		|	|	|	turn towards the ball 
						getPlayer().turnNeck(dirGoalOther);								//|		|	|	|	and face the other goal regardless of what happens
					} else if (canSeeGoalOther) {										//|		|	|	if he can't, can he see the other goal?					
						if (distGoalOther < 23) {										//|		|	|	|	if he can, is he less than 23 to the other goal?
							this.getPlayer().kick(100, dirGoalOther);					//|		|	|	|	|	if he is, kick the ball as hard as he can towards the other goal to try and score
						} else {														//|		|	|	|	|	|	if he isn't, is he less than 2 from a player on the other team?
							if (distanceOtherPlayer < 2) {								//|		|	|	|	|	|	|	if the agent is less than 2 from an opponent																						
								this.getPlayer().kick(50, directionOwnPlayer);			//|		|	|	|	|	|	|	|	attempt to pass in the direction of his own team
							} else {													//|		|	|	|	|	|	|	if he isn't
								this.getPlayer().kick(20, dirGoalOther);				//|		|	|	|	|	|	|	|	dribble towards the other goal so he doesn't give away possession
							}
						}						
						getPlayer().turn(dirBall);										//|		|	|	|	|	|	|	turn towards the ball 
						getPlayer().turnNeck(dirGoalOther);								//|		|	|	|	|	|	|	and face the other goal regardless of what happens
					} else {															//|		|	|	|	|	|	if he can't see his own goal, or the other goal						
						this.getPlayer().kick(20, dirGoalOther);						//|		|	|	|	|	|	|	dribble towards the other goal
						getPlayer().turn(dirBall);										//|		|	|	|	|	|	|	turn towards the ball
						getPlayer().turnNeck(dirGoalOther);								//|		|	|	|	|	|	|	and face the other goal regardless of what happens
					}
				}
			}
		} else {
			getPlayer().turn(20);
			getPlayer().dash(this.randomDashValueSlow());
		}
		if (log.isDebugEnabled()) {
			log.debug("b(" + directionBall + "," + distanceBall + ")");
		}
	}

	/**
	 * If the player can see anything that is not a ball or a goal, it turns.
	 */
	private void canSeeAnythingAction() {
		if (distanceOwnPlayer < 15 && (distanceBallOwnPlayer > distanceBall)) {
			getPlayer().dash(this.randomDashValueSlow());
			getPlayer().turn(directionToTurn);
		} else {
			getPlayer().dash(this.randomDashValueSlow());
			getPlayer().turn(directionBall);
		}
		if (log.isDebugEnabled()) {
			log.debug("a");
		}
	}

	/**
	 * If the player can see nothing, it turns 180 degrees.
	 */
	private void canSeeNothingAction() {
		getPlayer().turn(180);
		if (log.isDebugEnabled()) {
			log.debug("n");
		}
	}

	/**
	 * If the player can see its own goal, it goes and stands by it...
	 */
	private void canSeeOwnGoalAction() {
		turnTowardOwnGoal();
		getPlayer().dash(this.randomDashValueFast());
		getPlayer().turn(180);
		if (log.isDebugEnabled()) {
			log.debug("g(" + directionOwnGoal + "," + distanceOwnGoal + ")");
		}
	}

	/**
	 * Randomly choose a fast dash value.
	 * 
	 * @return
	 */
	private int randomDashValueFast() {
		return 30 + random.nextInt(100);
	}

	private int randomDashValueVeryFast() {
		return 100 + random.nextInt(30);
	}

	/**
	 * Randomly choose a slow dash value.
	 * 
	 * @return
	 */
	private int randomDashValueSlow() {
		return -10 + random.nextInt(50);
	}

	/**
	 * Turn towards the ball.
	 */
	private void turnTowardBall() {
		getPlayer().turn(directionBall);
	}

	/**
	 * Turn towards our goal.
	 */
	private void turnTowardOwnGoal() {
		getPlayer().turn(directionOwnGoal);
	}

	/**
	 * Randomly choose a kick direction.
	 * 
	 * @return
	 */
	private int randomKickDirectionValue() {
		return -45 + random.nextInt(90);
	}

	/**
	 * Pause the thread.
	 * 
	 * @param ms
	 *            How long to pause the thread for (in ms).
	 */
	private synchronized void pause(int ms) {
		try {
			this.wait(ms);
		} catch (InterruptedException ex) {
			log.warn("Interrupted Exception ", ex);
		}
	}
}
