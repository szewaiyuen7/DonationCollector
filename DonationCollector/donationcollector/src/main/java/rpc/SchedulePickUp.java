package rpc;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.ElasticSearchConnection;
import util.Email;

/**
 * Servlet implementation class SchedulePickUp
 */
public class SchedulePickUp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SchedulePickUp() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String itemId = request.getParameter("itemId");
		String ngoId = request.getParameter("ngoId");
		String pickUpNGOName = request.getParameter("ngoName");
		String availablePickUpTime = request.getParameter("pickUpDate");

		ElasticSearchConnection connection = new ElasticSearchConnection();
		connection.elasticSearchConnection();
		Map<String, Object> hit = connection.updateItemPickUpInfo(itemId, ngoId, pickUpNGOName, availablePickUpTime);
		try {
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (hit.isEmpty()) {
			response.sendError(404, "cannot find this item");
			return;
		}
		String status = (String) hit.get("itemStatus");
		String pickUpNGOId = (String) hit.get("pickUpNGOId");
		if (status.contentEquals("SCHEDULED") && pickUpNGOId.contentEquals(ngoId)) {
			response.getWriter().write("Sucessfully scheduled.");
			response.setStatus(204);

		} else {
			response.sendError(404, "Cannot schedule pick up for this item");
			return;
		}
		Email emailNotification = new Email();
		// System.out.println(hit);
		try {
			emailNotification.sendNotificationEmail(hit.get("itemName").toString(), hit.get("posterId").toString(),
					availablePickUpTime, pickUpNGOName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

// map empty 
