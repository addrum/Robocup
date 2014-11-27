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

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Random;

/**
 * <p>
 * Silly class.
 * </p>
 * 
 * @author Atan
 */
public class AttackerManager implements ControllerPlayer {
	private double distBall = 1000;
	private double dirBall = 0;
	private double dirOwnGoal = 0;
	private double dirGoalOther = 0;
	private double distGoal = -1.0;
	private double distGoalOther = 1.0;
	private boolean canSeeGoal, canSeeGoalOther, dribble,
			canSeePenalty, alreadySeeingGoal, canSeeFieldEnd, canSeeGoalRight,
			canSeeGoalRightOther, canSeeGoalLeft, canSeeGoalLeftOther,
			needsToRetreat, canSeeBall = false;
	private ActionsPlayer player;
	private Random random = null;
	private static int count = 0;
	private double dirMultiplier = 1.0;
	private double goalTurn;
	private double distanceOwnPlayer;
	private double directionOwnPlayer;
	private double distanceBallOwnPlayer;
	private double distanceOtherPlayer;
	private boolean canSeeFlagRight;
	private boolean canSeeFlagLeft;

	public AttackerManager() {
		random = new Random(System.currentTimeMillis() + count);
		count++;
	}

	/** {@inheritDoc} */
	@Override
	public void preInfo() {
		distBall = 1000;
		distGoal = 1000;
		distGoalOther = 1000;
		dirGoalOther = 90;
		canSeeGoal = false;
		canSeeGoalOther = false;
		canSeeGoalLeft = false;
		canSeeGoalLeftOther = false;
		canSeeGoalRight = false;
		canSeeGoalRightOther = false;
		canSeePenalty = false;
		canSeeFieldEnd = false;
		canSeeBall = false;
		canSeeFlagRight = false;
		canSeeFlagLeft = false;
		dribble = false;
		goalTurn = 0.0;
	}

