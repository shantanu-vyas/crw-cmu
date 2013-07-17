package edu.cmu.ri.crw.ros;

import org.ros.actionlib.ActionSpec;
import org.ros.actionlib.client.SimpleActionClient;
import org.ros.actionlib.server.DefaultSimpleActionServer;
import org.ros.actionlib.server.SimpleActionServerCallbacks;
import org.ros.exception.RosException;
import org.ros.message.crwlib_msgs.VehicleImageCaptureAction;
import org.ros.message.crwlib_msgs.VehicleImageCaptureActionFeedback;
import org.ros.message.crwlib_msgs.VehicleImageCaptureActionGoal;
import org.ros.message.crwlib_msgs.VehicleImageCaptureActionResult;
import org.ros.message.crwlib_msgs.VehicleImageCaptureFeedback;
import org.ros.message.crwlib_msgs.VehicleImageCaptureGoal;
import org.ros.message.crwlib_msgs.VehicleImageCaptureResult;
import org.ros.node.Node;

/**
 * Contains boilerplate template classes that are necessary for ROS actionlib
 * implementation of vehicle Imaging actions.
 * 
 * @author pkv
 *
 */
public class RosVehicleImaging {

	/**
	 * Wrapper for utilizing a SimpleActionClient
	 * 
	 * @author kshaurya
	 * 
	 */
	public static final class Client
			extends
			SimpleActionClient<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> {

		public Client(
				Node parentNode,
				String nameSpace,
				ActionSpec<?, VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> spec)
				throws RosException {
			super(parentNode, nameSpace, spec);

		}

		public Client(
				String nameSpace,
				ActionSpec<?, VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> spec)
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
			DefaultSimpleActionServer<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> {

		public Server(String nameSpace,
				Spec spec, SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> callbacks,
				boolean useBlockingGoalCallback) {
			super(nameSpace, spec, callbacks, useBlockingGoalCallback);
		}

		public Server(Node parent, String nameSpace,
				Spec spec, SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> callbacks,
				boolean useBlockingGoalCallback) {
			super(parent, nameSpace, spec, callbacks, useBlockingGoalCallback);
		}

	}

	/**
	 * Provides Actionlib specifications for the particular .action file. in
	 * this case VehicleImageCapture.action
	 * 
	 * @author kshaurya
	 * 
	 */
	public static final class Spec
			extends
			ActionSpec<VehicleImageCaptureAction, VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> {

		public Spec() throws RosException {
			super(VehicleImageCaptureAction.class,
					"crwlib_msgs/VehicleImageCaptureAction",
					"crwlib_msgs/VehicleImageCaptureActionFeedback",
					"crwlib_msgs/VehicleImageCaptureActionGoal",
					"crwlib_msgs/VehicleImageCaptureActionResult",
					"crwlib_msgs/VehicleImageCaptureFeedback",
					"crwlib_msgs/VehicleImageCaptureGoal",
					"crwlib_msgs/VehicleImageCaptureResult");
		}

		@Override
		public Server buildSimpleActionServer(
				String nameSpace,
				SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> callbacks,
				boolean useBlockingGoalCallback) {

			return new Server(nameSpace, this,
					callbacks,
					useBlockingGoalCallback);
		}

		@Override
		public Server buildSimpleActionServer(
				Node node,
				String nameSpace,
				SimpleActionServerCallbacks<VehicleImageCaptureActionFeedback, VehicleImageCaptureActionGoal, VehicleImageCaptureActionResult, VehicleImageCaptureFeedback, VehicleImageCaptureGoal, VehicleImageCaptureResult> callbacks,
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
