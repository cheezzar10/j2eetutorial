package odin.j2ee.clnt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class HttpNotificationsSender {
	public static void main(String[] args) throws Exception {
		System.out.println("sending notifications");
		
		System.out.printf("id: %s%n", UUID.randomUUID().toString());
		System.out.printf("time: %d%n", System.currentTimeMillis());
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		System.out.printf("message: %s", RandomStringUtils.random(16, true, false));
		
		int count = 512;
		for (int nid = 1;nid <= count;nid++) {
			System.out.printf("sending request: %d%n", nid);
			
			HttpPost sendReq = new HttpPost("http://10.0.1.3:8080/tutorial/notification/send");
			List<NameValuePair> params = new ArrayList<>(2);
			params.add(new BasicNameValuePair("userId", "1"));
			StringBuilder message = new StringBuilder();
			message.append(nid);
			message.append(":").append(RandomStringUtils.random(1024 * 32, true, false));
			params.add(new BasicNameValuePair("message", message.toString()));
			UrlEncodedFormEntity reqBody = new UrlEncodedFormEntity(params, Consts.UTF_8);
			sendReq.setEntity(reqBody);
			try (CloseableHttpResponse resp = httpClient.execute(sendReq)) {
				
			} catch (Exception ex) {
				ex.printStackTrace(System.out);
			}
		}
		
		httpClient.close();
	}
}