	/** {@inheritDoc} */
	@Override
	public void postInfo() {												/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (distBall < 15) {												//if the distance to the ball is less than 15 from the agent		
			if (canSeeBall) {												//|		if the agent can see the ball (agent is facing the ball)
				getPlayer().turn(dirBall);									//|		|		make the agent turn his body to the ball
				getPlayer().turnNeck(dirGoalOther);							//|		|		make the agent turn his head to face the other goal
				getPlayer().dash(randomDashValueVeryFast());				//|		|		make the agent dash very fast to the ball 
			}																//|		|
			if (distBall < 0.7) {											//|		if the distance to the ball is less than 0.7 from the agent (very close)
				if (canSeeGoal) {											//|		|		if the agent can see his own goal (facing his own goal)
					if (distGoal < 20) {									//|		|		|	if the agent is less than 20 from his own goal										
						this.getPlayer().kick(60, 135);						//|		|		|	|	kick the ball hard away from the goal to clear it
					} else {												//|		|		|	if the agent is greater than 20 from his own goal	
						if (canSeeFlagLeft) {								//|		|		|	|	if the agent can see the left side of the field
							this.getPlayer().kick(10, 45);					//|		|		|	|	|	kick the ball soft enough to get to at a 45 degree angle clockwise
						} else if (canSeeFlagRight) {						//|		|		|	|	if the agent can see the right side of the field
							this.getPlayer().kick(10, -45);					//|		|		|	|	|	kick the ball soft enough to get to at a 45 degree angle anti-clockwise
						} else {											//| 	|		|	|	if the agent can't see either the left or right side of the pitch
							this.getPlayer().kick(20, dirGoalOther);		//|		|		|	|	|	dribble the ball in the direction of the other team's goal	
						}													//|		|		|	|	|	
					}														//|		|		|	|
				} else if (canSeeGoalOther) {								//|		|		if the agent can see the other teams goal (facing the other team's goal)
					if (distGoalOther < 23) {								//|		|		|	if the agent is less than 23 from the opponent's goal
						this.getPlayer().kick(100, dirGoalOther);			//|		|		|	|	kick the ball as hard as possible to try and score
					} else {												//|		|		|	if the agent is greater than 23 from the opponent's goal
						if (distanceOtherPlayer < 2) {						//|		|		|	|	if the agent is less than 2 from an opponent
							this.getPlayer().kick(30, directionOwnPlayer);	//|		|		|	|	|	pass the ball towards a team mate	
						} else {											//|		|		|	|	if the agent is greater than 2 from an opponent
							if (canSeeFlagLeft) {							//|		|		|	|	|	if the agent can see the left side of the field		
								this.getPlayer().kick(10, 45);				//|		|		|	|	|	|	kick the ball soft enough to get to at a 45 degree angle clockwise
							} else if (canSeeFlagRight) {					//|		|		|	|	|	if the agent can see the right side of the field
								this.getPlayer().kick(10, -45);				//|		|		|	|	|	|	kick the ball soft enough to get to at a 45 degree angle anti-clockwise
							} else {										//| 	|		|	|	|	if the agent can't see either the left or right side of the pitch
								this.getPlayer().kick(20, dirGoalOther);	//|		|		|	|	|	|	dribble the ball in the direction of the other team's goal
							}												//|		|		|	|	|	
						}													//|		|		|	|
					}														//|		|		|
				} else {													//|		if the distance to the ball is greater than 0.7 from the agent (very close)
					if (canSeeFlagLeft) {									//|		|		if the agent can see the left side of the field
						this.getPlayer().kick(10, 45);						//|		|		|	kick the ball soft enough to get to at a 45 degree angle clockwise
					} else if (canSeeFlagRight) {							//|		|		if the agent can see the right side of the field
						this.getPlayer().kick(10, -45);						//|		|		|	kick the ball soft enough to get to at a 45 degree angle anti-clockwise
					} else {												//| 	|		if the agent can't see either the left or right side of the pitch
						this.getPlayer().kick(20, dirGoalOther);			//|		|		|	dribble the ball in the direction of the other team's goal
					}														//|		|
				}															//|
			}																//|
		} else {															//if the distance to the ball is greater than 15 from the agent
			if (canSeeBall) {												//|		if the agent can see the ball (agent is facing the ball)
				getPlayer().turn(dirBall);									//|		|		make the agent turn his body to the ball	
				getPlayer().turnNeck(dirGoalOther);							//|		|		make the agent turn his head to face the other goal
				getPlayer().dash(randomDashValueFast());					//|		|		make the agent dash very fast to the ball
			} else if (!canSeeBall) {										//|		if the agent can't see the ball
				if (!canSeeGoal && !needsToRetreat) {						//|		|		if the agent can't see his own goal and doesn't need to retreat
					if (!canSeePenalty) {									//|		|		|	if the agent can't see his own penalty box
						getPlayer().turn(90);								//|		|		|	|	turn 90 degrees to face a more appropriate position
						getPlayer().dash(randomDashValueFast());			//|		|		|	|	sprint in that direction		
					} else if ((canSeeGoalLeft || canSeeGoalRight)			//|		|		|	if the agent can see his own goal's left or right flag and can't see the end of the field
							&& !canSeeFieldEnd) {							//|		|		|	
						getPlayer().turn(-1.0 * goalTurn);					//|		|		|	|	turn in a direction based on which flag he sees and how much to turn by
						getPlayer().dash(randomDashValueSlow());			//|		|		|	|	dash in that direction slowly to conserve stamina
					} else													//|		|		|	if the agent can see his penalty box and can't see the left or right goal flags or the end of the field
						getPlayer().turn(25 * dirMultiplier);				//|		|		|	|	turn either positive or negative based on what he sees (dirMultiplier)
				} else {													//|		|		if the agent can see his goal and needs to retreat
					if (!canSeeGoalOther) {									//|		|		if the agent can't see the other team's goal
						getPlayer().turn(90);								//|		|		|	turn 90 degrees clockwise
						getPlayer().dash(randomDashValueSlow());			//|		|		|	and dash slowly to conserve stamina	
					} else if (distGoalOther > 50) {						//|		|		if the agen't can see the other goal and the distance to the goal is greater than 50
						if (!alreadySeeingGoal) {							//|		|		|	if the agent isn't already seeing the other goal
							getPlayer().turn(dirGoalOther);					//|		|		|	|	turn towards the other goal
							alreadySeeingGoal = true;						//|		|		|	|	and set the flag to say he is now seeing the goal
						}													//|		|		|	
						getPlayer().dash(randomDashValueVeryFast());		//|		|		|	and dash fast to get there
					} else {												//|		|		if the agent is less than 50 from the other goal
						needsToRetreat = false;								//|		|		|	then he doesn't need to retreat anymore
						if (alreadySeeingGoal) {							//|		|		|	if he's already seeing the other goal
							getPlayer().turn(goalTurn);						//|		|		|	|	turn positive or negative based on what he sees
							alreadySeeingGoal = false;						//|		|		|	|	now he's not seeing the goal anymore
						} else												//|		|		|	if he isn't already seeing the other goal
							alreadySeeingGoal = true;						//|		|		|	|	set the flag to say that he is now seeing the goal
					}
				}
			}
		}
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
	public void infoSeeLine(Line line, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeBall(double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
		distBall = distance;	//set the agent's distance to the ball equal to what the infoSeeBall provides
		dirBall = direction;	//set the direction of the ball to the agent equal to what the infoSeeBall provides
		canSeeBall = true;		//set the flag to say the agent can see the ball
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearReferee(RefereeMessage refereeMessage) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearPlayMode(PlayMode playMode) {
		if (playMode == PlayMode.BEFORE_KICK_OFF)
			getPlayer().move(-10, 0);
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearPlayer(double direction, String string) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSenseBody(ViewQuality viewQuality, ViewAngle viewAngle,
			double stamina, double unknown, double effort, double speedAmount,
			double speedDirection, double headAngle, int kickCount,
			int dashCount, int turnCount, int sayCount, int turnNeckCount,
			int catchCount, int moveCount, int changeViewCount) {
	}

	/** {@inheritDoc} */
	@Override
	public String getType() {
		return "Attacker";
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
	public void infoSeeFlagRight(Flag flag, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
		canSeeFlagRight = true; //set the flag to say the agent can see the right side of the field
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagLeft(Flag flag, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
		canSeeFlagLeft = true;	//set the flag to say the agent can see the left side of the field
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOwn(Flag flag, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
		canSeeFieldEnd = true;	//set the flag to say the agent can see the end of the field
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOther(Flag flag, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCenter(Flag flag, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCornerOwn(Flag flag, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCornerOther(Flag flag, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOwn(Flag flag, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
		canSeePenalty = true;	//set the flag to say the agent can see his own penalty
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOther(Flag flag, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOwn(Flag flag, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
		if (!alreadySeeingGoal)						//if the agent isn't already seeing the goal
			dirMultiplier *= -1.0;					//|		set the direction to turn to positive

		if (flag.compareTo(Flag.CENTER) == 0) {		//if the flag returned is the center
			distGoal = distance;					//|		set the distance to the goal
			dirOwnGoal = direction;					//|		set the direction to the goal

			canSeeGoal = true;						//|		set the flag that the agent can see the goal

			goalTurn = 180;							//|		set the amount to turn to 180 so he turns around completely
		}
		if (flag.compareTo(Flag.LEFT) == 0) {		//if the flag returned is the left side of the goal
			canSeeGoalLeft = true;					//|		set the flag that the agent see's the left side of the goal
			goalTurn = 90;							//|		make the agent turn 90 degrees in a clockwise direction so he faces up the pitch again instead of the end of the field
		}
		if (flag.compareTo(Flag.RIGHT) == 0) {		//if the flag returned is the right side of the goal
			canSeeGoalRight = true;					//|		set the flag that the agent see's the right side of the goal
			goalTurn = -90;							//|		make the agent turn 90 degrees in a anti-clockwise direction so he faces up the pitch again instead of the end of the field
		}
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOther(Flag flag, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
		if (!alreadySeeingGoal)
			dirMultiplier *= -1.0;

		distGoalOther = distance;
		dirGoalOther = direction;

		if (flag.compareTo(Flag.CENTER) == 0) {
			canSeeGoalOther = true;

			goalTurn = 180;
		}
		if (flag.compareTo(Flag.LEFT) == 0) {
			canSeeGoalLeftOther = true;
			goalTurn = 90;
		}
		if (flag.compareTo(Flag.RIGHT) == 0) {
			canSeeGoalRightOther = true;
			goalTurn = -90;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOther(int number, boolean goalie, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
		distanceOtherPlayer = distance;	//set the distance to an opposing player to the distance from the info 
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOwn(int number, boolean goalie, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
		distanceOwnPlayer = distance;								//set the distance to a team mate to the distance from the info 
		directionOwnPlayer = direction;								//set the direction of a team mate to the direction from the info 
		distanceBallOwnPlayer = Math.sqrt((distBall * distBall)		//work out the distance of the team mate's distance to the ball based on a^2 = b^2 + c^2
				+ (distanceOwnPlayer * distanceOwnPlayer));
	}

	/** {@inheritDoc} */
	@Override
	public void infoPlayerParam(double allowMultDefaultType,
			double dashPowerRateDeltaMax, double dashPowerRateDeltaMin,
			double effortMaxDeltaFactor, double effortMinDeltaFactor,
			double extraStaminaDeltaMax, double extraStaminaDeltaMin,
			double inertiaMomentDeltaFactor, double kickRandDeltaFactor,
			double kickableMarginDeltaMax, double kickableMarginDeltaMin,
			double newDashPowerRateDeltaMax, double newDashPowerRateDeltaMin,
			double newStaminaIncMaxDeltaFactor, double playerDecayDeltaMax,
			double playerDecayDeltaMin, double playerTypes, double ptMax,
			double randomSeed, double staminaIncMaxDeltaFactor, double subsMax) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoPlayerType(int id, double playerSpeedMax,
			double staminaIncMax, double playerDecay, double inertiaMoment,
			double dashPowerRate, double playerSize, double kickableMargin,
			double kickRand, double extraStamina, double effortMax,
			double effortMin) {
	}

	/** {@inheritDoc} */

	private int randomDashValueVeryFast() {
		return 100 + random.nextInt(30);
	}

	private int randomDashValueFast() {
		return 30 + random.nextInt(100);
	}

	/**
	 * Randomly choose a slow dash value.
	 * 
	 * @return
	 */
	private int randomDashValueSlow() {
		return -10 + random.nextInt(50);
	}

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
}
