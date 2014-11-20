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
	private double dirGoalOther = 180;
	private double distGoal = -1.0;
	private double distGoalOther = 1.0;
	private double sidelineDistance;
	private boolean canSeeGoal, canSeeGoalOther, canSeeSideline, dribble,
			canSeePenalty, alreadySeeingGoal, canSeeFieldEnd, canSeeGoalRight,
			canSeeGoalRightOther, canSeeGoalLeft, canSeeGoalLeftOther,
			needsToRetreat, canSeeBall, goingForBall = false;
	private ActionsPlayer player;
	private Random random = null;
	private static int count = 0;
	private double dirMultiplier = 1.0;
	private double goalTurn;

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
		if (distBall < 15) {
			if (canSeeBall) {
				getPlayer().turn(dirBall);
				getPlayer().dash(randomDashValueFast());
				goingForBall = true;
			}
			if (distBall < 0.7) {
				if (canSeeBall) {
					getPlayer().turn(dirBall);
					getPlayer().dash(randomDashValueFast());
					goingForBall = true;
				}
				System.out.println(goingForBall);
				if (canSeeGoal)
					this.getPlayer().kick(60, 135);
				else if (canSeeGoalOther) {
					this.getPlayer().kick(60, dirGoalOther);
				} else {
					this.getPlayer().kick(60, dirGoalOther);
				}
			} else if (canSeeGoal || canSeePenalty) {
				System.out.println("can see goal or penalty");
				if (distBall < 10) {
					getPlayer().turn(dirBall);
					getPlayer().dash(randomDashValueFast());
				} else {
					getPlayer().turn(dirGoalOther);
					getPlayer().dash(randomDashValueVeryFast());
				}
			} else if (canSeeGoalOther) {
				System.out.println("can see goal other");
				if (distBall < 2) {
					getPlayer().turn(dirBall);
					getPlayer().dash(randomDashValueFast());
				} else {
					if (!goingForBall)
						getPlayer().turn(90);
					getPlayer().dash(randomDashValueFast());
				}
			} else {
				System.out.println("else");
				if (distBall < 2) {
					getPlayer().turn(dirBall);
					getPlayer().dash(randomDashValueFast());
				} else {
					if (!goingForBall)
						getPlayer().turn(90);
					getPlayer().dash(randomDashValueFast());
				}
			}
		} else {
			if (!canSeeGoal && !needsToRetreat) {
				if (!canSeePenalty) {
					getPlayer().turn(90);
					getPlayer().dash(randomDashValueFast());
				} else if ((canSeeGoalLeft || canSeeGoalRight)
						&& !canSeeFieldEnd) {
					getPlayer().turn(-1.0 * goalTurn);
					getPlayer().dash(randomDashValueSlow());
				} else
					getPlayer().turn(25 * dirMultiplier);
			} else {
				if (!canSeeGoalOther) {
					getPlayer().turn(90);
					getPlayer().dash(randomDashValueSlow());
				} else if (distGoalOther > 50) {
					if (!alreadySeeingGoal) {
						getPlayer().turn(dirGoalOther);
						alreadySeeingGoal = true;
					}

					getPlayer().dash(randomDashValueVeryFast());
				} else {
					needsToRetreat = false;

					if (alreadySeeingGoal) {
						getPlayer().turn(goalTurn);
						alreadySeeingGoal = false;
					} else
						alreadySeeingGoal = true;
				}
			}
		}
		if (sidelineDistance < 10) {
			if (!goingForBall)
				getPlayer().turn(90);
			getPlayer().dash(randomDashValueSlow());
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
		canSeeSideline = true;
		sidelineDistance = distance;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeBall(double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
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
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagLeft(Flag flag, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOwn(Flag flag, double distance, double direction,
			double distChange, double dirChange, double bodyFacingDirection,
			double headFacingDirection) {
		canSeeFieldEnd = true;
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
		canSeePenalty = true;
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
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeePlayerOwn(int number, boolean goalie, double distance,
			double direction, double distChange, double dirChange,
			double bodyFacingDirection, double headFacingDirection) {
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
