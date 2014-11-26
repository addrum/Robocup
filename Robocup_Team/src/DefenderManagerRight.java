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
public class DefenderManagerRight implements ControllerPlayer {
	private double distBall = 1000;
	private double dirBall = 0;
	private boolean canSeePenaltyRight = false;
	private ActionsPlayer player;
	private Random random = null;
	private static int count = 0;
	//mah code
	private boolean canSeeFlagLeft = false;
	private boolean canSeeFlagRight = false;
	private boolean canSeeCenter = false;
	private boolean canSeeBall = false;
	private double distCenter = 0;
	private double distPenaltyRight = 0;
	private double dirPenaltyRight = 0;
	private double dirCenter = 0;

	public DefenderManagerRight() {
		random = new Random(System.currentTimeMillis() + count);
		count++;
	}

	/** {@inheritDoc} */
	@Override
	public void preInfo() {
		distBall = 1000;
		canSeePenaltyRight = false;
		canSeeFlagLeft = false;
		canSeeFlagRight = false;
		canSeeCenter = false;
		canSeeBall = false;
		distCenter = 0;
		distPenaltyRight = 0;
		dirCenter = 0;
		dirPenaltyRight = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void postInfo() {									//////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(distBall < 20) { 									//if the ball is within a range of 20 from the agent (close)
			if(distBall < 0.7) { 								//|		if the ball is within a range of 0.7 from the agent (extremely close)
				if(canSeeCenter) { 								//|		|	if the agent can see the center of the field
					getPlayer().kick(100, dirCenter); 			//|		|	|	then the agent kicks
				} else if(canSeeFlagLeft && !canSeeCenter) {	//|		|	if the agent can't see the center of the field but can see the left touchline of the field
					getPlayer().kick(100, 90);					//|		|	|	then kick the ball 90 degrees clockwise from where the agent is currently facing
				} else if(canSeeFlagRight && !canSeeCenter) {	//|		|	if the agent can't see the center of the field but can see the right touchline of the field
					getPlayer().kick(100, -90);					//|		|	|	then kick the ball 90 degrees counterclockwise from where the agent is currently facing
				} else {										//|		|	if the agent can't see the center of the field and neither of the touchlines
																//|		|	this means he must be facing towards his own goal
					getPlayer().kick(100, 180);					//|		|	|	in which case he kicks the ball 180 degrees clockwise from where he is facing
																//|		|	in other words he shoots it towards the opposite of his own goal, being the center of the field
				}												//|		|
			} else {											//|		if the ball is not within a range of 0.7 from the agent (not extremely close)
				getPlayer().turn(dirBall);						//|		|	then turn in the direction of the ball
				getPlayer().dash(randomDashValueVeryFast());	//|		|	and dash very fast towards the ball
			}													//|		|
		} else {												//if the ball is not within a range of 20 from the agent (not close)
			if(canSeeCenter && distCenter < 55){				//|		if the agent can see the the center of the field and the distance to it is less than 55
				if(canSeePenaltyRight && !canSeeBall) {			//|		|	if the agent can also see the right corner of his team's penalty box but can't see the ball
																//|		|	(meaning he is behind the penalty box, closer to the field end)
					getPlayer().turn(dirPenaltyRight);			//|		|	|	then turn towards the right corner of his team's penalty box
					getPlayer().dash(randomDashValueVeryFast());//|		|	|	and dash very fast towards it
				} else if(distPenaltyRight<1){					//|		|	if the agent is in range 1 to the right corner of his team's penalty box
					getPlayer().turn(90);						//|		|	|	then turn the agent 90 degrees clockwise from where he is currently facing
				}												//|		|	|
			} else if(canSeePenaltyRight){						//|		if the agent can see the right corner of his own team's penalty box (but not the center of the field)
																//|		(also this is assuming the agent is at a greater distance than 55 from the center of the field)
				getPlayer().turn(dirPenaltyRight);				//|		|	then turn towards the right corner of the agent's own team's penalty box
				getPlayer().dash(randomDashValueVeryFast());	//|		|	and dash very fast towards it
			} else if(distPenaltyRight<1){						//|		if the agent is closer than 1 to the right corner of his own team's penalty box (assuming he can not
																//|		see the center of the field, he is further than 55 away from it and can't see the penalty box corner)
				getPlayer().turn(30);							//|		|	then just turn 30 degrees clockwise from where the agent is currently facing
																//|		|	(this produces a sort of lookout for when he can't see the ball by constantly rotating him so he
																//|		|	is always aware of his surroundings)
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
		//here we are saving data for when the agent sees the ball
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
			getPlayer().move(-40, 10);
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
		return "Defender";
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
		//here we save data for when the agent can see the right touchline of the field
		canSeeFlagRight = true;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagLeft(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		//here we save data for when the agent can see the left touchline of the field
		canSeeFlagLeft = true;
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagCenter(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
		//here we are saving data for when the agent can see the center of the field
		if(flag.compareTo(Flag.CENTER) ==0){
			canSeeCenter = true;
			dirCenter = direction;
			distCenter = distance;
		}
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
		//here we are saving data for when the agent sees the right corner of his own team's penalty box
		if(flag.compareTo(Flag.RIGHT)==0){
			canSeePenaltyRight = true;
			dirPenaltyRight = direction;
			distPenaltyRight = distance;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagPenaltyOther(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
	}

	/** {@inheritDoc} */
	@Override
	public void infoSeeFlagGoalOwn(Flag flag, double distance, double direction, double distChange, double dirChange, double bodyFacingDirection, double headFacingDirection) {
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
