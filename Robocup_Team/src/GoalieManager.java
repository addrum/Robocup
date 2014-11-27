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
public class GoalieManager implements ControllerPlayer {
	private double distBall = 1000;
	private double dirBall = 0;
	private double dirOwnGoal = 0;
	private double distGoal = -1.0;
	private boolean canSeeGoal = false;
	private boolean canSeeGoalLeft = false;
	private boolean canSeeGoalRight = false;
	private boolean canSeeFieldEnd = false;
	private boolean alreadySeeingGoal = false;
	private boolean canSeePenalty = false;
	private ActionsPlayer player;
	private Random random = null;
	private static int count = 0;
	private double dirMultiplier = 1.0;
	private double goalTurn;
	private boolean needsToRetreat = false;

	public GoalieManager() {
		random = new Random(System.currentTimeMillis() + count);
		count++;
	}

	/** {@inheritDoc} */
	@Override
	public void preInfo() {
		distBall = 1000;
		distGoal = 1000;
		canSeeGoal = false;
		canSeeGoalLeft = false;
		canSeeGoalRight = false;
		canSeePenalty = false;
		canSeeFieldEnd = false;
		goalTurn = 0.0;
	}

	/** {@inheritDoc} */
	@Override
	public void postInfo() {                                                          //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (distBall < 15) {                                                          //If the ball is within a range of 15 from the agnet (close)
			if (distBall < 0.7) {                                                     //|   if the ball is within a range of 0.7 from the agent (extremely close)
				if (canSeeGoal || canSeePenalty)                                      //|   |   if the agent can see his own team's goal or his own team's penalty box
					this.getPlayer().catchBall(dirBall);                              //|   |   |   the agent catches the ball
				if (canSeeGoal)                                                       //|   |   if the agent can just see his own team's goal
					this.getPlayer().kick(60, 135);                                   //|   |   |   the agent kicks the ball with a force of 60 135 degrees clockwise from the agent is currently facing
				else                                                                  //|   |   if the agen't can't see his own team's goal or his own team's penalty box
					this.getPlayer().kick(60, 0);                                     //|   |   |   kick the ball in the direction the agent is currently facing
			} else if (canSeeGoal || canSeePenalty) {                                 //|   if the agent can see his own team's goal or his own team's penalty box
				if (distBall < 2) {                                                   //|   |   if the ball is within distance 2 from the agent (pretty close)
					needsToRetreat = true;                                            //|   |   |   the agent needs to retreat (remember that)
					getPlayer().turn(dirBall);                                        //|   |   |   turn the agent in the direction of the ball
					getPlayer().dash(randomDashValueFast());                          //|   |   |   the agent dashes fast
				} else {                                                              //|   |   if the ball is not within distance 2 of the agent (not that close)
					needsToRetreat = true;                                            //|   |   |   the agent needs to retreat (remember that)
					getPlayer().turn(dirBall);                                        //|   |   |   turn the agent in the direction of the ball
					getPlayer().dash(randomDashValueVeryFast());                      //|   |   |   the agent dashes very fast
				}                                                                     //|
			}                                                                         //|
		} else {                                                                      //If the ball is not within a range of 15 from the agent (not close)
			if (!canSeeGoal && !needsToRetreat) {                                     //|   if the agent can't see his own team's goal and he doesn't need to retreat
				if (!canSeePenalty) {                                                 //|   |   if the agent can't see his own team's penalty box
					getPlayer().turn(90);                                             //|   |   |   turn the agent 90 degrees clockwise from where he is currently facing
					getPlayer().dash(randomDashValueFast());                          //|   |   |   the agent dashes fast
				} else if ((canSeeGoalLeft || canSeeGoalRight) && !canSeeFieldEnd) {  //|   |   if the agent can see either the right or left side of his own team's goal and he can't see the field end
					getPlayer().turn(-1.0 * goalTurn);                                //|   |   |   turn the agent towards the goal
					getPlayer().dash(randomDashValueSlow());                          //|   |   |   the player dashes slow
				} else                                                                //|   |   if none of the conditions above are met (agent can see penalty box and field end)
					getPlayer().turn(25 * dirMultiplier);                             //|   |   |   turn the agent 25 degrees in a specific direction
			} else {                                                                  //|   if the agent see his own team's goal or needs to retreat
				if (!canSeeGoal) {                                                    //|   |   if the agent can't see his own team's goal
					getPlayer().turn(90);                                             //|   |   |   turn the agent 90 degrees from where he is facing
					getPlayer().dash(randomDashValueSlow());                          //|   |   |   the agent dashes slow
				} else if (distGoal > 3.5) {                                          //|   |   if the agent can see his own team's goal and he more than 3.5 distance away from it
					if (!alreadySeeingGoal) {                                         //|   |   |   if the agent is already seeing the goal
						getPlayer().turn(dirOwnGoal);                                 //|   |   |   |   turn the agent in the direction oh his own team's goal
						alreadySeeingGoal = true;                                     //|   |   |   |   the agent is already seeing his own team's goal (remember that)
					}                                                                 //|   |   |   |
					getPlayer().dash(randomDashValueVeryFast());                      //|   |   |   the agent dashes very fast
				} else {                                                              //|   |   if the agent is within 3.5 distance from his own team's goal
					needsToRetreat = false;                                           //|   |   |   the agent doesn't have to retreat
					if (alreadySeeingGoal) {                                          //|   |   |   if the agent is already seeing the goal
						getPlayer().turn(goalTurn);                                   //|   |   |   |   the agent turns towards the goal
						alreadySeeingGoal = false;                                    //|   |   |   |   the agent isn't seeing the goal anymore (remember that)
					} else                                                            //|   |   |   if the agent is not already seeing the goal
						alreadySeeingGoal = true;                                     //|   |   |   |   he is already seeing the goal
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
	public void infoSeeLine(Line line, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeBall(double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		//here we save the distance and direction of the ball
		distBall = distance;
		dirBall = direction;
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearReferee(RefereeMessage refereeMessage) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearPlayMode(PlayMode playMode) {
		if (playMode == PlayMode.BEFORE_KICK_OFF)
			getPlayer().move(-50, 0);
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearPlayer(double direction, String string) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSenseBody(ViewQuality viewQuality, ViewAngle viewAngle, double stamina, double unknown, double effort, double speedAmount, double speedDirection, double headAngle, int kickCount,
			int dashCount, int turnCount, int sayCount, int turnNeckCount, int catchCount, int moveCount, int changeViewCount) {
	}

	/** {@inheritDoc} */
	@Override
	public String getType() {
		return "Goalie";
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
	public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		//here we save data about if we see the end of the field
		canSeeFieldEnd = true;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCenter(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCornerOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCornerOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		//here we save data if we can see our own penalty box
		canSeePenalty = true;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		//here we save data if we see our own goal depending on which flag is in view
		if (!alreadySeeingGoal)
			dirMultiplier *= -1.0;

		if (flag.compareTo(Flag.CENTER) == 0) {
			distGoal = distance;
			dirOwnGoal = direction;

			canSeeGoal = true;

			goalTurn = 180;
		}
		if (flag.compareTo(Flag.LEFT) == 0) {
			canSeeGoalLeft = true;
			goalTurn = 90;
		}
		if (flag.compareTo(Flag.RIGHT) == 0) {
			canSeeGoalRight = true;
			goalTurn = -90;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
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
