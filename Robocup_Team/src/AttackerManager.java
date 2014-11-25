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
 * This class manages the Attackers.
 */
public class AttackerManager implements ControllerPlayer {
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
	/*
	 * in order: whether the player can see their own goal whether the player
	 * can see the other goal whether the player can see the side line whether
	 * the player is allowed to dribble whether the player can see their own
	 * penalty area whether the player is already seeing their own goal whether
	 * the player can see either end of the field whether the player can see the
	 * right flag of their own goal whether the player can see the right flag of
	 * the other goal whether the player can see the left flag of their own goal
	 * whether the player can see the left flag of the other goal whether the
	 * player needs to retreat/return to original position whether the player
	 * can see the ball whether the player is going for the ball
	 */
	private boolean canSeeGoal, canSeeGoalOther, canSeeSideline, dribble, canSeePenalty, alreadySeeingGoal, canSeeFieldEnd, canSeeGoalRight, canSeeGoalRightOther, canSeeGoalLeft, canSeeGoalLeftOther,
			needsToRetreat, canSeeBall, goingForBall = false;
	private ActionsPlayer player;
	private Random random = null;
	private static int count = 0;
	// allows the player to turn positive or negative based on this value
	private double dirMultiplier = 1.0;
	// the amount to turn to face the goal
	private double goalTurn;
	// the distance to a player on this player's own team
	private double distanceOwnPlayer;
	// the direction to a player on this player's own team
	private double directionOwnPlayer;
	// the distance from a player on this player's own team to the ball
	private double distanceBallOwnPlayer;
	// the distance from this player to a player on the other team
	private double distanceOtherPlayer;

	public AttackerManager() {
		random = new Random(System.currentTimeMillis() + count);
		count++;
	}

	/** {@inheritDoc} */
	@Override
	public void preInfo() {
		// reset values to default so they can be updated correctly on each tick
		distBall = 1000;
		distGoal = 1000;
		distGoalOther = 1000;
		distanceBallOwnPlayer = 20;
		dirGoalOther = 90;
		canSeeGoal = false;
		canSeeGoalOther = false;
		canSeeGoalLeft = false;
		canSeeGoalLeftOther = false;
		canSeeGoalRight = false;
		canSeeGoalRightOther = false;
		canSeePenalty = false;
		canSeeFieldEnd = false;
		canSeeSideline = false;
		canSeeBall = false;
		goingForBall = false;
		dribble = false;
		goalTurn = 0.0;
	}

