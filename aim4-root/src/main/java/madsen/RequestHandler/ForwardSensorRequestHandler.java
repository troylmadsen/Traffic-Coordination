package madsen.RequestHandler;

import java.util.List;

import aim4.im.v2i.RequestHandler.RequestHandler;
import aim4.im.v2i.policy.BasePolicy;
import aim4.im.v2i.policy.BasePolicy.ProposalFilterResult;
import aim4.im.v2i.policy.BasePolicy.ReserveParam;
import aim4.im.v2i.policy.BasePolicyCallback;
import aim4.im.v2i.policy.ExtendedBasePolicyCallback;
import aim4.msg.i2v.Reject;
import aim4.msg.v2i.Request;
import aim4.sim.StatCollector;

/**
 * The no-stop request handler.
 * 
 * @author Troy Madsen
 *
 */
public class ForwardSensorRequestHandler implements RequestHandler{
	/////////////////////////////////
	// CONSTANTS
	/////////////////////////////////

	/**
	 * The time window before the last vehicle inside the intersection
	 * leaving the intersection such that other vehicles can consider
	 * entering the intersection.
	 */
	public static final double DEFAULT_TIME_WINDOW_BEFORE_LAST_EXIT_VEHICLE = 0.1;

	/////////////////////////////////
	// PRIVATE FIELDS
	/////////////////////////////////

	/** The base policy */
	private ExtendedBasePolicyCallback basePolicy;



	/////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////

	/**
	 * Set the base policy call-back.
	 *
	 * @param basePolicy  the base policy's call-back
	 */
	@Override
	public void setBasePolicyCallback(BasePolicyCallback basePolicy) {
		if (basePolicy instanceof ExtendedBasePolicyCallback) {
			this.basePolicy = (ExtendedBasePolicyCallback)basePolicy;
		} else {
			throw new RuntimeException("The BasePolicyCallback for " +
					"AllStopRequestHandler must be ExtendedBasePolicyCallback.");
		}
	}

	/**
	 * Let the request handler act for a given time period.
	 *
	 * @param timeStep  the time period
	 */
	@Override
	public void act(double timeStep) {
		// do nothing
	}

	/**
	 * Process the request message.
	 *
	 * @param msg the request message
	 */
	@Override
	public void processRequestMsg(Request msg) {
		int vin = msg.getVin();

		// filter the proposals
		ProposalFilterResult filterResult =
				BasePolicy.standardProposalsFilter(msg.getProposals(),
						basePolicy.getCurrentTime());
//		if (filterResult.isNoProposalLeft()) {
//			basePolicy.sendRejectMsg(vin,
//					msg.getRequestId(),
//					filterResult.getReason());
//		}

		List<Request.Proposal> proposals = filterResult.getProposals();

		// try to see if reservation is possible for the remaining proposals.
	    ReserveParam reserveParam = basePolicy.findReserveParam(msg, proposals);
	    if (reserveParam != null) {
	      basePolicy.sendComfirmMsg(msg.getRequestId(), reserveParam);
	    } else {
	      basePolicy.sendRejectMsg(vin, msg.getRequestId(),
	                               Reject.Reason.NO_CLEAR_PATH);
	    }
	}

	/**
	 * Get the statistic collector.
	 *
	 * @return the statistic collector
	 */
	@Override
	public StatCollector<?> getStatCollector() {
		return null;
	}
}
