package odin.j2ee;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import odin.j2ee.api.NotificationSender;
import odin.j2ee.api.ClassicNotificationSender;

@WebServlet("/notification/send")
public class NotificationSenderServlet extends HttpServlet {
	private static final long serialVersionUID = -6148126330829108536L;

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@EJB
	private ClassicNotificationSender sender;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("send notification request received");
		
		String userIdStr = request.getParameter("userId");
		Integer userId = Integer.parseInt(userIdStr);
		
		String[] messages = request.getParameterValues("message");
		sender.sendMessages(userId, messages);
		
		String redirectUrl = request.getContextPath() + "/notification.html";
		log.debug("redirecting to: {}", redirectUrl);
		response.sendRedirect(redirectUrl);
	}
}