	/** {@inheritDoc} */
	@Override
	public void postInfo() {
		// check to see if this player is close than 15 to the ball
		if (distBall < 15) {
			// if he is, can he see the ball?
			if (canSeeBall) {
				// if he can, turn towards the ball, face the other goal, dash
				// towards the ball, and set going for ball true
				if (distanceBallOwnPlayer > 8) {
					getPlayer().turn(dirBall);
					getPlayer().turnNeck(dirGoalOther);
					getPlayer().dash(randomDashValueVeryFast());
					goingForBall = true;
				} else {
					getPlayer().turn(dirGoalOther);
					getPlayer().turnNeck(dirBall);
					getPlayer().dash(randomDashValueFast());
				}
			}
			// if he is, is he less than 0.7 to the ball?
			if (distBall < 0.7) {
				// if he is, can he see the goal?
				if (canSeeGoal) {
					// if he can, is he less than 20 to his own goal?
					if (distGoal < 20) {
						// if he is, kick it hard away from the goal
						this.getPlayer().kick(60, 135);
					} else {
						// if he isn't, dribble the ball towards the other goal
						// so he doesn't give away possession
						this.getPlayer().kick(20, dirGoalOther);
					}
					// turn towards the ball and face the other goal regardless
					// if what happens
					getPlayer().turn(dirBall);
					getPlayer().turnNeck(dirGoalOther);
					// if he can't, can he see the other goal?
				} else if (canSeeGoalOther) {
					// if he can, is he less than 23 to the other goal?
					if (distGoalOther < 23) {
						// if he is, kick the ball as hard as he can towards the
						// other goal to try and score
						this.getPlayer().kick(100, dirGoalOther);
					} else {
						// if he isn't, is he less than 2 from a player on the
						// other team?
						if (distanceOtherPlayer < 2) {
							// if he is, attempt to pass in the direction of his
							// own team
							this.getPlayer().kick(50, directionOwnPlayer);
						} else {
							// if he isn't, dribble towards the other goal so he
							// doesn't give away possession
							this.getPlayer().kick(20, dirGoalOther);
						}
					}
					// turn towards the ball and face the other goal regardless
					// if what happens
					getPlayer().turn(dirBall);
					getPlayer().turnNeck(dirGoalOther);
				} else {
					// if he can't see his own goal, or the other goal, turn
					// towards the ball and face the other goal regardless if
					// what happens
					this.getPlayer().kick(20, dirGoalOther);
					getPlayer().turn(dirBall);
					getPlayer().turnNeck(dirGoalOther);
				}
			}
			/*
			 * if (canSeeGoal || canSeePenalty) {
			 * System.out.println("can see goal or penalty"); if (distBall < 2)
			 * { getPlayer().turn(dirBall);
			 * getPlayer().dash(randomDashValueFast()); } else {
			 * getPlayer().turn(dirGoalOther);
			 * getPlayer().dash(randomDashValueVeryFast()); } } else if
			 * (canSeeGoalOther) { System.out.println("can see goal other"); if
			 * (distBall < 2) { getPlayer().turn(dirBall);
			 * getPlayer().dash(randomDashValueFast()); } else { if
			 * (!goingForBall) getPlayer().turn(90);
			 * getPlayer().dash(randomDashValueFast()); } } else {
			 * System.out.println("else"); if (distBall < 2) {
			 * getPlayer().turn(dirBall);
			 * getPlayer().dash(randomDashValueFast()); } else { if
			 * (!goingForBall) getPlayer().turn(90);
			 * getPlayer().dash(randomDashValueFast()); } }
			 */
		} else {
			// if he isn't closer than 15, can he see the ball?
			if (canSeeBall) {
				// if he can, turn towards the ball, face the goal, and dash
				// towards the ball
				getPlayer().turn(dirBall);
				getPlayer().turnNeck(dirGoalOther);
				getPlayer().dash(randomDashValueFast());
			} else if (!canSeeBall) {
				// if he can't see the ball, can he see his own goal, and needs
				// to retreat?
				if (!canSeeGoal && !needsToRetreat) {
					// if he can't and doesn't need to retreat, can he see his
					// own penalty box?
					if (!canSeePenalty) {
						// if he can, turn 90 and dash to get to a new position
						getPlayer().turn(90);
						getPlayer().dash(randomDashValueFast());
						// if he can see his own penalty box, can he see his own
						// goal's left or right flag and he can't see the end of
						// the field
					} else if ((canSeeGoalLeft || canSeeGoalRight) && !canSeeFieldEnd) {
						// turn in a anti clockwise direction to face somewhere
						// more appropriate
						getPlayer().turn(-1.0 * goalTurn);
						getPlayer().dash(randomDashValueSlow());
					} else
						// turn in a clockwise direction to face somewhere more
						// appropriate
						getPlayer().turn(25 * dirMultiplier);
					// if he can see his own goal and needs to retreat
				} else {
					// can he see the other goal?
					if (!canSeeGoalOther) {
						// if he can't, turn 90 and run slowly to face somewhere
						// more appropriate
						getPlayer().turn(90);
						getPlayer().dash(randomDashValueSlow());
						// if he can see his own goal, is he greater than 50
						// away?
					} else if (distGoalOther > 50) {
						// is he already seeing the goal?
						if (!alreadySeeingGoal) {
							// if he is, turn towards the other goal and set the
							// flag to true
							getPlayer().turn(dirGoalOther);
							alreadySeeingGoal = true;
						}
						// dash very quickly regardless of what happens
						getPlayer().dash(randomDashValueVeryFast());
						// if he is less than 50 from the other goal
					} else {
						// set flag to false
						needsToRetreat = false;
						// can he already see the goal?
						if (alreadySeeingGoal) {
							getPlayer().turn(goalTurn);
							alreadySeeingGoal = false;
						} else
							alreadySeeingGoal = true;
					}
				}
			}
		}
		/*
		 * if (sidelineDistance < 0.5) { if (!goingForBall)
		 * getPlayer().turn(90); getPlayer().dash(randomDashValueSlow()); }
		 */
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
		// set the side line distance each tick
		canSeeSideline = true;
		sidelineDistance = distance;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeBall(double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		// set the distance and direction of the ball each tick
		distBall = distance;
		dirBall = direction;
		canSeeBall = true;
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearReferee(RefereeMessage refereeMessage) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoHearPlayMode(PlayMode playMode) {
		if (playMode == PlayMode.BEFORE_KICK_OFF)
			switch (this.getPlayer().getNumber()) {
			case 2:
				this.getPlayer().move(-10, 10);
				break;
			case 3:
				this.getPlayer().move(-10, -10);
				break;
			}
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
	public void infoSeeFlagRight(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
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
		canSeePenalty = true;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
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
	public void infoSeePlayerOther(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		distanceOtherPlayer = distance;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOwn(int number, boolean goalie, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		distanceOwnPlayer = distance;
		directionOwnPlayer = direction;
		distanceBallOwnPlayer = Math.sqrt((distBall * distBall) + (distanceOwnPlayer * distanceOwnPlayer));
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
