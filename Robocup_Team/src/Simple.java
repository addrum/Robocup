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
	}

	/** {@inheritDoc} */
	@Override
	public void postInfo() {
		if (canSeeNothing) {
		} else if (canSeeOwnGoal) {
			if ((distanceOwnGoal < 20) && (distanceOwnGoal > 10)) {
				canSeeOwnGoalAction();
			} else if (canSeeBall) {
				if (distanceOtherPlayer < 10) {
					canSeeBallAction(directionOwnPlayer);
				} else {
					canSeeBallAction(directionOtherGoal);
				}
			} else {
				canSeeAnythingAction();
			}

		} else if (canSeeBall) {
			if (distanceOtherPlayer < 10) {
				canSeeBallAction(directionOwnPlayer);
			} else {
				canSeeBallAction(directionOtherGoal);
			}
		} else {
			canSeeAnythingAction();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		canSeeNothing = false;
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
		distanceOtherGoal = 104 - distanceOwnGoal;
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
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		this.distanceOtherPlayer = distance;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		this.distanceOwnPlayer = distance;
		this.directionOwnPlayer = direction;
		this.directionToTurn = 90;
		this.distanceBallOwnPlayer = Math.sqrt((distanceBall * distanceBall) + (distanceOwnPlayer * distanceOwnPlayer));
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
		if (distanceOwnPlayer < 15 && (distanceBallOwnPlayer < distanceBall)) {
			getPlayer().turn(dirOwnGoal);
			getPlayer().dash(this.randomDashValueSlow());
		} else if (distanceOwnPlayer < 15 && (distanceBallOwnPlayer > distanceBall)) {
			getPlayer().turn(directionBall);
			getPlayer().dash(this.randomDashValueFast());
			if (distanceBall < 10) {
				if (distanceBall < 0.7) {
					// if he is, can he see the goal?
					if (canSeeGoal) {
						// if he can, is he less than 20 to his own goal?
						if (distGoal < 20) {
							// if he is, kick it hard away from the goal
							this.getPlayer().kick(60, 135);
						} else {
							// if he isn't, dribble the ball towards the other goal so he doesn't give away possession
							this.getPlayer().kick(20, dirGoalOther);
						}
						// turn towards the ball and face the other goal regardless if what happens
						getPlayer().turn(dirBall);
						getPlayer().turnNeck(dirGoalOther);
					// if he can't, can he see the other goal?
					} else if (canSeeGoalOther) {
						// if he can, is he less than 23 to the other goal?
						if (distGoalOther < 23) {
							// if he is, kick the ball as hard as he can towards the other goal to try and score
							this.getPlayer().kick(100, dirGoalOther);
						} else {
							// if he isn't, is he less than 2 from a player on the other team?
							if (distanceOtherPlayer < 2) {
								// if he is, attempt to pass in the direction of his own team
								this.getPlayer().kick(50, directionOwnPlayer);
							} else {
								// if he isn't, dribble towards the other goal so he doesn't give away possession
								this.getPlayer().kick(20, dirGoalOther);
							}
						}
						// turn towards the ball and face the other goal regardless if what happens
						getPlayer().turn(dirBall);
						getPlayer().turnNeck(dirGoalOther);
					} else {
						// if he can't see his own goal, or the other goal,  turn towards the ball and face the other goal regardless if what happens
						this.getPlayer().kick(20, dirGoalOther);
						getPlayer().turn(dirBall);
						getPlayer().turnNeck(dirGoalOther);
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
