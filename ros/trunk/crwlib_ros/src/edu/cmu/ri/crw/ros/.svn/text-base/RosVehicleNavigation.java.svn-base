package edu.cmu.ri.crw.ros;

import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.client.SimpleActionClient;
import org.ros.actionlib.server.DefaultSimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.message.crwlib_msgs.VehicleNavigationAction;
import org.ros.message.crwlib_msgs.VehicleNavigationActionFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationActionGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationActionResult;
import org.ros.message.crwlib_msgs.VehicleNavigationFeedback;
import org.ros.message.crwlib_msgs.VehicleNavigationGoal;
import org.ros.message.crwlib_msgs.VehicleNavigationResult;
import org.ros.node.Node;

/**
 * Contains boilerplate template classes that are necessary for ROS actionlib
 * implementation of vehicle navigation actions.
 * 
 * @author pkv
 *
 */
public class RosVehicleNavigation {

	/**
	 * Wrapper for utilizing a SimpleActionClient
	 * 
	 * @author kshaurya
	 * 
	 */
	public static final class Client
			extends
			SimpleActionClient<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

		public Client(
				Node parentNode,
				String nameSpace,
				ActionSpec<?, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> spec)
				throws RosException {
			super(parentNode, nameSpace, spec);

		}

		public Client(
				String nameSpace,
				ActionSpec<?, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> spec)
				throws RosException {
			super(nameSpace, spec);
		}

	}

	/**
	 * Wrapper for utilizing a DefaultSimpleActionServer
	 * 
	 * @author kshaurya
	 * 
	 */
	public static final class Server
			extends
			DefaultSimpleActionServer<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

		public Server(String nameSpace,
				Spec spec, SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> callbacks,
				boolean useBlockingGoalCallback) {
			super(nameSpace, spec, callbacks, useBlockingGoalCallback);
		}

		public Server(Node parent, String nameSpace,
				Spec spec, SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> callbacks,
				boolean useBlockingGoalCallback) {
			super(parent, nameSpace, spec, callbacks, useBlockingGoalCallback);
		}

	}

	/**
	 * Provides Actionlib specifications for the particular .action file. in
	 * this case VehicleNavigation.action
	 * 
	 * @author kshaurya
	 * 
	 */
	public static final class Spec
			extends
			ActionSpec<VehicleNavigationAction, VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> {

		public Spec() throws RosException {
			super(VehicleNavigationAction.class,
					"crwlib_msgs/VehicleNavigationAction",
					"crwlib_msgs/VehicleNavigationActionFeedback",
					"crwlib_msgs/VehicleNavigationActionGoal",
					"crwlib_msgs/VehicleNavigationActionResult",
					"crwlib_msgs/VehicleNavigationFeedback",
					"crwlib_msgs/VehicleNavigationGoal",
					"crwlib_msgs/VehicleNavigationResult");
		}

		@Override
		public Server buildSimpleActionServer(
				String nameSpace,
				SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> callbacks,
				boolean useBlockingGoalCallback) {

			return new Server(nameSpace, this,
					callbacks,
					useBlockingGoalCallback);

		}

		@Override
		public Server buildSimpleActionServer(
				Node node,
				String nameSpace,
				SimpleActionServerCallbacks<VehicleNavigationActionFeedback, VehicleNavigationActionGoal, VehicleNavigationActionResult, VehicleNavigationFeedback, VehicleNavigationGoal, VehicleNavigationResult> callbacks,
				boolean useBlockingGoalCallback) {

			return new Server(node, nameSpace, this,
					callbacks,
					useBlockingGoalCallback);

		}

		@Override
		public Client buildSimpleActionClient(String nameSpace) {

			Client sac = null;
			try {
				return new Client(nameSpace, this);
			} catch (RosException e) {
				e.printStackTrace();
			}
			return sac;

		}

		@Override
		public Client buildSimpleActionClient(Node node,
				String nameSpace) {

			Client sac = null;
			try {
				sac = new Client(node, nameSpace, this);
			} catch (RosException e) {
				e.printStackTrace();
			}
			return sac;

		}

	}
}
